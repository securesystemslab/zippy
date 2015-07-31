/*
 * Copyright (c) 2014, Regents of the University of California
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

import java.lang.reflect.*;

import sun.misc.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.nodes.NodeUtil.*;
import com.oracle.truffle.api.unsafe.*;

public class ObjectLayoutUtil {

    public static final FieldOffsetProvider OFFSET_PROVIDER = unsafeFieldOffsetProvider();
    private static final Unsafe unsafe = jdk.internal.jvmci.common.UnsafeAccess.unsafe;
    private static final UnsafeAccess unsafeAccess = Truffle.getRuntime().getCapability(UnsafeAccessFactory.class).createUnsafeAccess(unsafe);

    private static FieldOffsetProvider unsafeFieldOffsetProvider() {
        try {
            Field field = NodeFieldAccessor.class.getDeclaredField("unsafeFieldOffsetProvider");
            field.setAccessible(true);
            return (FieldOffsetProvider) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static UnsafeAccess getUnsafeAccess() {
        return unsafeAccess;
    }

    public static long getFieldOffset(NodeFieldAccessor field) {
        if (field instanceof NodeFieldAccessor.AbstractUnsafeNodeFieldAccessor) {
            return ((NodeFieldAccessor.AbstractUnsafeNodeFieldAccessor) field).getOffset();
        } else {
            try {
                java.lang.reflect.Field reflectionField = field.getDeclaringClass().getDeclaredField(field.getName());
                return unsafe.objectFieldOffset(reflectionField);
            } catch (NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static long getExactPrimitiveDoubleOffsetOf(int index) {
        assert index >= 0 && index <= FixedPythonObjectStorage.PRIMITIVE_DOUBLE_STORAGE_LOCATIONS_COUNT - 1;

        try {
            return OFFSET_PROVIDER.objectFieldOffset(FixedPythonObjectStorage.class.getDeclaredField("primitiveDouble" + index));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected static long getExactPrimitiveIntOffsetOf(int index) {
        assert index >= 0 && index <= FixedPythonObjectStorage.PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT - 1;
        try {
            return OFFSET_PROVIDER.objectFieldOffset(FixedPythonObjectStorage.class.getDeclaredField("primitiveInt" + index));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected static long getExactFieldObjectOffsetOf(int index) {
        assert index >= 0 && index <= FixedPythonObjectStorage.FIELD_OBJECT_STORAGE_LOCATIONS_COUNT - 1;
        try {
            return OFFSET_PROVIDER.objectFieldOffset(FixedPythonObjectStorage.class.getDeclaredField("fieldObject" + index));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected static long getExactFieldOffsetOf(Class<?> storageClass, String fieldName) throws NoSuchFieldException {
        Field field = storageClass.getDeclaredField(fieldName);
        assert field != null;
        return OFFSET_PROVIDER.objectFieldOffset(field);
    }

    public static final Object readObjectArrayUnsafeAt(Object[] array, int index, Object locationIdentity) {
        return unsafeAccess.getObject(array, Unsafe.ARRAY_OBJECT_BASE_OFFSET + Unsafe.ARRAY_OBJECT_INDEX_SCALE * index, true, locationIdentity);
    }

    public static final void writeObjectArrayUnsafeAt(Object[] array, int index, Object value, Object locationIdentity) {
        unsafeAccess.putObject(array, Unsafe.ARRAY_OBJECT_BASE_OFFSET + Unsafe.ARRAY_OBJECT_INDEX_SCALE * index, value, locationIdentity);
    }

    public static final int readIntArrayUnsafeAt(int[] array, int index, Object locationIdentity) {
        return unsafeAccess.getInt(array, Unsafe.ARRAY_INT_BASE_OFFSET + Unsafe.ARRAY_INT_INDEX_SCALE * index, true, locationIdentity);
    }

    public static final void writeIntArrayUnsafeAt(int[] array, int index, int value, Object locationIdentity) {
        unsafeAccess.putInt(array, Unsafe.ARRAY_INT_BASE_OFFSET + Unsafe.ARRAY_INT_INDEX_SCALE * index, value, locationIdentity);
    }

    public static final double readDoubleArrayUnsafeAt(double[] array, int index, Object locationIdentity) {
        return unsafeAccess.getDouble(array, Unsafe.ARRAY_DOUBLE_BASE_OFFSET + Unsafe.ARRAY_DOUBLE_INDEX_SCALE * index, true, locationIdentity);
    }

    public static final void writeDoubleArrayUnsafeAt(double[] array, int index, double value, Object locationIdentity) {
        unsafeAccess.putDouble(array, Unsafe.ARRAY_DOUBLE_BASE_OFFSET + Unsafe.ARRAY_DOUBLE_INDEX_SCALE * index, value, locationIdentity);
    }

    public static final char readCharArrayUnsafeAt(char[] array, int index, Object locationIdentity) {
        final short value = unsafeAccess.getShort(array, Unsafe.ARRAY_CHAR_BASE_OFFSET + Unsafe.ARRAY_CHAR_INDEX_SCALE * index, true, locationIdentity);
        return unsafeAccess.uncheckedCast(value, char.class, true, true);
    }

    public static final void writeCharArrayUnsafeAt(char[] array, int index, char value, Object locationIdentity) {
        final short castedValue = unsafeAccess.uncheckedCast(value, short.class, true, true);
        unsafeAccess.putShort(array, Unsafe.ARRAY_CHAR_BASE_OFFSET + Unsafe.ARRAY_CHAR_INDEX_SCALE * index, castedValue, locationIdentity);
    }

}
