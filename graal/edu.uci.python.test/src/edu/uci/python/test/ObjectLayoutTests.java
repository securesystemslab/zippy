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
package edu.uci.python.test;

import static org.junit.Assert.*;

import org.junit.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;
import edu.uci.python.runtime.objects.*;

public class ObjectLayoutTests {

    @Test
    public void testNewInstanceVariable() {
        final PythonContext context = new PythonContext();

        // Create a class and an instance
        final PythonClass classA = new PythonClass(context, null, "A");
        final ObjectLayout layoutClassA = classA.getObjectLayoutForInstances();

        final PythonBasicObject objectA = new PythonBasicObject(classA);
        final ObjectLayout layoutObjectA = objectA.getObjectLayout();

        // Add an instance variable to the instance
        objectA.setInstanceVariable("foo", 14);

        // That should have changed the layout of the class
        assertNotSame(layoutClassA, classA.getObjectLayoutForInstances());

        // If we notify the object, it should also change the layout of that
        objectA.updateLayout();
        assertNotSame(layoutObjectA, objectA.getObjectLayout());

        // We should be able to find that instance variable as a storage location in the class
        assertNotNull(classA.getObjectLayoutForInstances().findStorageLocation("foo"));

        // We should be able to read that value back out
        assertEquals(14, objectA.getInstanceVariable("foo"));
    }

    @Test
    public void testOverflowPrimitives() {
        final PythonContext context = new PythonContext();

        // Create a class and an instance
        final PythonClass classA = new PythonClass(context, null, "A");
        final PythonBasicObject objectA = new PythonBasicObject(classA);

        // Add many more Fixnums that we have space for primitives
        final int count = 100;

        for (int n = 0; n < count; n++) {
            objectA.setInstanceVariable("foo" + n, n);
        }

        // We should be able to read them back out
        for (int n = 0; n < count; n++) {
            assertEquals(n, objectA.getInstanceVariable("foo" + n));
        }
    }

    @Test
    public void testGeneralisation() {
        final PythonContext context = new PythonContext();

        // Create a class and two instances
        final PythonClass classA = new PythonClass(context, null, "A");
        final PythonBasicObject object1 = new PythonBasicObject(classA);
        final PythonBasicObject object2 = new PythonBasicObject(classA);

        // Set an instance variable to be a PInt in object 1
        object1.setInstanceVariable("foo", 14);

        // We should be able to read that instance variable back, and it should still be a PInt
        assertEquals(14, object1.getInstanceVariable("foo"));
        assertSame(Integer.class, object1.getInstanceVariable("foo").getClass());

        // The underlying instance store should be PInt
        assertSame(PIntStorageLocation.class, object1.getObjectLayout().findStorageLocation("foo").getClass());

        /*
         * The same instance variable in object 2 should be Nil. Note that this requires that we realize that even though the instance variable is known about in the layout of object 2, and we are using a primitive int to hold it, that it hasn't been
         * set and is actually None. We don't want it to appear as 0.
         */
        assertSame(PNone.NONE, object2.getInstanceVariable("foo"));

        /*
         * We should be able to set the same instance variable in object 2 to also be a PInt without changing the layout.
         */
        final ObjectLayout objectLayout2 = object2.getObjectLayout();
        object2.setInstanceVariable("foo", 2);
        assertEquals(2, object2.getInstanceVariable("foo"));
        assertSame(Integer.class, object2.getInstanceVariable("foo").getClass());
        assertSame(objectLayout2, object2.getObjectLayout());

        // Set the instance variable in object 2 to be a Float
        object2.setInstanceVariable("foo", 2.25);

        // We should be able to read that instance variable back, and it should still be a PInt
        assertEquals(2.25, object2.getInstanceVariable("foo"));
        assertSame(Double.class, object2.getInstanceVariable("foo").getClass());

        // Object 1 should give still think the instance variable is a PInt
        assertEquals(14, object1.getInstanceVariable("foo"));
        assertSame(Integer.class, object1.getInstanceVariable("foo").getClass());

        // The underlying instance store in both objects should now be Object
        assertSame(ObjectStorageLocation.class, object1.getObjectLayout().findStorageLocation("foo").getClass());
        assertSame(ObjectStorageLocation.class, object2.getObjectLayout().findStorageLocation("foo").getClass());
    }

    @Test
    public void testSubclasses() {
        final PythonContext context = new PythonContext();

        // Create two classes, A, and a subclass, B, and an instance of each

        final PythonClass classA = new PythonClass(context, null, "A");
        final PythonClass classB = new PythonClass(context, classA, "B");

        ObjectLayout layoutClassA = classA.getObjectLayoutForInstances();
        ObjectLayout layoutClassB = classA.getObjectLayoutForInstances();

        final PythonBasicObject objectA = new PythonBasicObject(classA);
        final PythonBasicObject objectB = new PythonBasicObject(classB);

        ObjectLayout layoutObjectA = objectA.getObjectLayout();
        ObjectLayout layoutObjectB = objectB.getObjectLayout();

        // Add an instance variable to the instance of A

        objectA.setInstanceVariable("foo", 14);

        // That should have changed the layout of both classes

        assertNotSame(layoutClassA, classA.getObjectLayoutForInstances());
        assertNotSame(layoutClassB, classB.getObjectLayoutForInstances());

        layoutClassA = classA.getObjectLayoutForInstances();
        layoutClassB = classA.getObjectLayoutForInstances();

        // If we notify the objects, both of them should have changed layouts

        objectA.updateLayout();
        objectB.updateLayout();
        assertNotSame(layoutObjectA, objectA.getObjectLayout());
        assertNotSame(layoutObjectB, objectB.getObjectLayout());

        layoutObjectA = objectA.getObjectLayout();
        layoutObjectB = objectB.getObjectLayout();

        // We should be able to find that instance variable as a storage location in both classes

        assertNotNull(classA.getObjectLayoutForInstances().findStorageLocation("foo"));
        assertNotNull(classB.getObjectLayoutForInstances().findStorageLocation("foo"));

        // We should be able to read that value back out

        assertEquals(14, objectA.getInstanceVariable("foo"));

        // Add an instance variable to the instance of B

        objectB.setInstanceVariable("bar", 2);

        // This should not have changed the layout of A or the instance of A

        assertSame(layoutClassA, classA.getObjectLayoutForInstances());
        assertSame(layoutObjectA, objectA.getObjectLayout());

        // But the layout of B and the instance of B should have changed

        assertNotSame(layoutClassB, classB.getObjectLayoutForInstances());

        objectB.updateLayout();
        assertNotSame(layoutObjectB, objectB.getObjectLayout());

        // We should be able to find the new instance variable in the instance of B but not A

        assertNull(classA.getObjectLayoutForInstances().findStorageLocation("bar"));
        assertNotNull(classB.getObjectLayoutForInstances().findStorageLocation("bar"));

        // We should be able to read that value back out

        assertEquals(2, objectB.getInstanceVariable("bar"));
    }

    /**
     * This is simplified.
     */
    @Test
    public void testPerObjectInstanceVariables() {
        final PythonContext context = new PythonContext();

        // Create a class and an instance
        final PythonClass classA = new PythonClass(context, null, "A");
        final PythonBasicObject objectA = new PythonBasicObject(classA);

        ObjectLayout layoutClassA = classA.getObjectLayoutForInstances();
        ObjectLayout layoutObjectA = objectA.getObjectLayout();

        // Add an instance variable to the instance of A
        objectA.setInstanceVariable("foo", 2);

        // That should have changed the layout of the class and the object
        assertNotSame(layoutClassA, classA.getObjectLayoutForInstances());
        assertNotSame(layoutObjectA, objectA.getObjectLayout());
        layoutClassA = classA.getObjectLayoutForInstances();
        layoutObjectA = classA.getObjectLayout();

        // We should be able to read the value back out
        assertEquals(2, objectA.getInstanceVariable("foo"));

        /*
         * We should also be able to read the first variable back out, even though we've switched to private layout since then.
         */
        assertEquals(2, objectA.getInstanceVariable("foo"));
    }
}
