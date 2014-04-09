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

import edu.uci.python.runtime.datatype.*;

/**
 * A storage location for any object.
 */
public final class ObjectStorageLocation extends StorageLocation {

    private final int index;

    public ObjectStorageLocation(ObjectLayout objectLayout, int index) {
        super(objectLayout);
        this.index = index;
    }

    @Override
    public boolean isSet(PythonBasicObject object) {
        return object.objectStorageLocations[index] != null;
    }

    @Override
    public Object read(PythonBasicObject object) {
        final Object result = object.objectStorageLocations[index];

        if (result == null) {
            return PNone.NONE;
        } else {
            return result;
        }
    }

    @Override
    public void write(PythonBasicObject object, Object value) {
        object.objectStorageLocations[index] = value;
    }

    @Override
    public Class getStoredClass() {
        return Object.class;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " at " + index;
    }

}
