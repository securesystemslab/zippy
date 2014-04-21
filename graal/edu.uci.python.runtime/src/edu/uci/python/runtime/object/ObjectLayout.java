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

import edu.uci.python.runtime.*;

/**
 * Maps names of instance variables to storage locations, which are either the offset of a primitive
 * field in <code>PythonObject</code>, or an index into an object array in <code>PythonObject</code>
 * . Object layouts are chained, with each having zero or one parents.
 * <p>
 * Object layouts are immutable, with the methods for adding new instance variables of generalising
 * the type of existing instance variables returning new object layouts.
 */
public class ObjectLayout {

    private final String originHint;

    private final ObjectLayout parent;

    private final Map<String, StorageLocation> storageLocations = new HashMap<>();

    private final int primitiveIntStorageLocationsUsed;
    private final int primitiveDoubleStorageLocationsUsed;
    private final int objectStorageLocationsUsed;

    private ObjectLayout(String originHint) {
        this.originHint = originHint;
        this.parent = null;
        primitiveIntStorageLocationsUsed = 0;
        primitiveDoubleStorageLocationsUsed = 0;
        objectStorageLocationsUsed = 0;
    }

    public ObjectLayout(String originHint, PythonContext context, ObjectLayout parent) {
        this(originHint, context, parent, new HashMap<String, Class>());
    }

    public ObjectLayout(String originHint, PythonContext context, ObjectLayout parent, Map<String, Class> storageTypes) {
        this.originHint = originHint;
        this.parent = parent;

        // Start our offsets from where the parent ends

        int primitiveIntStorageLocationIndex;
        int primitiveDoubleStorageLocationIndex;
        int objectStorageLocationIndex;

        if (parent == null) {
            primitiveIntStorageLocationIndex = 0;
            primitiveDoubleStorageLocationIndex = 0;
            objectStorageLocationIndex = 0;
        } else {
            primitiveIntStorageLocationIndex = parent.primitiveIntStorageLocationsUsed;
            primitiveDoubleStorageLocationIndex = parent.primitiveDoubleStorageLocationsUsed;
            objectStorageLocationIndex = parent.objectStorageLocationsUsed;
        }

        // Go through the variables we've been asked to store

        for (Entry<String, Class> entry : storageTypes.entrySet()) {
            // TODO: what if parent has it, but we need a more general type?

            final String name = entry.getKey();
            final Class type = entry.getValue();

            if (parent == null || parent.findStorageLocation(name) == null) {
                Class storageClass;

                if (context.getUseUnsafe()) {
                    if (type == Integer.class) {
                        if (primitiveIntStorageLocationIndex + 1 <= PythonBasicObject.PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT) {
                            storageClass = Integer.class;
                        } else {
                            storageClass = Object.class;
                        }
                    } else if (type == Double.class) {
                        if (primitiveDoubleStorageLocationIndex + 1 <= PythonBasicObject.PRIMITIVE_DOUBLE_STORAGE_LOCATIONS_COUNT) {
                            storageClass = Double.class;
                        } else {
                            storageClass = Object.class;
                        }
                    } else {
                        storageClass = Object.class;
                    }
                } else {
                    storageClass = Object.class;
                }

                if (storageClass == Integer.class) {
                    final IntStorageLocation newStorageLocation = new IntStorageLocation(this, primitiveIntStorageLocationIndex);
                    storageLocations.put(entry.getKey(), newStorageLocation);
                    primitiveIntStorageLocationIndex++;
                } else if (storageClass == Double.class) {
                    final FloatStorageLocation newStorageLocation = new FloatStorageLocation(this, primitiveDoubleStorageLocationIndex);
                    storageLocations.put(entry.getKey(), newStorageLocation);
                    primitiveDoubleStorageLocationIndex++;
                } else {
                    final ObjectStorageLocation newStorageLocation = new ObjectStorageLocation(this, objectStorageLocationIndex);
                    storageLocations.put(entry.getKey(), newStorageLocation);
                    objectStorageLocationIndex++;
                }
            }
        }

        primitiveIntStorageLocationsUsed = primitiveIntStorageLocationIndex;
        primitiveDoubleStorageLocationsUsed = primitiveDoubleStorageLocationIndex;
        objectStorageLocationsUsed = objectStorageLocationIndex;
    }

    public static final ObjectLayout empty() {
        return new ObjectLayout("(empty)");
    }

    /**
     * Create a new version of this layout, but with a different parent. The new parent probably
     * comes from the same Python class as it did, but it's a new layout because layouts are
     * immutable, so modifications to the superclass yields a new layout.
     */
    public ObjectLayout renew(PythonContext context, ObjectLayout newParent) {
        return new ObjectLayout(originHint + ".renewed", context, newParent, getStorageTypes());
    }

    /**
     * Create a new version of this layout but with a new variable.
     */
    public ObjectLayout withNewAttribute(PythonContext context, String name, Class type) {
        final Map<String, Class> storageTypes = getStorageTypes();
        storageTypes.put(name, type);
        return new ObjectLayout(originHint + ".withnew", context, parent, storageTypes);
    }

    public ObjectLayout withoutAttribute(PythonContext context, String name) {
        final Map<String, Class> storageTypes = getStorageTypes();
        storageTypes.remove(name);
        return new ObjectLayout(originHint + ".without", context, parent, storageTypes);
    }

    /**
     * Create a new version of this layout but with an existing variable generalized to support any
     * type.
     */
    public ObjectLayout withGeneralisedVariable(PythonContext context, String name) {
        return withNewAttribute(context, name, Object.class);
    }

    /**
     * Get a map of instance variable names to the type that they store.
     */
    public Map<String, Class> getStorageTypes() {
        Map<String, Class> storageTypes = new HashMap<>();

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
     * Get a map of instance variable names to the type that they store, but including both this
     * layout and all parent layouts.
     */
    public Map<String, StorageLocation> getAllStorageLocations() {
        final Map<String, StorageLocation> allStorageLocations = new HashMap<>();

        allStorageLocations.putAll(storageLocations);

        if (parent != null) {
            allStorageLocations.putAll(parent.getAllStorageLocations());
        }

        return allStorageLocations;
    }

    /**
     * Find a storage location from a name, including in parents.
     */
    public StorageLocation findStorageLocation(String name) {
        final StorageLocation storageLocation = storageLocations.get(name);

        if (storageLocation != null) {
            return storageLocation;
        }

        if (parent == null) {
            return null;
        }

        return parent.findStorageLocation(name);
    }

    public int getObjectStorageLocationsUsed() {
        return objectStorageLocationsUsed;
    }

    /**
     * Does this layout include another layout? That is, is that other layout somewhere in the chain
     * of parents? We say 'include' because all of the variables in a parent layout are available in
     * your layout as well.
     */
    public boolean contains(ObjectLayout other) {
        ObjectLayout layout = this;

        do {
            if (other == layout) {
                return true;
            }

            layout = layout.parent;
        } while (layout != null);

        return false;
    }

    public String getOriginHint() {
        return originHint;
    }

    public boolean isEmpty() {
        return storageLocations.isEmpty() && objectStorageLocationsUsed == 0 && //
                        this.primitiveIntStorageLocationsUsed == 0 && //
                        this.primitiveDoubleStorageLocationsUsed == 0;
    }

    @Override
    public String toString() {
        return "ObjectLayout:" + this.storageLocations.toString();
    }

}
