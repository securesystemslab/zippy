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
package edu.uci.python.test.grammar;

import static edu.uci.python.test.PythonTests.*;

import org.junit.*;

public class LambdaTests {

    @Test
    public void lambdaTest() {
        String source = "def make_incrementor (n):\n" + //
                        "    return lambda x: x + n\n" + //
                        "f = make_incrementor(2)\n" + //
                        "g = make_incrementor(6)\n" + //
                        "print(f(42))\n" + //
                        "print(g(42))\n" + //
                        "print(make_incrementor(22)(33))\n";
        assertPrints("44\n48\n55\n", source);
    }

    @Test
    public void mapLambdaTest() {
        String source = "n = 5\n" + //
                        "print(list(map(lambda x:x*2, [i for i in range(10)])))\n" + //
                        "print(map(lambda x:x*2 + n, [i for i in range(10)]))\n";
        assertPrints("[0, 2, 4, 6, 8, 10, 12, 14, 16, 18]\n[5, 7, 9, 11, 13, 15, 17, 19, 21, 23]\n", source);
    }

    @Test
    public void mapReduceLambdaTest() {
        String source = "from functools import reduce\n" + //
                        "print(reduce(lambda x, y: x + y, list(map(lambda x:x*2, [i for i in range(10)]))))\n" + //
                        "print(reduce(lambda x, y: x * y + 1, map(lambda x:x*2, [i for i in range(10)])))\n" + //
                        "print(reduce(lambda x, y: x * y + 1, map(lambda x:x*2, [i for i in range(10)]), 5))\n";
        assertPrints("90\n120528883\n306323443\n", source);
    }

}
