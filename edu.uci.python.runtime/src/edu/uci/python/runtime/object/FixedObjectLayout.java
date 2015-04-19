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

import edu.uci.python.runtime.object.location.*;

public final class FixedObjectLayout extends ObjectLayout {

    private final int primitiveIntStorageLocationsUsed;
    private final int primitiveDoubleStorageLocationsUsed;
    private final int fieldObjectStorageLocationsUsed;
    private final int arrayObjectStorageLocationsUsed;

    public FixedObjectLayout(String originHint) {
        super(originHint);
        primitiveIntStorageLocationsUsed = 0;
        primitiveDoubleStorageLocationsUsed = 0;
        fieldObjectStorageLocationsUsed = 0;
        arrayObjectStorageLocationsUsed = 0;
    }

    public FixedObjectLayout(String originalHint, Map<String, Class<?>> storageTypes) {
        super(originalHint);
        int primitiveIntStorageLocationIndex = 0;
        int primitiveDoubleStorageLocationIndex = 0;
        int fieldObjectStorageLocationIndex = 0;
        int arrayObjectStorageLocationIndex = 0;

        // Go through the variables we've been asked to store
        for (Entry<String, Class<?>> entry : storageTypes.entrySet()) {
            final Class<?> type = entry.getValue();
            Class<?> storedClass;

            if (type == Integer.class) {
                if (primitiveIntStorageLocationIndex + 1 <= FixedPythonObjectStorage.PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT) {
                    storedClass = Integer.class;
                } else {
                    storedClass = Object.class;
                }
            } else if (type == Double.class) {
                if (primitiveDoubleStorageLocationIndex + 1 <= FixedPythonObjectStorage.PRIMITIVE_DOUBLE_STORAGE_LOCATIONS_COUNT) {
                    storedClass = Double.class;
                } else {
                    storedClass = Object.class;
                }
            } else if (type == Boolean.class) {
                if (primitiveIntStorageLocationIndex + 1 <= FixedPythonObjectStorage.PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT) {
                    storedClass = Boolean.class;
                } else {
                    storedClass = Object.class;
                }
            } else {
                storedClass = Object.class;
            }

            if (storedClass == Integer.class) {
                final long offset = ObjectLayoutUtil.getExactPrimitiveIntOffsetOf(primitiveIntStorageLocationIndex);
                final IntStorageLocation newStorageLocation = new IntStorageLocation(this, primitiveIntStorageLocationIndex, offset);
                storageLocations.put(entry.getKey(), newStorageLocation);
                primitiveIntStorageLocationIndex++;
            } else if (storedClass == Double.class) {
                final long offset = ObjectLayoutUtil.getExactPrimitiveDoubleOffsetOf(primitiveDoubleStorageLocationIndex);
                final DoubleStorageLocation newStorageLocation = new DoubleStorageLocation(this, primitiveDoubleStorageLocationIndex, offset);
                storageLocations.put(entry.getKey(), newStorageLocation);
                primitiveDoubleStorageLocationIndex++;
            } else if (storedClass == Boolean.class) {
                final long offset = ObjectLayoutUtil.getExactPrimitiveIntOffsetOf(primitiveIntStorageLocationIndex);
                final BooleanStorageLocation newStorageLocation = new BooleanStorageLocation(this, primitiveIntStorageLocationIndex, offset);
                storageLocations.put(entry.getKey(), newStorageLocation);
                primitiveIntStorageLocationIndex++;
            } else {
                if (fieldObjectStorageLocationIndex + 1 <= FixedPythonObjectStorage.FIELD_OBJECT_STORAGE_LOCATIONS_COUNT) {
                    final long offset = ObjectLayoutUtil.getExactFieldObjectOffsetOf(fieldObjectStorageLocationIndex);
                    final FieldObjectStorageLocation newStorageLocation = new FieldObjectStorageLocation(this, fieldObjectStorageLocationIndex, offset, type);
                    storageLocations.put(entry.getKey(), newStorageLocation);
                    fieldObjectStorageLocationIndex++;
                } else {
                    final ArrayObjectStorageLocation newStorageLocation = new ArrayObjectStorageLocation(this, arrayObjectStorageLocationIndex, type);
                    storageLocations.put(entry.getKey(), newStorageLocation);
                    arrayObjectStorageLocationIndex++;
                }
            }
        }

        primitiveIntStorageLocationsUsed = primitiveIntStorageLocationIndex;
        primitiveDoubleStorageLocationsUsed = primitiveDoubleStorageLocationIndex;
        fieldObjectStorageLocationsUsed = fieldObjectStorageLocationIndex;
        arrayObjectStorageLocationsUsed = arrayObjectStorageLocationIndex;
    }

    @Override
    public int getObjectStorageLocationsUsed() {
        return arrayObjectStorageLocationsUsed;
    }

    @Override
    public boolean isEmpty() {
        return storageLocations.isEmpty() && //
                        arrayObjectStorageLocationsUsed == 0 && //
                        primitiveIntStorageLocationsUsed == 0 && //
                        fieldObjectStorageLocationsUsed == 0 && //
                        primitiveDoubleStorageLocationsUsed == 0;
    }

    @Override
    protected ObjectLayout copy() {
        final Map<String, Class<?>> attributeTypes = getAttributeTypes();
        validAssumption.invalidate();
        return new FixedObjectLayout(originHint + "copy", attributeTypes);
    }

    @Override
    protected ObjectLayout addAttribute(String name, Class<?> type) {
        final Map<String, Class<?>> attributeTypes = getAttributeTypes();
        attributeTypes.put(name, type);
        validAssumption.invalidate();
        return new FixedObjectLayout(originHint + "+" + name, attributeTypes);
    }

    @Override
    protected ObjectLayout deleteAttribute(String name) {
        final Map<String, Class<?>> attributeTypes = getAttributeTypes();
        attributeTypes.remove(name);
        validAssumption.invalidate();
        return new FixedObjectLayout(originHint + "-" + name, attributeTypes);
    }

    @Override
    public ObjectLayout generalizedAttribute(String name) {
        final Map<String, Class<?>> storageTypes = getAttributeTypes();
        storageTypes.put(name, Object.class);
        validAssumption.invalidate();
        return new FixedObjectLayout(originHint + "!" + name, storageTypes);
    }

    @Override
    protected boolean verifyObjectStorage(PythonObject objectStorage) {
        assert FixedPythonObjectStorage.class.isAssignableFrom(objectStorage.getClass());
        return true;
    }

}
