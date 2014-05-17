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

import static edu.uci.python.test.PythonTests.*;

import java.nio.file.*;

import org.junit.*;

import edu.uci.python.runtime.*;

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
    public void conditionAndLoop() {
        Path script = Paths.get("generator-if-and-loop-test.py");
        assertPrints("10\n0\n1\n2\n3\n4\n", script);
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
    public void euler11() {
        PythonOptions.OptimizeGeneratorExpressions = false;
        Path script = Paths.get("euler11-test.py");
        assertPrints("9507960\n9507960\n", script);
    }

    @Test
    public void inline() {
        PythonOptions.InlineGeneratorCalls = true;
        Path script = Paths.get("generator-inline-test.py");
        assertPrints("99\n99\n99\n99\n99\n", script);
    }

    @Test
    public void inlineNone() {
        PythonOptions.InlineGeneratorCalls = true;
        Path script = Paths.get("generator-inline-none-test.py");
        assertPrints("99\n99\n99\n99\n99\n", script);
    }

    @Test
    public void objectsInList() {
        Path script = Paths.get("generator-objects-test.py");
        assertPrints("1\n2\n10\n11\n", script);
    }

}
