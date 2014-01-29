/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.runtime.object;

import java.util.*;
import java.util.Map.Entry;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class PythonBasicObject {

    @CompilationFinal protected PythonClass pythonClass;

    private ObjectLayout objectLayout;

    public static final int PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT = 4;
    protected int primitiveIntStorageLocation0;
    protected int primitiveIntStorageLocation1;
    protected int primitiveIntStorageLocation2;
    protected int primitiveIntStorageLocation3;

    public static final int PRIMITIVE_DOUBLE_STORAGE_LOCATIONS_COUNT = 7;
    protected double primitiveDoubleStorageLocation0;
    protected double primitiveDoubleStorageLocation1;
    protected double primitiveDoubleStorageLocation2;
    protected double primitiveDoubleStorageLocation3;
    protected double primitiveDoubleStorageLocation4;
    protected double primitiveDoubleStorageLocation5;
    protected double primitiveDoubleStorageLocation6;

    // A bit map to indicate which primitives are set.
    protected int primitiveSetMap;

    protected Object[] objectStorageLocations = null;

    public PythonBasicObject(PythonClass pythonClass) {
        if (pythonClass != null) {
            unsafeSetPythonClass(pythonClass);
        } else {
            this.pythonClass = null;
        }

        objectLayout = ObjectLayout.EMPTY;
    }

    public PythonBasicObject(PythonClass pythonClass, PythonBasicObject module) {
        if (pythonClass != null) {
            unsafeSetPythonClass(pythonClass);
        } else {
            this.pythonClass = null;
        }

        this.objectLayout = module.objectLayout;
        this.objectStorageLocations = module.objectStorageLocations;
    }

    public PythonClass getPythonClass() {
        assert pythonClass != null;
        return pythonClass;
    }

    public ObjectLayout getObjectLayout() {
        return objectLayout;
    }

    /**
     * Does this object have an instance variable defined?
     */
    public boolean isOwnAttribute(String name) {
        return objectLayout.findStorageLocation(name) != null;
    }

    private void allocateObjectStorageLocations() {
        final int objectStorageLocationsUsed = objectLayout.getObjectStorageLocationsUsed();

        if (objectStorageLocationsUsed == 0) {
            objectStorageLocations = null;
        } else {
            objectStorageLocations = new Object[objectStorageLocationsUsed];
        }
    }

    public void unsafeSetPythonClass(PythonClass newPythonClass) {
        assert pythonClass == null;
        pythonClass = newPythonClass;
    }

    /**
     * The new APIs, more Python like..
     * <p>
     * Object and its Type (Class) maintain their own 'dictionary'.<br>
     * Class variables and methods are not inlined in the instantiated object's layout.<br>
     * Class attribute modification after the object instantiation does not affect the object's
     * layout. Likewise, object attribute modification after instantiation updates its own layout.
     * <p>
     * As described in the Python documentation, the attribute lookup order is:<br>
     * Object's dict -> its type's dict -> super classes' dicts.<br>
     * More advanced Method Resolution Order (C3 MRO), descriptors and special method overriding are
     * not covered here..
     */
    public Object getAttribute(String name) {
        // Find the storage location
        final StorageLocation storageLocation = objectLayout.findStorageLocation(name);

        // Continue the look up in PythonType.
        if (storageLocation == null) {
            return pythonClass == null ? PNone.NONE : pythonClass.getAttribute(name);
        }

        return storageLocation.read(this);
    }

    public void setAttribute(String name, Object value) {
        CompilerAsserts.neverPartOfCompilation();

        // Find the storage location
        StorageLocation storageLocation = objectLayout.findStorageLocation(name);

        if (storageLocation == null) {
            /*
             * It doesn't exist, so create a new layout for the class that includes it and update
             * the layout of this object.
             */
            updateLayout(objectLayout.withNewAttribute(pythonClass.getContext(), name, value.getClass()));
            storageLocation = objectLayout.findStorageLocation(name);
        }

        // Try to write to that storage location
        try {
            storageLocation.write(this, value);
        } catch (GeneralizeStorageLocationException e) {
            /*
             * It might not be able to store the type that we passed, if not generalize the class's
             * layout and update the layout of this object.
             */
            updateLayout(objectLayout.withGeneralisedVariable(pythonClass.getContext(), name));

            storageLocation = objectLayout.findStorageLocation(name);

            // Try to write to the generalized storage location

            try {
                storageLocation.write(this, value);
            } catch (GeneralizeStorageLocationException e1) {
                // We know that we just generalized it, so this should not happen
                throw new RuntimeException("Generalised an instance variable, but it still rejected the value");
            }
        }
    }

    public void deleteAttribute(String name) {
        // Find the storage location
        StorageLocation storageLocation = objectLayout.findStorageLocation(name);

        if (storageLocation == null) {
            throw Py.AttributeError(this + " object has no attribute " + name);
        }

        updateLayout(objectLayout.withoutAttribute(pythonClass.getContext(), name));
    }

    public void updateLayout(ObjectLayout newLayout) {
        // Get the current values of instance variables
        final Map<String, Object> instanceVariableMap = getAttributes();

        // Use new Layout
        objectLayout = newLayout;

        // Make all primitives as unset
        primitiveSetMap = 0;

        // Create a new array for objects
        allocateObjectStorageLocations();

        // Restore values
        setAttributes(instanceVariableMap);
    }

    public List<String> getAttributeNames() {
        if (objectLayout == null) {
            return Collections.emptyList();
        }

        final List<String> attributeNames = new ArrayList<>();

        for (Entry<String, StorageLocation> entry : objectLayout.getAllStorageLocations().entrySet()) {
            final String name = entry.getKey();
            attributeNames.add(name);
        }

        return attributeNames;
    }

    protected Map<String, Object> getAttributes() {
        if (objectLayout == null) {
            return Collections.emptyMap();
        }

        final Map<String, Object> attributesMap = new HashMap<>();

        for (Entry<String, StorageLocation> entry : objectLayout.getAllStorageLocations().entrySet()) {
            final String name = entry.getKey();
            final StorageLocation storageLocation = entry.getValue();

            if (storageLocation.isSet(this)) {
                attributesMap.put(name, storageLocation.read(this));
            }
        }

        return attributesMap;
    }

    protected void setAttributes(Map<String, Object> attributes) {
        for (Entry<String, Object> entry : attributes.entrySet()) {
            final StorageLocation storageLocation = objectLayout.findStorageLocation(entry.getKey());

            if (storageLocation == null) {
                // attribute is deleted
                continue;
            }

            try {
                storageLocation.write(this, entry.getValue());
            } catch (GeneralizeStorageLocationException e) {
                throw new RuntimeException("Should not have to be generalising when setting instance variables - " + entry.getValue().getClass().getName() + ", " +
                                storageLocation.getStoredClass().getName());
            }
        }
    }

    public abstract Assumption getUnmodifiedAssumption();

}
