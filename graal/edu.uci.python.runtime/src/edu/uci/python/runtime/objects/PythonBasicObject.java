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
package edu.uci.python.runtime.objects;

import java.util.*;
import java.util.Map.Entry;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;

public class PythonBasicObject {

    @CompilationFinal protected PythonClass pythonClass;

    private ObjectLayout objectLayout;

    public static final int PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT = 4;
    protected int primitiveIntStorageLocation1;
    protected int primitiveIntStorageLocation2;
    protected int primitiveIntStorageLocation3;
    protected int primitiveIntStorageLocation4;

    public static final int PRIMITIVE_DOUBLE_STORAGE_LOCATIONS_COUNT = 7;
    protected double primitiveDoubleStorageLocation1;
    protected double primitiveDoubleStorageLocation2;
    protected double primitiveDoubleStorageLocation3;
    protected double primitiveDoubleStorageLocation4;
    protected double primitiveDoubleStorageLocation5;
    protected double primitiveDoubleStorageLocation6;
    protected double primitiveDoubleStorageLocation7;

    // A bit map to indicate which primitives are set, so that they can be Nil
    protected int primitiveSetMap;

    protected Object[] objectStorageLocations = null;

    public PythonBasicObject(PythonClass pythonClass) {
        if (pythonClass != null) {
            unsafeSetPythonClass(pythonClass);
        } else {
            this.pythonClass = null;
        }

        if (pythonClass != null) {
            objectLayout = pythonClass.getObjectLayoutForInstances();
            allocateObjectStorageLocations();
        } else {
            objectLayout = null;
        }
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
    public boolean isInstanceVariableDefined(String name) {

        if (objectLayout != pythonClass.getObjectLayoutForInstances()) {
            updateLayout();
        }

        return objectLayout.findStorageLocation(name) != null;
    }

    /**
     * Set an instance variable to be a value. Slow path.
     */
    public void setInstanceVariable(String name, Object value) {
        CompilerAsserts.neverPartOfCompilation();

        // If the object's layout doesn't match the class, update
        if (objectLayout != pythonClass.getObjectLayoutForInstances()) {
            updateLayout();
        }

        // Find the storage location
        StorageLocation storageLocation = objectLayout.findStorageLocation(name);

        if (storageLocation == null) {
            /*
             * It doesn't exist, so create a new layout for the class that includes it and update
             * the layout of this object.
             */
            pythonClass.setObjectLayoutForInstances(pythonClass.getObjectLayoutForInstances().withNewVariable(pythonClass.getContext(), name, value.getClass()));
            updateLayout();

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

            pythonClass.setObjectLayoutForInstances(pythonClass.getObjectLayoutForInstances().withGeneralisedVariable(pythonClass.getContext(), name));
            updateLayout();

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

    /**
     * Get the value of an instance variable, or None if it isn't defined. Slow path.
     */
    public Object getInstanceVariable(String name) {
        CompilerAsserts.neverPartOfCompilation();

        // If the object's layout doesn't match the class, update
        if (objectLayout != pythonClass.getObjectLayoutForInstances()) {
            updateLayout();
        }

        // Find the storage location
        final StorageLocation storageLocation = objectLayout.findStorageLocation(name);

        // Get the value
        if (storageLocation == null) {
            return PNone.NONE;
        }

        return storageLocation.read(this);
    }

    public String[] getInstanceVariableNames() {
        final Set<String> instanceVariableNames = getInstanceVariables().keySet();
        return instanceVariableNames.toArray(new String[instanceVariableNames.size()]);
    }

    /**
     * Get a map of all instance variables.
     */
    protected Map<String, Object> getInstanceVariables() {
        if (objectLayout == null) {
            return Collections.emptyMap();
        }

        final Map<String, Object> instanceVariableMap = new HashMap<>();

        for (Entry<String, StorageLocation> entry : objectLayout.getAllStorageLocations().entrySet()) {
            final String name = entry.getKey();
            final StorageLocation storageLocation = entry.getValue();

            if (storageLocation.isSet(this)) {
                instanceVariableMap.put(name, storageLocation.read(this));
            }
        }

        return instanceVariableMap;
    }

    /**
     * Set instance variables from a map.
     */
    protected void setInstanceVariables(Map<String, Object> instanceVariables) {
        for (Entry<String, Object> entry : instanceVariables.entrySet()) {
            final StorageLocation storageLocation = objectLayout.findStorageLocation(entry.getKey());

            try {
                storageLocation.write(this, entry.getValue());
            } catch (GeneralizeStorageLocationException e) {
                throw new RuntimeException("Should not have to be generalising when setting instance variables - " + entry.getValue().getClass().getName() + ", " +
                                storageLocation.getStoredClass().getName());
            }
        }
    }

    /**
     * Update the layout of this object to match that of its class.
     */
    public void updateLayout() {

        // Get the current values of instance variables
        final Map<String, Object> instanceVariableMap = getInstanceVariables();

        // Use the layout of the class
        objectLayout = pythonClass.getObjectLayoutForInstances();

        // Make all primitives as unset
        primitiveSetMap = 0;

        // Create a new array for objects
        allocateObjectStorageLocations();

        // Restore values
        setInstanceVariables(instanceVariableMap);
    }

    private void allocateObjectStorageLocations() {
        final int objectStorageLocationsUsed = objectLayout.getObjectStorageLocationsUsed();

        if (objectStorageLocationsUsed == 0) {
            objectStorageLocations = null;
        } else {
            objectStorageLocations = new Object[objectStorageLocationsUsed];
        }
    }

    @Override
    public String toString() {
        return "#<" + pythonClass.getName() + ">";
    }

    public void unsafeSetPythonClass(PythonClass newPythonClass) {
        assert pythonClass == null;
        pythonClass = newPythonClass;
        updateLayout();
    }
}
