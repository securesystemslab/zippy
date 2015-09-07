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

import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.nodes.NodeUtil.*;

import edu.uci.python.runtime.object.location.*;

public class ObjectLayoutUtil {

    public static final FieldOffsetProvider OFFSET_PROVIDER = unsafeFieldOffsetProvider();

    private static FieldOffsetProvider unsafeFieldOffsetProvider() {
        try {
            Field field = NodeFieldAccessor.class.getDeclaredField("unsafeFieldOffsetProvider");
            field.setAccessible(true);
            return (FieldOffsetProvider) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getFieldOffset(NodeFieldAccessor field) {
        if (field instanceof NodeFieldAccessor.AbstractUnsafeNodeFieldAccessor) {
            return ((NodeFieldAccessor.AbstractUnsafeNodeFieldAccessor) field).getOffset();
        } else {
            try {
                java.lang.reflect.Field reflectionField = field.getDeclaringClass().getDeclaredField(field.getName());
                return UnsafeAccess.objectFieldOffset(reflectionField);
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
        return UnsafeAccess.getObject(array, Unsafe.ARRAY_OBJECT_BASE_OFFSET + Unsafe.ARRAY_OBJECT_INDEX_SCALE * index, true, locationIdentity);
    }

    public static final void writeObjectArrayUnsafeAt(Object[] array, int index, Object value, Object locationIdentity) {
        UnsafeAccess.putObject(array, Unsafe.ARRAY_OBJECT_BASE_OFFSET + Unsafe.ARRAY_OBJECT_INDEX_SCALE * index, value, locationIdentity);
    }

    public static final int readIntArrayUnsafeAt(int[] array, int index, Object locationIdentity) {
        return UnsafeAccess.getInt(array, Unsafe.ARRAY_INT_BASE_OFFSET + Unsafe.ARRAY_INT_INDEX_SCALE * index, true, locationIdentity);
    }

    public static final void writeIntArrayUnsafeAt(int[] array, int index, int value, Object locationIdentity) {
        UnsafeAccess.putInt(array, Unsafe.ARRAY_INT_BASE_OFFSET + Unsafe.ARRAY_INT_INDEX_SCALE * index, value, locationIdentity);
    }

    public static final double readDoubleArrayUnsafeAt(double[] array, int index, Object locationIdentity) {
        return UnsafeAccess.getDouble(array, Unsafe.ARRAY_DOUBLE_BASE_OFFSET + Unsafe.ARRAY_DOUBLE_INDEX_SCALE * index, true, locationIdentity);
    }

    public static final void writeDoubleArrayUnsafeAt(double[] array, int index, double value, Object locationIdentity) {
        UnsafeAccess.putDouble(array, Unsafe.ARRAY_DOUBLE_BASE_OFFSET + Unsafe.ARRAY_DOUBLE_INDEX_SCALE * index, value, locationIdentity);
    }

    public static final char readCharArrayUnsafeAt(char[] array, int index, Object locationIdentity) {
        return UnsafeAccess.getChar(array, Unsafe.ARRAY_CHAR_BASE_OFFSET + Unsafe.ARRAY_CHAR_INDEX_SCALE * index, true, locationIdentity);
    }

    public static final void writeCharArrayUnsafeAt(char[] array, int index, char value, Object locationIdentity) {
        UnsafeAccess.putChar(array, Unsafe.ARRAY_CHAR_BASE_OFFSET + Unsafe.ARRAY_CHAR_INDEX_SCALE * index, value, locationIdentity);
    }

    public static Object getObject(Node toMatch, long nodeOffset, boolean b, Object object) {
        return UnsafeAccess.getObject(toMatch, nodeOffset, b, object);
    }

    public static boolean getBoolean(PythonObject object, long offset, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") StorageLocation storageLocation) {
        return UnsafeAccess.getBoolean(object, offset);
    }

    public static void putBoolean(PythonObject object, long offset, boolean value, @SuppressWarnings("unused") StorageLocation storageLocation) {
        UnsafeAccess.putBoolean(object, offset, value);
    }

    public static double getDouble(PythonObject object, long offset, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") StorageLocation storageLocation) {
        return UnsafeAccess.getDouble(object, offset);
    }

    public static void putDouble(PythonObject object, long offset, double value, @SuppressWarnings("unused") StorageLocation storageLocation) {
        UnsafeAccess.putDouble(object, offset, value);
    }

    public static int getInt(PythonObject object, long offset, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") StorageLocation storageLocation) {
        return UnsafeAccess.getInt(object, offset);
    }

    public static void putInt(PythonObject object, long offset, int value, @SuppressWarnings("unused") StorageLocation storageLocation) {
        UnsafeAccess.putInt(object, offset, value);
    }

    public static Object getObject(PythonObject object, long offset, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") StorageLocation storageLocation) {
        return UnsafeAccess.getObject(object, offset);
    }

    public static void putObject(PythonObject object, long offset, Object value, @SuppressWarnings("unused") StorageLocation storageLocation) {
        UnsafeAccess.putObject(object, offset, value);
    }

    private static final class UnsafeAccess {
        private static final Unsafe UNSAFE = jdk.internal.jvmci.common.UnsafeAccess.unsafe;

        public static void putBoolean(PythonObject object, long offset, boolean value) {
            UNSAFE.putBoolean(object, offset, value);
        }

        public static boolean getBoolean(PythonObject object, long offset) {
            return UNSAFE.getBoolean(object, offset);
        }

        public static void putInt(PythonObject object, long offset, int value) {
            UNSAFE.putInt(object, offset, value);
        }

        public static int getInt(PythonObject object, long offset) {
            return UNSAFE.getInt(object, offset);
        }

        public static void putDouble(PythonObject object, long offset, double value) {
            UNSAFE.putDouble(object, offset, value);
        }

        public static double getDouble(PythonObject object, long offset) {
            return UNSAFE.getDouble(object, offset);
        }

        public static void putObject(PythonObject object, long offset, Object value) {
            UNSAFE.putObject(object, offset, value);
        }

        public static Object getObject(PythonObject object, long offset) {
            return UNSAFE.getObject(object, offset);
        }

        public static Object getObject(Object[] array, long i, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") Object locationIdentity) {
            return UNSAFE.getObject(array, i);
        }

        public static Object getObject(Node node, long nodeOffset, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") Object object) {
            return UNSAFE.getObject(node, nodeOffset);
        }

        public static void putObject(Object[] array, long i, Object value, @SuppressWarnings("unused") Object locationIdentity) {
            UNSAFE.putObject(array, i, value);
        }

        public static void putChar(char[] array, long i, char value, @SuppressWarnings("unused") Object locationIdentity) {
            UNSAFE.putChar(array, i, value);
        }

        public static char getChar(char[] array, long i, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") Object locationIdentity) {
            return UNSAFE.getChar(array, i);
        }

        public static int getInt(int[] array, long i, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") Object locationIdentity) {
            return UNSAFE.getInt(array, i);
        }

        public static void putInt(int[] array, long i, int value, @SuppressWarnings("unused") Object locationIdentity) {
            UNSAFE.putInt(array, i, value);
        }

        public static double getDouble(double[] array, long i, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") Object locationIdentity) {
            return UNSAFE.getDouble(array, i);
        }

        public static void putDouble(double[] array, long i, double value, @SuppressWarnings("unused") Object locationIdentity) {
            UNSAFE.putDouble(array, i, value);
        }

        public static long objectFieldOffset(Field reflectionField) {
            return UNSAFE.objectFieldOffset(reflectionField);
        }
    }

}
