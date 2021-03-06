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
import edu.uci.python.runtime.object.location.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class PythonObject implements Comparable<Object> {

    @CompilationFinal protected PythonClass pythonClass;

    protected ObjectLayout objectLayout;
    protected boolean usePrivateLayout;

    // A bit map to indicate which primitives are set.
    private int primitiveSetMap;

    protected Object[] arrayObjects = null;

    public PythonObject(PythonClass pythonClass) {
        this.pythonClass = pythonClass;
        objectLayout = pythonClass == null ? ObjectLayout.empty() : pythonClass.getInstanceObjectLayout();
        allocateSpillArray();
        assert verifyLayout();
    }

    public final PythonClass getPythonClass() {
        assert pythonClass != null;
        return pythonClass;
    }

    public final ObjectLayout getObjectLayout() {
        return objectLayout;
    }

    public final Object[] getSpillArray() {
        return arrayObjects;
    }

    public int getPrimitiveSetMap() {
        return primitiveSetMap;
    }

    public void setPrimitiveSetMap(int primitiveSetMap) {
        this.primitiveSetMap = primitiveSetMap;
    }

    public abstract void syncObjectLayoutWithClass();

    public abstract void updateLayout(ObjectLayout newLayout);

    /**
     * Does this object have an instance variable defined?
     */
    public final boolean isOwnAttribute(String name) {
        return objectLayout.findStorageLocation(name) != null;
    }

    public final StorageLocation getOwnValidLocation(String attributeId) {
        final StorageLocation location = objectLayout.findStorageLocation(attributeId);
        assert location != null;
        return location;
    }

    public PythonObject getValidStorageFullLookup(String attributeId) {
        PythonObject storage = null;

        if (isOwnAttribute(attributeId)) {
            storage = this;
        } else if (pythonClass != null) {
            storage = pythonClass.getValidStorageFullLookup(attributeId);
        }

        return storage;
    }

    protected void allocateSpillArray() {
        final int objectStorageLocationsUsed = objectLayout.getObjectStorageLocationsUsed();

        if (objectStorageLocationsUsed == 0) {
            arrayObjects = null;
        } else {
            arrayObjects = new Object[objectStorageLocationsUsed];
        }
    }

    /**
     * Object and its Type (Class) maintain their own 'dictionary'.<br>
     * Class variables and methods are not inlined in the instantiated object's layout.<br>
     * Class attribute modification after the object instantiation does not affect the object's
     * layout. Likewise, object attribute modification after instantiation updates the instance
     * layout of its class.
     * <p>
     * As described in the Python documentation, the attribute lookup order is:<br>
     * Object's dict -> its type's dict -> super classes' dicts.<br>
     * More advanced Method Resolution Order (C3 MRO) is not yet supported.
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
        assert verifyLayout();

        // Find the storage location
        StorageLocation storageLocation = objectLayout.findStorageLocation(name);

        if (storageLocation == null) {
            /*
             * It doesn't exist, so create a new layout for the class that includes it and update
             * the layout of this object.
             */
            updateLayout(objectLayout.addAttribute(name, value.getClass()));
            storageLocation = objectLayout.findStorageLocation(name);
        }

        // Try to write to that storage location
        try {
            storageLocation.write(this, value);
        } catch (StorageLocationGeneralizeException e) {
            /*
             * It might not be able to store the type that we passed, if not generalize the class's
             * layout and update the layout of this object.
             */
            updateLayout(objectLayout.generalizedAttribute(name));
            storageLocation = objectLayout.findStorageLocation(name);

            // Try to write to the generalized storage location
            try {
                storageLocation.write(this, value);
            } catch (StorageLocationGeneralizeException e1) {
                // We know that we just generalized it, so this should not happen
                throw new RuntimeException("Generalised an instance variable, but it still rejected the value");
            }
        }

        assert verifyLayout();
    }

    public void deleteAttribute(String name) {
        // Find the storage location
        StorageLocation storageLocation = objectLayout.findStorageLocation(name);

        if (storageLocation == null) {
            throw Py.AttributeError(this + " object has no attribute " + name);
        }

        updateLayout(objectLayout.deleteAttribute(name));
    }

    public void migrateTo(PythonObject to) {
        // Get the current values of instance variables
        final Map<String, Object> instanceVariableMap = getAttributes();
        to.setAttributes(instanceVariableMap);
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
            } catch (StorageLocationGeneralizeException e) {
                throw new RuntimeException("Should not have to be generalising when setting instance variables - " + entry.getValue().getClass().getName() + ", " +
                                storageLocation.getStoredClass().getName());
            }
        }
    }

    public boolean usePrivateLayout() {
        return usePrivateLayout;
    }

    public void switchToPrivateLayout() {
        usePrivateLayout = true;
    }

    public final Assumption getStableAssumption() {
        return getObjectLayout().getValidAssumption();
    }

    public final PythonClass asPythonClass() {
        if (this instanceof PythonClass) {
            return (PythonClass) this;
        }

        return getPythonClass();
    }

    public boolean verifyLayout() {
        return objectLayout.verifyObjectStorage(this);
    }

    @Override
    public int compareTo(Object o) {
        return this.equals(o) ? 0 : 1;
    }

    @Override
    public String toString() {
        return "<" + pythonClass.getName() + " object at " + hashCode() + ">";
    }

}
