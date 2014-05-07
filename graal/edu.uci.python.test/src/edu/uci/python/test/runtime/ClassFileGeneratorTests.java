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
package edu.uci.python.test.runtime;

import static org.junit.Assert.*;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;

import org.junit.*;
import org.python.core.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;
import edu.uci.python.test.*;

public class ClassFileGeneratorTests {

    @Test
    public void emptyLayout() {
        PythonContext context = PythonTests.getContext();
        PythonObject obj = PythonContext.newPythonObjectInstance(context.getObjectClass());
        ClassFileGenerator cfg = new ClassFileGenerator(obj.getObjectLayout(), "Foo");
        String className = cfg.getValidClassName();
        byte[] data = cfg.generate();

        Class<?> loadedClass = BytecodeLoader.makeClass(className, data, PythonObject.class);
        assertTrue(loadedClass != null);

        PythonObject instance = null;

        try {
            instance = (PythonObject) loadedClass.getConstructor(new Class[]{PythonClass.class}).newInstance(context.getObjectClass());
        } catch (Exception e) {
            throw new RuntimeException();
        }

        assertTrue(instance != null);
        ObjectLayout layout = instance.getObjectLayout();
        assertEquals(context.getObjectClass().getInstanceObjectLayout(), layout);
    }

    @Test
    public void smallNumberOfFields() {
        PythonContext context = PythonTests.getContext();
        PythonClass pyclazz = new PythonClass(context.getObjectClass(), "Foo");
        PythonObject obj = PythonContext.newPythonObjectInstance(pyclazz);

        // Setup object layout.
        obj.setAttribute("int0", 0);
        obj.setAttribute("int1", 1);
        obj.setAttribute("int2", 2);
        obj.setAttribute("int3", 3);
        obj.setAttribute("int4", 4);
        obj.setAttribute("int5", 5);

        assertTrue(pyclazz.getInstanceObjectLayout().findStorageLocation("int5") != null);

        // Generate the storage class.
        ClassFileGenerator cfg = new ClassFileGenerator(pyclazz.getInstanceObjectLayout(), "Foo");
        byte[] data = cfg.generate();

        // Load the generated class.
        Class<?> loadedClass = BytecodeLoader.makeClass(cfg.getValidClassName(), data, PythonObject.class);
        assertTrue(loadedClass != null);

        // Instantiate
        PythonObject instance = null;

        try {
            instance = (PythonObject) loadedClass.getConstructor(new Class[]{PythonClass.class}).newInstance(pyclazz);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        assertTrue(instance != null);

        /**
         * Write and read int field.
         */
        ObjectLayout layout = pyclazz.getInstanceObjectLayout();
        IntStorageLocation i5location = (IntStorageLocation) layout.findStorageLocation("int5");
        i5location.writeInt(instance, 42);

        IntStorageLocation i0location = (IntStorageLocation) layout.findStorageLocation("int0");
        i0location.writeInt(instance, 24);

        try {
            assertEquals(42, i5location.readInt(instance));
            assertEquals(24, i0location.readInt(instance));
        } catch (UnexpectedResultException e) {
            throw new RuntimeException();
        }

        /**
         * Inspecting the loaded storage class.
         */

        try {
            for (int i = 0; i < 5; i++) {
                Field field = loadedClass.getDeclaredField("int" + i);
                assertTrue(field != null);
            }
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void methodHandleInvoke() {
        PythonContext context = PythonTests.getContext();
        PythonObject obj = PythonContext.newPythonObjectInstance(context.getObjectClass());
        ClassFileGenerator cfg = new ClassFileGenerator(obj.getObjectLayout(), "Foo");
        String className = cfg.getValidClassName();
        byte[] data = cfg.generate();

        Class<?> pyclazz = BytecodeLoader.makeClass(className, data, PythonObject.class);
        assertTrue(pyclazz != null);

        try {
            Lookup lookup = MethodHandles.lookup();
            MethodType mt = MethodType.methodType(PythonObject.class, PythonClass.class);
            MethodHandle ctor = lookup.findStatic(pyclazz, ClassFileGenerator.CREATE, mt);
            PythonObject instance = (PythonObject) ctor.invokeExact((PythonClass) context.getObjectClass());
            assertTrue(instance != null);
        } catch (Throwable e) {
            throw new RuntimeException();
        }
    }

}
