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

public abstract class FieldStorageLocation extends StorageLocation {

    private final int mask;
    protected final int index; // logical index not physical

    protected FieldStorageLocation(ObjectLayout objectLayout, int index) {
        super(objectLayout);
        mask = 1 << index;
        this.index = index;
    }

    @Override
    public boolean isSet(PythonBasicObject object) {
        return (object.primitiveSetMap & mask) != 0;
    }

    protected void markAsSet(PythonBasicObject object) {
        object.primitiveSetMap |= mask;
    }

    protected void markAsUnset(PythonBasicObject object) {
        object.primitiveSetMap &= ~mask;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " at " + index;
    }

    protected static long getExactPrimitiveDoubleOffsetOf(int index) {
        assert index >= 0 && index <= PythonBasicObject.PRIMITIVE_DOUBLE_STORAGE_LOCATIONS_COUNT - 1;
        try {
            return PythonUnsafe.UNSAFE.objectFieldOffset(PythonBasicObject.class.getDeclaredField("primitiveDouble" + index));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected static long getExactPrimitiveIntOffsetOf(int index) {
        assert index >= 0 && index <= PythonBasicObject.PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT - 1;
        try {
            return PythonUnsafe.UNSAFE.objectFieldOffset(PythonBasicObject.class.getDeclaredField("primitiveInt" + index));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected static long getExactFieldObjectOffsetOf(int index) {
        assert index >= 0 && index <= PythonBasicObject.FIELD_OBJECT_STORAGE_LOCATIONS_COUNT - 1;
        try {
            return PythonUnsafe.UNSAFE.objectFieldOffset(PythonBasicObject.class.getDeclaredField("fieldObject" + index));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
