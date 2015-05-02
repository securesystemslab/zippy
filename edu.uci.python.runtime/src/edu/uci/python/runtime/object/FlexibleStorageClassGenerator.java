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

import java.lang.invoke.*;
import java.util.Map.Entry;

import org.objectweb.asm.*;
import org.python.core.*;
import org.python.modules.jffi.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.location.*;
import edu.uci.python.runtime.standardtype.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author zwei
 */
public final class FlexibleStorageClassGenerator {

    private static final String PYTHON_OBJECT_STORAGE_CLASS = "edu/uci/python/runtime/object/FlexiblePythonObjectStorage";
    private static final String PYTHON_CLASS = "edu/uci/python/runtime/standardtype/PythonClass";
    private static final String CLASSPATH = "edu/uci/python/runtime/object/";
    private static final String CREATE = "create";
    private static final String ATTRIBUTE_FIELD_PREFIX = "af_";

    private final PythonClass pythonClass;
    private final String validClassName;

    private final ClassWriter classWriter;
    private FieldVisitor fieldVisitor;
    private MethodVisitor methodVisitor;

    public FlexibleStorageClassGenerator(PythonClass pythonClass) {
        this.pythonClass = pythonClass;
        this.classWriter = new ClassWriter(0);
        // Python class name mangling. Replacing dot following the module name with a dollar sign.
        this.validClassName = CLASSPATH + pythonClass.getName().replace('.', '$') + pythonClass.getFlexibleObjectStorageVersion();
    }

    public FlexiblePythonObjectStorageFactory generate() {
        final Class<?> storageClass = BytecodeLoader.makeClass(getValidClassName(), generateClassData(), PythonObject.class);
        final MethodHandle ctor = lookupConstructor(storageClass);
        synchronizeObjectLayout(storageClass);

        if (PythonOptions.TraceObjectLayoutCreation) {
            // CheckStyle: stop system..print check
            System.out.println("[ZipPy] generate " + storageClass.toString());
            // CheckStyle: resume system..print check
        }

        return new FlexiblePythonObjectStorageFactory(ctor);
    }

    public static String getFieldName(String attributeName) {
        return ATTRIBUTE_FIELD_PREFIX + attributeName;
    }

    private String getValidClassName() {
        return validClassName.replace('/', '.');
    }

    private static MethodHandle lookupConstructor(Class<?> storageClass) {
        try {
            MethodType mt = MethodType.methodType(FlexiblePythonObjectStorage.class, PythonClass.class);
            return MethodHandles.lookup().findStatic(storageClass, CREATE, mt);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException();
        }
    }

    private void synchronizeObjectLayout(Class<?> storageClass) {
        ObjectLayout oldLayout = pythonClass.getInstanceObjectLayout();
        ObjectLayout newLayout = oldLayout.toFlexibleObjectLayout(storageClass);
        pythonClass.updateInstanceObjectLayout(newLayout);
    }

    private byte[] generateClassData() {
        CompilerAsserts.neverPartOfCompilation();

        classWriter.visit(V1_7, ACC_PUBLIC + ACC_SUPER, validClassName, null, PYTHON_OBJECT_STORAGE_CLASS, null);
        ObjectLayout old = pythonClass.getInstanceObjectLayout();

        for (Entry<String, StorageLocation> entry : old.getAllStorageLocations().entrySet()) {
            StorageLocation location = entry.getValue();
            addField(entry.getKey(), getPrimitiveStoredClass(location.getStoredClass()));
        }

        addConstructor();
        addConstructorAdaptor();

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private static Class<?> getPrimitiveStoredClass(Class<?> clazz) {
        if (clazz == Integer.class) {
            return int.class;
        } else if (clazz == Boolean.class) {
            return int.class;
        } else if (clazz == Double.class) {
            return double.class;
        } else {
            return Object.class;
        }
    }

    private void addField(String name, Class<?> clazz) {
        fieldVisitor = classWriter.visitField(ACC_PROTECTED, ATTRIBUTE_FIELD_PREFIX + name, CodegenUtils.ci(clazz), null, null);
        fieldVisitor.visitEnd();
    }

    private void addConstructor() {
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(L" + PYTHON_CLASS + ";)V", null, null);
        methodVisitor.visitCode();
        Label l0 = new Label();
        methodVisitor.visitLabel(l0);
        methodVisitor.visitLineNumber(53, l0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, PYTHON_OBJECT_STORAGE_CLASS, "<init>", "(L" + PYTHON_CLASS + ";)V", false);
        Label l1 = new Label();
        methodVisitor.visitLabel(l1);
        methodVisitor.visitLineNumber(54, l1);
        methodVisitor.visitInsn(RETURN);
        Label l2 = new Label();
        methodVisitor.visitLabel(l2);
        methodVisitor.visitLocalVariable("this", "L" + validClassName + ";", null, l0, l2, 0);
        methodVisitor.visitLocalVariable("pythonClass", "L" + PYTHON_CLASS + ";", null, l0, l2, 1);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }

    private void addConstructorAdaptor() {
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE, "(L" + PYTHON_CLASS + ";)L" + PYTHON_OBJECT_STORAGE_CLASS + ";", null, null);
        methodVisitor.visitCode();
        Label l0 = new Label();
        methodVisitor.visitLabel(l0);
        methodVisitor.visitLineNumber(57, l0);
        methodVisitor.visitTypeInsn(NEW, validClassName);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, validClassName, "<init>", "(L" + PYTHON_CLASS + ";)V", false);
        methodVisitor.visitInsn(ARETURN);
        Label l1 = new Label();
        methodVisitor.visitLabel(l1);
        methodVisitor.visitLocalVariable("clazz", "L" + PYTHON_CLASS + ";", null, l0, l1, 0);
        methodVisitor.visitMaxs(3, 1);
        methodVisitor.visitEnd();
    }
}
