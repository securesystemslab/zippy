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

import java.util.*;

public class TestPrograms {
    public static void helloWorldTest() {
        System.out.println("Hello world!");
    }

    public static void formattedOutputTest() {
        System.out.printf("%s %s!", "Hello", "world");
    }

    @SuppressWarnings("unused")
    public static void newTest() {
        Integer x = new Integer(5);
    }

    public static void arraycopyTest() {
        Object[] arr = new Object[1];
        arr[0] = new TestObject();
        TestObject[] newArr = Arrays.copyOf(arr, 1, TestObject[].class);
        newArr[0].testMethod();
    }

    @SuppressWarnings("unchecked")
    public static void arrayListTest() {
        ArrayList list = new ArrayList();
        list.add(new TestObject());
        TestObject[] newArr = (TestObject[]) list.toArray(new TestObject[0]);
        newArr[0].testMethod();
    }

    public static class TestObject {
        public void testMethod() {
        }
    }
}
