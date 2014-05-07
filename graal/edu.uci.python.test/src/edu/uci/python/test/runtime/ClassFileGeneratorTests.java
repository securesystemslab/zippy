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

import org.junit.*;
import org.python.core.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;
import edu.uci.python.test.*;

public class ClassFileGeneratorTests {

    @Test
    public void test0() {
        PythonContext context = PythonTests.getContext();
        PythonObject obj = PythonContext.newPythonObjectInstance(context.getObjectClass());
        ClassFileGenerator cfg = new ClassFileGenerator(obj.getObjectLayout(), "Foo");
        String className = cfg.getFullClassName().replace('/', '.');
        byte[] data = cfg.generate();

        Class<?> loadedClass = BytecodeLoader.makeClass(className, data, PythonObject.class);
        assertTrue(loadedClass != null);

        PythonObject instance = null;

        try {
            instance = (PythonObject) loadedClass.getConstructor(new Class[]{PythonClass.class}).newInstance(context.getObjectClass());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException();
        }

        assertTrue(instance != null);
        ObjectLayout layout = instance.getObjectLayout();
        assertEquals(context.getObjectClass().getInstanceObjectLayout(), layout);
    }

}
