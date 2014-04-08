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
package edu.uci.python.test.datatype;

import static edu.uci.python.test.PythonTests.*;

import org.junit.*;

public class ListComprehensionTests {

    @Test
    public void simple() {
        String source = "llist = [x*2 for x in range(5)]\n" + //
                        "print(llist)\n";

        assertPrints("[0, 2, 4, 6, 8]\n", source);
    }

    @Test
    public void doubleLoop() {
        String source = "llist = [x+y for x in range(5) for y in range(3)]\n" + //
                        "print(llist)\n";

        assertPrints("[0, 1, 2, 1, 2, 3, 2, 3, 4, 3, 4, 5, 4, 5, 6]\n", source);
    }

    @Test
    public void nestedListComp() {
        String source = "llist = [[x for x in range(5)] for y in range(3)]\n" + //
                        "print(llist)\n";

        assertPrints("[[0, 1, 2, 3, 4], [0, 1, 2, 3, 4], [0, 1, 2, 3, 4]]\n", source);
    }

    @Test
    public void simpleWithLocalTarget() {
        String source = "def foo():\n" + //
                        "    return [x*2 for x in range(5)]\n" + //
                        "print(foo())\n";

        assertPrints("[0, 2, 4, 6, 8]\n", source);
    }

    @Test
    public void doubleLoopWithLocalTarget() {
        String source = "def foo():" + //
                        "    return [x+y for x in range(5) for y in range(3)]\n" + //
                        "print(foo())\n";

        assertPrints("[0, 1, 2, 1, 2, 3, 2, 3, 4, 3, 4, 5, 4, 5, 6]\n", source);
    }

}
