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
 * A storage location for floats.
 */
public class FloatStorageLocation extends PrimitiveStorageLocation {

    private final long offset;

    public FloatStorageLocation(ObjectLayout objectLayout, int index) {
        super(objectLayout, index);
        offset = getExactOffsetOf(index);
    }

    @Override
    public Object read(PythonBasicObject object) {
        try {
            return readDouble(object);
        } catch (UnexpectedResultException e) {
            return e.getResult();
        }
    }

    public double readDouble(PythonBasicObject object) throws UnexpectedResultException {
        if (isSet(object)) {
            return PythonUnsafe.UNSAFE.getDouble(object, offset);
        } else {
            throw new UnexpectedResultException(PNone.NONE);
        }
    }

    @Override
    public void write(PythonBasicObject object, Object value) throws GeneralizeStorageLocationException {
        if (value instanceof Double) {
            writeDouble(object, (double) value);
        } else if (value instanceof PNone) {
            markAsUnset(object);
        } else {
            throw new GeneralizeStorageLocationException();
        }
    }

    public void writeDouble(PythonBasicObject object, Double value) {
        PythonUnsafe.UNSAFE.putDouble(object, offset, value);
        markAsSet(object);
    }

    @Override
    public Class getStoredClass() {
        return Double.class;
    }

    private static long getExactOffsetOf(int index) {
        assert index >= 0 && index <= PythonBasicObject.PRIMITIVE_DOUBLE_STORAGE_LOCATIONS_COUNT - 1;
        try {
            return PythonUnsafe.UNSAFE.objectFieldOffset(PythonBasicObject.class.getDeclaredField("primitiveDoubleStorageLocation" + index));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
