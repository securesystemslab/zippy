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

import org.junit.*;

public class BigBangTest {

    @Test
    public void helloWorldTest() {
        BootImageGenerator generator = new BootImageGenerator();
        generator.addEntryMethod(TestPrograms.class, "helloWorldTest");
        Assert.assertArrayEquals(generator.getBigBang().printState(), new int[]{3, 148, 66, 24});
    }

    @Test
    public void formattedOutputTest() {
        BootImageGenerator generator = new BootImageGenerator();
        generator.addEntryMethod(TestPrograms.class, "formattedOutputTest");
        Assert.assertArrayEquals(generator.getBigBang().printState(), new int[]{15, 979, 346, 98});
    }


    @Test
    public void newTest() {
        BootImageGenerator generator = new BootImageGenerator();
        generator.addEntryMethod(TestPrograms.class, "newTest");
        Assert.assertArrayEquals(generator.getBigBang().printState(), new int[]{0, 3, 0, 0});
    }


    @Test
    public void arraycopyTest() {
        BootImageGenerator generator = new BootImageGenerator();
        generator.addEntryMethod(TestPrograms.class, "arraycopyTest");
        Assert.assertArrayEquals(generator.getBigBang().printState(), new int[]{2, 6, 0, 0});
    }

    @Test
    public void arrayListTest() {
        BootImageGenerator generator = new BootImageGenerator();
        generator.addEntryMethod(TestPrograms.class, "arrayListTest");
        Assert.assertArrayEquals(generator.getBigBang().printState(), new int[]{2, 20, 3, 2});
    }

    @Test
    public void arrayListTestWithCalls() {
        BootImageGenerator generator = new BootImageGenerator();
        generator.addEntryMethod(TestPrograms.class, "arrayListTestWithCalls");
        Assert.assertArrayEquals(generator.getBigBang().printState(), new int[]{2, 20, 3, 2});
    }
}
