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

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.datatype.*;

/**
 * A storage location for ints.
 */
public class IntStorageLocation extends PrimitiveStorageLocation {

    private final long offset;

    public IntStorageLocation(ObjectLayout objectLayout, int index) {
        super(objectLayout, index);
        offset = getExactOffsetOf(index);
    }

    @Override
    public Object read(PythonBasicObject object) {
        try {
            return readInt(object);
        } catch (UnexpectedResultException e) {
            return e.getResult();
        }
    }

    public int readInt(PythonBasicObject object) throws UnexpectedResultException {
        if (isSet(object)) {
            return PythonUnsafe.UNSAFE.getInt(object, offset);
        } else {
            throw new UnexpectedResultException(PNone.NONE);
        }
    }

    public boolean readBoolean(PythonBasicObject object) throws UnexpectedResultException {
        if (isSet(object)) {
            return PythonUnsafe.UNSAFE.getInt(object, offset) == 1;
        } else {
            throw new UnexpectedResultException(PNone.NONE);
        }
    }

    @Override
    public void write(PythonBasicObject object, Object value) throws GeneralizeStorageLocationException {
        if (value instanceof Integer) {
            writeInt(object, (int) value);
        } else if (value instanceof PNone) {
            markAsUnset(object);
        } else {
            throw new GeneralizeStorageLocationException();
        }
    }

    public void writeInt(PythonBasicObject object, int value) {
        PythonUnsafe.UNSAFE.putInt(object, offset, value);
        markAsSet(object);
    }

    public void writeBoolean(PythonBasicObject object, boolean value) {
        PythonUnsafe.UNSAFE.putInt(object, offset, value ? 1 : 0);
        markAsSet(object);
    }

    @Override
    public Class getStoredClass() {
        return Integer.class;
    }

    private static long getExactOffsetOf(int index) {
        assert index >= 0 && index <= PythonBasicObject.PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT - 1;
        try {
            return PythonUnsafe.UNSAFE.objectFieldOffset(PythonBasicObject.class.getDeclaredField("primitiveIntStorageLocation" + index));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " at " + index;
    }

}
