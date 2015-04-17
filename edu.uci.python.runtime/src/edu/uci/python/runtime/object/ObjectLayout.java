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

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.object.location.*;

/**
 * Maps the names of instance attributes to storage locations, which are either the offset of a
 * field in {@link PythonObject}, or an index into the object array in {@link PythonObject}. Object
 * layouts are immutable, with the methods for adding new instance variables of generalizing the
 * type of existing instance variables returning new object layouts.
 *
 * @author zwei
 */
public class ObjectLayout {

    protected final String originHint;
    protected final Assumption validAssumption;
    protected final Map<String, StorageLocation> storageLocations = new HashMap<>();

    private final int primitiveIntStorageLocationsUsed;
    private final int primitiveDoubleStorageLocationsUsed;
    private final int fieldObjectStorageLocationsUsed;
    private final int arrayObjectStorageLocationsUsed;

    public ObjectLayout(String originHint) {
        this.originHint = originHint;
        primitiveIntStorageLocationsUsed = 0;
        primitiveDoubleStorageLocationsUsed = 0;
        fieldObjectStorageLocationsUsed = 0;
        arrayObjectStorageLocationsUsed = 0;
        validAssumption = Truffle.getRuntime().createAssumption(originHint);
    }

    protected ObjectLayout(String originHint, Map<String, Class<?>> storageTypes) {
        this.originHint = originHint;
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
        validAssumption = Truffle.getRuntime().createAssumption(originHint + " ObjectLayout valid");
    }

    /**
     * Creates an ObjectLayout attached to a storage instance of a specified Java class.
     *
     * @param objectStorageClass storage type
     */
    protected ObjectLayout(String originHint, Map<String, Class<?>> storageTypes, Class<?> objectStorageClass) {
        this.originHint = originHint;
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
                long offset = ObjectLayoutUtil.getExactFieldOffsetOf(objectStorageClass, name);

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

        primitiveIntStorageLocationsUsed = primitiveIntStorageLocationIndex;
        primitiveDoubleStorageLocationsUsed = primitiveDoubleStorageLocationIndex;
        fieldObjectStorageLocationsUsed = fieldObjectStorageLocationIndex;
        arrayObjectStorageLocationsUsed = arrayObjectStorageLocationIndex;
        validAssumption = Truffle.getRuntime().createAssumption(originHint + " ObjectLayout valid");
    }

    public static final ObjectLayout empty() {
        return new ObjectLayout("(empty)");
    }

    public final Assumption getValidAssumption() {
        return validAssumption;
    }

    /**
     * Create a new version of this layout but with a new variable.
     */
    protected ObjectLayout withNewAttribute(String name, Class<?> type) {
        final Map<String, Class<?>> storageTypes = getStorageTypes();
        storageTypes.put(name, type);
        validAssumption.invalidate();
        return new ObjectLayout(originHint + "+" + name, storageTypes);
    }

    protected ObjectLayout withoutAttribute(String name) {
        final Map<String, Class<?>> storageTypes = getStorageTypes();
        storageTypes.remove(name);
        validAssumption.invalidate();
        return new ObjectLayout(originHint + "-" + name, storageTypes);
    }

    /**
     * Create a new version of this layout but with an existing variable generalized to support any
     * type.
     */
    public ObjectLayout withGeneralisedVariable(String name) {
        final Map<String, Class<?>> storageTypes = getStorageTypes();
        storageTypes.put(name, Object.class);
        validAssumption.invalidate();
        return new ObjectLayout(originHint + "!" + name, storageTypes);
    }

    protected ObjectLayout switchToFlexibleObjectStorageClass(Class<?> objectStorageClass) {
        validAssumption.invalidate();
        return new ConservativeObjectLayout(originHint + ".switch", getStorageTypes(), objectStorageClass);
    }

    /**
     * Get a map of instance variable names to the type that they store.
     */
    public Map<String, Class<?>> getStorageTypes() {
        Map<String, Class<?>> storageTypes = new HashMap<>();

        for (Entry<String, StorageLocation> entry : storageLocations.entrySet()) {
            final String name = entry.getKey();
            final StorageLocation storageLocation = entry.getValue();

            if (storageLocation.getStoredClass() != null) {
                storageTypes.put(name, storageLocation.getStoredClass());
            }
        }

        return storageTypes;
    }

    /**
     * Get a map of instance variable names to the type that they store.
     */
    public Map<String, StorageLocation> getAllStorageLocations() {
        final Map<String, StorageLocation> allStorageLocations = new HashMap<>();
        allStorageLocations.putAll(storageLocations);
        return allStorageLocations;
    }

    /**
     * Find a storage location from a name.
     */
    public StorageLocation findStorageLocation(String name) {
        final StorageLocation storageLocation = storageLocations.get(name);

        if (storageLocation != null) {
            return storageLocation;
        }

        return null;
    }

    public int getObjectStorageLocationsUsed() {
        return arrayObjectStorageLocationsUsed;
    }

    public String findAttributeId(StorageLocation location) {
        for (Entry<String, StorageLocation> entry : storageLocations.entrySet()) {
            if (entry.getValue() == location) {
                return entry.getKey().toString();
            }
        }

        throw new IllegalStateException();
    }

    public String getOriginHint() {
        return originHint;
    }

    public boolean isEmpty() {
        return storageLocations.isEmpty() && //
                        arrayObjectStorageLocationsUsed == 0 && //
                        primitiveIntStorageLocationsUsed == 0 && //
                        fieldObjectStorageLocationsUsed == 0 && //
                        primitiveDoubleStorageLocationsUsed == 0;
    }

    @Override
    public String toString() {
        return super.toString() + " " + this.storageLocations.toString();
    }

}
