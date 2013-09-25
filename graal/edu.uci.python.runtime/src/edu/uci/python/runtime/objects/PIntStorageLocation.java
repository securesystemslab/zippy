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

import sun.misc.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.datatypes.*;

/**
 * A storage location for PInts.
 */
public class PIntStorageLocation extends PrimitiveStorageLocation {

    private static final long FIRST_OFFSET = getFirstOffset();

    private final long offset;

    public PIntStorageLocation(ObjectLayout objectLayout, int index) {
        super(objectLayout, index);
        offset = FIRST_OFFSET + index * Unsafe.ARRAY_INT_INDEX_SCALE;
    }

    @Override
    public Object read(PythonBasicObject object) {
        try {
            return readFixnum(object);
        } catch (UnexpectedResultException e) {
            return e.getResult();
        }
    }

    public int readFixnum(PythonBasicObject object) throws UnexpectedResultException {
        if (isSet(object)) {
            return PythonUnsafe.UNSAFE.getInt(object, offset);
        } else {
            throw new UnexpectedResultException(PNone.NONE);
        }
    }

    @Override
    public void write(PythonBasicObject object, Object value) throws GeneralizeStorageLocationException {
        if (value instanceof Integer) {
            writeFixnum(object, (int) value);
        } else if (value instanceof PNone) {
            markAsUnset(object);
        } else {
            throw new GeneralizeStorageLocationException();
        }
    }

    public void writeFixnum(PythonBasicObject object, int value) {
        PythonUnsafe.UNSAFE.putInt(object, offset, value);
        markAsSet(object);
    }

    @Override
    public Class getStoredClass() {
        return Integer.class;
    }

    private static long getFirstOffset() {
        try {
            return PythonUnsafe.UNSAFE.objectFieldOffset(PythonBasicObject.class.getDeclaredField("primitiveIntStorageLocation1"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
