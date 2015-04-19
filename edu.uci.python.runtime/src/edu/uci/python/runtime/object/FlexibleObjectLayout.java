/*
 * Copyright (c) 2015, Regents of the University of California
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
import java.util.Map.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.location.*;

/**
 * The layout descriptor attached to a FlexiblePythonObjectStorage. What is different here is that
 * since the object storage in this case is generated, the layout extending needs to preserve the
 * existing layout specified in the old layout. Otherwise, we are shuffling attributes randomly on
 * the object storage.
 *
 * @author zwei
 *
 */
public final class FlexibleObjectLayout extends ObjectLayout {

    private final int arrayObjectStorageLocationsUsed;
    private final Class<?> storageClass;
    private final ObjectLayout predecessor;
    private final Assumption isOptimalAssumption;

    protected FlexibleObjectLayout(String originHint, Class<?> storageClass, ObjectLayout predecessor) {
        super(originHint);
        this.arrayObjectStorageLocationsUsed = 0;
        this.storageClass = storageClass;
        this.predecessor = predecessor;
        this.isOptimalAssumption = Truffle.getRuntime().createAssumption();
        assert FlexiblePythonObjectStorage.class.isAssignableFrom(storageClass);

        if (PythonOptions.TraceObjectLayoutCreation) {
            // CheckStyle: stop system..print check
            System.out.println("[ZipPy] create " + this.toString());
            // CheckStyle: resume system..print check
        }
    }

    protected FlexibleObjectLayout(String originHint, Map<String, Class<?>> storageTypes, Class<?> objectStorageClass, ObjectLayout predecessor) {
        super(originHint);
        int primitiveIntStorageLocationIndex = 0;
        int primitiveDoubleStorageLocationIndex = 0;
        int fieldObjectStorageLocationIndex = 0;
        int arrayObjectStorageLocationIndex = 0;

        // Go through the variables we've been asked to store
        for (Entry<String, Class<?>> entry : storageTypes.entrySet()) {
            final String name = entry.getKey();
            final Class<?> type = entry.getValue();
            StorageLocation newStorageLocation;

            try {
                long offset = ObjectLayoutUtil.getExactFieldOffsetOf(objectStorageClass, FlexibleStorageClassGenerator.getFieldName(name));

                // Field storage location
                if (type == Integer.class) {
                    newStorageLocation = new IntStorageLocation(this, primitiveIntStorageLocationIndex++, offset);
                } else if (type == Boolean.class) {
                    newStorageLocation = new BooleanStorageLocation(this, primitiveIntStorageLocationIndex++, offset);
                } else if (type == Double.class) {
                    newStorageLocation = new DoubleStorageLocation(this, primitiveDoubleStorageLocationIndex++, offset);
                } else {
                    newStorageLocation = new FieldObjectStorageLocation(this, fieldObjectStorageLocationIndex++, offset, type);
                }
            } catch (NoSuchFieldException e) {
                // Spill to object array
                newStorageLocation = new ArrayObjectStorageLocation(this, arrayObjectStorageLocationIndex++, type);
            }

            storageLocations.put(entry.getKey(), newStorageLocation);
        }

        this.arrayObjectStorageLocationsUsed = arrayObjectStorageLocationIndex;
        this.storageClass = objectStorageClass;
        this.predecessor = predecessor;
        this.isOptimalAssumption = Truffle.getRuntime().createAssumption();
        assert FlexiblePythonObjectStorage.class.isAssignableFrom(storageClass);

        if (PythonOptions.TraceObjectLayoutCreation) {
            // CheckStyle: stop system..print check
            System.out.println("[ZipPy] create " + this.toString());
            // CheckStyle: resume system..print check
        }
    }

    public static FlexibleObjectLayout empty(Class<?> storageClass) {
        return new FlexibleObjectLayout("(empty)", storageClass, null);
    }

    @Override
    public int getObjectStorageLocationsUsed() {
        return arrayObjectStorageLocationsUsed;
    }

    @Override
    public boolean isEmpty() {
        return storageLocations.isEmpty() && arrayObjectStorageLocationsUsed == 0;
    }

    @Override
    public Assumption getCtorValidAssumption() {
        return isOptimalAssumption;
    }

    public final Assumption getIsOptimalAssumption() {
        return isOptimalAssumption;
    }

    @Override
    protected ObjectLayout copy() {
        final Map<String, Class<?>> attributeTypes = getAttributeTypes();
        validAssumption.invalidate();
        return new FlexibleObjectLayout(originHint + "copy", attributeTypes, storageClass, predecessor);
    }

    @Override
    protected ObjectLayout addAttribute(String name, Class<?> type) {
        final Map<String, Class<?>> attributeTypes = getAttributeTypes();
        attributeTypes.put(name, type);
        validAssumption.invalidate();
        isOptimalAssumption.invalidate();
        return new FlexibleObjectLayout(originHint + "+" + name, attributeTypes, storageClass, predecessor);
    }

    @Override
    protected ObjectLayout deleteAttribute(String name) {
        final Map<String, Class<?>> attributeTypes = getAttributeTypes();
        attributeTypes.remove(name);
        validAssumption.invalidate();
        return new FlexibleObjectLayout(originHint + "-" + name, attributeTypes, storageClass, predecessor);
    }

    @Override
    public ObjectLayout generalizedAttribute(String name) {
        final Map<String, Class<?>> attributeTypes = getAttributeTypes();
        attributeTypes.put(name, Object.class);
        validAssumption.invalidate();
        isOptimalAssumption.invalidate();
        return new FlexibleObjectLayout(originHint + "!" + name, attributeTypes, storageClass, predecessor);
    }

    @Override
    protected boolean verifyObjectStorage(PythonObject objectStorage) {
        Class<?> clazz = objectStorage.getClass();
        assert FlexiblePythonObjectStorage.class.isAssignableFrom(clazz);
        int arrayObjectStorageLocationsIndex = 0;

        for (Entry<String, StorageLocation> entry : storageLocations.entrySet()) {
            final String name = entry.getKey();
            final StorageLocation storageLocation = entry.getValue();

            if (storageLocation instanceof ArrayObjectStorageLocation) {
                arrayObjectStorageLocationsIndex++;
            } else {
                try {
                    ObjectLayoutUtil.getExactFieldOffsetOf(clazz, FlexibleStorageClassGenerator.getFieldName(name));
                } catch (NoSuchFieldException e) {
                    return false;
                }
            }
        }

        return arrayObjectStorageLocationsIndex == (objectStorage.arrayObjects != null ? objectStorage.arrayObjects.length : 0);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + storageClass.getSimpleName() + ") " + this.storageLocations.toString();
    }

}
