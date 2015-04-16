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

import java.lang.reflect.*;
import java.nio.file.*;

import org.junit.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;
import edu.uci.python.test.*;

public class ClassFileGeneratorTests {

    @Test
    public void emptyLayout() {
        PythonContext context = PythonTests.getContext();
        PythonClass pyclazz = new PythonClass(context, "Foo", context.getObjectClass());
        StorageClassGenerator cfg = new StorageClassGenerator(pyclazz);

        FlexiblePythonObjectStorageFactory factory = cfg.generate();
        PythonObject instance = factory.newInstance(context.getObjectClass());

        assertTrue(instance != null);
        ObjectLayout layout = instance.getObjectLayout();
        assertEquals(context.getObjectClass().getInstanceObjectLayout(), layout);
    }

    @Test
    public void smallNumberOfFields() {
        PythonContext context = PythonTests.getContext();
        PythonClass pyclazz = new PythonClass(context, "Foo", context.getObjectClass());
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
        StorageClassGenerator cfg = new StorageClassGenerator(pyclazz);

        // Instantiate
        PythonObject instance = cfg.generate().newInstance(pyclazz);

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
        Class<?> loadedClass = instance.getClass();
        try {
            for (int i = 0; i < 5; i++) {
                Field field = loadedClass.getDeclaredField("int" + i);
                assertTrue(field != null);
                assertTrue(field.getType() == int.class);
            }
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void methodHandleInvoke() {
        PythonContext context = PythonTests.getContext();
        PythonClass pyclazz = new PythonClass(context, "Foo", context.getObjectClass());
        StorageClassGenerator cfg = new StorageClassGenerator(pyclazz);
        FlexiblePythonObjectStorageFactory factory = cfg.generate();
        PythonObject instance = factory.newInstance(pyclazz);
        assertTrue(instance != null);
    }

    @Test
    public void layoutSwitch() {
        PythonContext context = PythonTests.getContext();
        PythonClass pyclazz = new PythonClass(context, "Foo", context.getObjectClass());
        PythonObject obj = PythonContext.newPythonObjectInstance(pyclazz);

        // Setup object layout.
        obj.setAttribute("int0", 0);
        obj.setAttribute("int1", 1);
        obj.setAttribute("int2", 2);
        obj.setAttribute("int3", 3);
        obj.setAttribute("int4", 4);
        obj.setAttribute("int5", 5);

        assertTrue(pyclazz.getInstanceObjectLayout().findStorageLocation("int5") != null);
        FlexiblePythonObjectStorageFactory factory = new StorageClassGenerator(pyclazz).generate();
        PythonObject newInstance = factory.newInstance(pyclazz);

        assertTrue(newInstance != null);
        assertTrue(newInstance.getObjectLayout() == pyclazz.getInstanceObjectLayout());

        try {
            for (int i = 0; i < 5; i++) {
                Field field = newInstance.getClass().getDeclaredField("int" + i);
                assertTrue(field != null);
                assertTrue(field.getType() == int.class);
            }
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException();
        }
    }

    @Test
    @SuppressWarnings("static-access")
    public void constructorNode() {
        PythonContext ctx = PythonTests.getContext();
        ctx.getPythonOptions().GenerateObjectStorage = true;

        String source = "class Foo:\n" + //
                        "    def __init__(self, n):\n" + //
                        "        self.n = n\n" + //
                        "for i in range(3):\n" + //
                        "    f = Foo(i)\n" + //
                        "    print(f.n)\n" + //
                        "\n";
        PythonTests.assertPrints("0\n1\n2\n", source);

        ctx.getPythonOptions().GenerateObjectStorage = false;
    }

    @Test
    @SuppressWarnings("static-access")
    public void layoutChange() {
        PythonContext ctx = PythonTests.getContext();
        ctx.getPythonOptions().GenerateObjectStorage = true;

        Path script = Paths.get("object-layout-change-after-ctor-test.py");
        PythonTests.assertPrints("42\n43\n", script);

        ctx.getPythonOptions().GenerateObjectStorage = false;
    }
}
