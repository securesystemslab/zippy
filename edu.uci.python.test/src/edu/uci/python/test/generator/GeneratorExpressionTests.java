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

public class GeneratorExpressionTests {

    @Test
    public void simple() {
        String source = "genexp = (x*2 for x in range(5))\n" + //
                        "for i in genexp:\n" + //
                        "    print(i)\n" + //
                        "\n";

        assertPrints("0\n2\n4\n6\n8\n", source);
    }

    @Test
    public void withCondition() {
        String source = "genexp = (x*2 for x in range(5) if x != 2)\n" + //
                        "for i in genexp:\n" + //
                        "    print(i)\n" + //
                        "\n";

        assertPrints("0\n2\n6\n8\n", source);
    }

    @Test
    public void nested() {
        String source = "genexp = (x+y for x in range(5) for y in range(3))\n" + //
                        "for i in genexp:\n" + //
                        "    print(i)\n" + //
                        "\n";

        assertPrints("0\n1\n2\n1\n2\n3\n2\n3\n4\n3\n4\n5\n4\n5\n6\n", source);
    }

    @Test
    public void generatorWithListComp() {
        String source = "genexp = (x*2 for x in range(5))\n" + //
                        "listcomp = [y for y in genexp]\n" + //
                        "for i in listcomp:\n" + //
                        "    print(i)\n" + //
                        "\n";

        assertPrints("0\n2\n4\n6\n8\n", source);
    }

    @Test
    public void generatorexpTest() {
        Path script = Paths.get("generatorexp_test.py");
        assertPrints("[(0, 0), (0, 1), (0, 2), (0, 3), (1, 0), (1, 1), (1, 2), (1, 3), (2, 0), (2, 1), (2, 2), (2, 3)]\n", script);
    }

    @Test
    public void sumGenExp() {
        Path script = Paths.get("generator-expression-sum-test.py");
        assertPrints("20\n20\n", script);
    }

}
