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

import org.python.core.*;

import com.oracle.truffle.api.*;

public final class ArrayObjectStorageLocation extends StorageLocation {

    private final int index;
    private final Class<?> storedClass;

    public ArrayObjectStorageLocation(ObjectLayout objectLayout, int index, Class<?> storedClass) {
        super(objectLayout);
        this.index = index;
        this.storedClass = storedClass;
    }

    @Override
    public boolean isSet(PythonObject object) {
        return object.arrayObjects[index] != null;
    }

    @Override
    public Object read(PythonObject object) {
        final Object result = ObjectLayoutUtil.readObjectArrayUnsafeAt(object.arrayObjects, index, this);

        if (result != null) {
            return result;
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw Py.AttributeError(object + " object has no attribute " + getObjectLayout().findAttributeId(this));
    }

    @Override
    public void write(PythonObject object, Object value) {
        ObjectLayoutUtil.writeObjectArrayUnsafeAt(object.arrayObjects, index, value, this);
    }

    @Override
    public Class<?> getStoredClass() {
        return storedClass;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " at " + index;
    }

}
