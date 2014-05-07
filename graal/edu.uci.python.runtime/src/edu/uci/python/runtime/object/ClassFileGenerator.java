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

import java.util.Map.Entry;

import org.objectweb.asm.*;
import org.python.modules.jffi.*;

import com.oracle.truffle.api.*;

import static org.objectweb.asm.Opcodes.*;

public class ClassFileGenerator {

    private static final String PYTHON_OBJECT_CLASS = "edu/uci/python/runtime/object/PythonObject";
    private static final String CLASSPATH = "edu/uci/python/runtime/object/";

    private final ObjectLayout layout;
    private final String className;

    private final ClassWriter classWriter;
    private FieldVisitor fieldVisitor;
    private MethodVisitor methodVisitor;

    public ClassFileGenerator(ObjectLayout layout, String className) {
        this.layout = layout;
        this.classWriter = new ClassWriter(0);
        this.className = CLASSPATH + className;
    }

    public String getValidClassName() {
        return className.replace('/', '.');
    }

    public byte[] generate() {
        CompilerAsserts.neverPartOfCompilation();

        classWriter.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, PYTHON_OBJECT_CLASS, null);

        for (Entry<String, StorageLocation> entry : layout.getAllStorageLocations().entrySet()) {
            StorageLocation location = entry.getValue();
            addField(entry.getKey(), fixStoredClass(location.getStoredClass()));
        }

        addConstructor();

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private static Class fixStoredClass(Class clazz) {
        if (clazz == Integer.class) {
            return int.class;
        } else if (clazz == Boolean.class) {
            return boolean.class;
        } else if (clazz == Double.class) {
            return double.class;
        } else {
            return Object.class;
        }
    }

    private void addField(String name, Class clazz) {
        fieldVisitor = classWriter.visitField(ACC_PROTECTED, name, CodegenUtils.ci(clazz), null, null);
        fieldVisitor.visitEnd();
    }

    private void addConstructor() {
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(Ledu/uci/python/runtime/standardtype/PythonClass;)V", null, null);
        methodVisitor.visitCode();
        Label l0 = new Label();
        methodVisitor.visitLabel(l0);
        methodVisitor.visitLineNumber(53, l0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "edu/uci/python/runtime/object/PythonObject", "<init>", "(Ledu/uci/python/runtime/standardtype/PythonClass;)V", false);
        Label l1 = new Label();
        methodVisitor.visitLabel(l1);
        methodVisitor.visitLineNumber(54, l1);
        methodVisitor.visitInsn(RETURN);
        Label l2 = new Label();
        methodVisitor.visitLabel(l2);
        methodVisitor.visitLocalVariable("this", "L" + className + ";", null, l0, l2, 0);
        methodVisitor.visitLocalVariable("pythonClass", "Ledu/uci/python/runtime/standardtype/PythonClass;", null, l0, l2, 1);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }

}
