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
public abstract class ObjectLayout {

    protected final String originHint;
    protected final Assumption validAssumption;
    protected final Map<String, StorageLocation> storageLocations = new HashMap<>();

    public ObjectLayout(String originHint) {
        this.originHint = originHint;
        validAssumption = Truffle.getRuntime().createAssumption(originHint);
    }

    /**
     * Creates an empty object layout of the default type. Currently it's a
     * {@link FixedObjectLayout}.
     */
    public static final ObjectLayout empty() {
        return new FixedObjectLayout("(empty)");
    }

    public final Assumption getValidAssumption() {
        return validAssumption;
    }

    public abstract boolean isEmpty();

    public abstract Assumption getCtorValidAssumption();

    public abstract int getObjectStorageLocationsUsed();

    protected abstract ObjectLayout copy();

    protected abstract ObjectLayout addAttribute(String name, Class<?> type);

    protected abstract ObjectLayout deleteAttribute(String name);

    protected abstract ObjectLayout generalizedAttribute(String name);

    protected abstract boolean verifyObjectStorage(PythonObject objectStorage);

    protected ObjectLayout toFlexibleObjectLayout(Class<?> objectStorageClass) {
        if (!(this instanceof FlexibleObjectLayout)) {
            validAssumption.invalidate();
        }
        return new FlexibleObjectLayout(originHint + ".toflex", getAttributeTypes(), objectStorageClass, null);
    }

    /**
     * Get a map of attribute names to the type that they store.
     */
    public final Map<String, Class<?>> getAttributeTypes() {
        Map<String, Class<?>> attributeTypes = new HashMap<>();

        for (Entry<String, StorageLocation> entry : storageLocations.entrySet()) {
            final String name = entry.getKey();
            final StorageLocation storageLocation = entry.getValue();

            if (storageLocation.getStoredClass() != null) {
                attributeTypes.put(name, storageLocation.getStoredClass());
            }
        }

        return attributeTypes;
    }

    /**
     * Get a map of attribute names to the type that they store.
     */
    public final Map<String, StorageLocation> getAllStorageLocations() {
        final Map<String, StorageLocation> allStorageLocations = new HashMap<>();
        allStorageLocations.putAll(storageLocations);
        return allStorageLocations;
    }

    /**
     * Find a storage location from a name.
     */
    public final StorageLocation findStorageLocation(String name) {
        final StorageLocation storageLocation = storageLocations.get(name);

        if (storageLocation != null) {
            return storageLocation;
        }

        return null;
    }

    public String findAttributeId(StorageLocation location) {
        for (Entry<String, StorageLocation> entry : storageLocations.entrySet()) {
            if (entry.getValue() == location) {
                return entry.getKey().toString();
            }
        }

        throw new IllegalStateException();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + this.storageLocations.toString();
    }

}
