/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.boot;

import java.lang.reflect.*;

import org.junit.*;


public class BootImageClassLoaderTest {

    @Test
    public void test() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, SecurityException {

        TestClassB.x = 1;
        BootImageClassLoader l = new BootImageClassLoader();

        // Assert that the class definition is really duplicated.
        Class<?> bClass = Class.forName(TestClassB.class.getCanonicalName(), true, l);
        Assert.assertNotSame(TestClassB.class, bClass);

        // Assert that the class definition is not duplicated more than once.
        Assert.assertSame(bClass, l.convert(TestClassB.class));

        // Set field x such that it is used by the subsequent static initializer for TestClassA.
        Field bField = bClass.getFields()[0];
        bField.setAccessible(true);
        bField.set(null, 2);

        // Assert that the class definition is duplicated.
        Class<?> aClass = l.convert(TestClassA.class);
        Assert.assertNotSame(TestClassA.class, aClass);

        // Assert that the original version of TestClassA was initialized correctly.
        Assert.assertEquals(1, TestClassA.x);

        // Assert that the duplicated version of TestClassA was initialized correctly.
        Field aField = aClass.getFields()[0];
        aField.setAccessible(true);
        Assert.assertEquals(2, aField.getInt(null));

        // Assert that system classes are not duplicated.
        Assert.assertSame(Object.class, l.convert(Object.class));
        Assert.assertSame(Object.class, Class.forName(Object.class.getCanonicalName(), true, l));
    }

}

class TestClassA {
    public static int x;

    static {
        x = TestClassB.x;
    }
}

class TestClassB {
    public static int x;
}
