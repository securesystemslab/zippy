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
package edu.uci.python.test.runtime;

import static org.junit.Assert.*;

import org.junit.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;
import edu.uci.python.test.*;

public class ObjectLayoutTests {

    public static class DummyPythonBasicObject extends PythonBasicObject {

        public DummyPythonBasicObject(PythonClass pythonClass) {
            super(pythonClass);
        }

        @Override
        public Assumption getStableAssumption() {
            return Truffle.getRuntime().createAssumption();
        }

        @Override
        public void invalidateStableAssumption() {
        }
    }

    @Test
    public void objectWithPrimitiveAttributes() {
        // Create a class and an instance
        final PythonContext context = PythonTests.getContext();
        final PythonClass classA = new PythonClass(context, null, "A");
        final PythonBasicObject obj = new DummyPythonBasicObject(classA);
        final ObjectLayout objLayoutBefore = obj.getObjectLayout();
        obj.setAttribute("foo", 42);
        obj.setAttribute("bar", 24);

        final ObjectLayout objLayoutAfter = obj.getObjectLayout();
        assertNotSame(objLayoutBefore, objLayoutAfter);

        assertEquals(42, obj.getAttribute("foo"));
        assertEquals(24, obj.getAttribute("bar"));
    }

    @Test
    public void objectPrimitiveAttributeOverflow() {
        // Create a class and an instance
        final PythonContext context = PythonTests.getContext();
        final PythonClass classA = new PythonClass(context, null, "A");
        final PythonBasicObject obj = new DummyPythonBasicObject(classA);

        for (int i = 0; i < 100; i++) {
            obj.setAttribute("foo" + i, i);
        }

        final ObjectLayout layout = obj.getObjectLayout();
        int objectStorageLocationUsed = layout.getObjectStorageLocationsUsed();
        assertEquals(100 - PythonBasicObject.PRIMITIVE_INT_STORAGE_LOCATIONS_COUNT, objectStorageLocationUsed);

        for (int i = 0; i < 100; i++) {
            assertEquals(i, obj.getAttribute("foo" + i));
        }
    }

    @Test
    public void booleanAttribute() {
        // Create a class and an instance
        final PythonContext context = PythonTests.getContext();
        final PythonClass classA = new PythonClass(context, null, "A");
        final PythonBasicObject obj = new DummyPythonBasicObject(classA);

        obj.setAttribute("boolean1", true);
        obj.setAttribute("boolean0", false);
        StorageLocation location1 = obj.getOwnValidLocation("boolean1");
        StorageLocation location0 = obj.getOwnValidLocation("boolean0");
        assertTrue(location1 instanceof IntStorageLocation);
        assertTrue(location0 instanceof IntStorageLocation);
        assertEquals(location1.read(obj), true);
        assertEquals(location0.read(obj), false);
    }

    @Test
    public void classAttributes() {
        // Create a class and an instance
        final PythonContext context = PythonTests.getContext();
        final PythonClass classA = new PythonClass(context, null, "A");

        // Add class variable
        classA.setAttribute("foo", 42);
        classA.setAttribute("bar", 24);

        assertEquals(42, classA.getAttribute("foo"));
        assertEquals(24, classA.getAttribute("bar"));

        final PythonBasicObject obj = new DummyPythonBasicObject(classA);
        int initialSize = obj.getObjectLayout().getAllStorageLocations().size();
        assertEquals(0, initialSize);

        // Get "foo" from its type
        assertEquals(42, obj.getAttribute("foo"));
        assertEquals(24, obj.getAttribute("bar"));

        assertFalse(obj.isOwnAttribute("foo"));

        // Override "foo" in object instance
        obj.setAttribute("foo", 43);
        assertEquals(43, obj.getAttribute("foo"));

        assertTrue(obj.isOwnAttribute("foo"));
    }
}
