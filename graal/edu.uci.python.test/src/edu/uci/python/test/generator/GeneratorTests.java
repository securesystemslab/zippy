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
package edu.uci.python.test.generator;

import static edu.uci.python.test.PythonTests.*;

import java.nio.file.*;

import org.junit.*;

public class GeneratorTests {

    @Test
    public void simpleLoop() {
        String source = "def loopgen(n):\n" + //
                        "    for i in range(n):\n" + //
                        "        yield i\n" + //
                        "\n" + //
                        "for i in loopgen(5):\n" + //
                        "    print(i)\n";

        assertPrints("0\n1\n2\n3\n4\n", source);
    }

    @Test
    public void specialIter() {
        String source = "class Foo:\n" + //
                        "    def __init__(self, n):\n" + //
                        "        self.n = n\n" + //
                        "    def __iter__(self):\n" + //
                        "        return (i for i in range(self.n))\n" + //
                        "for i in Foo(5):\n" + //
                        "    print(i)\n";

        assertPrints("0\n1\n2\n3\n4\n", source);
    }

    @Test
    public void desugared() {
        String source = "def gen(n):\n" + //
                        "    for i in range(n):\n" + //
                        "        yield i\n" + //
                        "g = gen(5)\n" + //
                        "try:\n" + //
                        "    while True:\n" + //
                        "       print(g.__next__())\n" + //
                        "except StopIteration:\n" + //
                        "    pass\n";

        assertPrints("0\n1\n2\n3\n4\n", source);
    }

    @Test
    public void conditionAndLoop() {
        Path script = Paths.get("generator-if-and-loop-test.py");
        assertPrints("10\n0\n1\n2\n3\n4\n", script);
    }

    @Test
    public void loopWithContinue() {
        Path script = Paths.get("generator-continue-test.py");
        assertPrints("1\n2\n3\n!!\n4\n5\n", script);
    }

    @Test
    public void multipleYields() {
        Path script = Paths.get("generator-multiple-yield-test.py");
        assertPrints("1\n3\n2\n1\n", script);
    }

    @Test
    public void accumulator() {
        Path script = Paths.get("generator-accumulator-test.py");
        assertPrints("['w', 'c', 'g']\n['h', 'z']\n", script);
    }

    @Test
    public void partition() {
        Path script = Paths.get("generator-partition-test.py");
        assertPrints("[range(0, 1), range(1, 2), range(2, 3)]\n", script);
    }

    @Test
    public void objectsInList() {
        Path script = Paths.get("generator-objects-test.py");
        assertPrints("1\n2\n10\n11\n", script);
    }

    @Test
    public void yieldExpression() {
        Path script = Paths.get("generator-yield-expression-test.py");
        assertPrints("0\n1\n2\n3\n4\n", script);
    }

}
