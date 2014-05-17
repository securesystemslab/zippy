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

import org.junit.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.*;
import static edu.uci.python.test.PythonTests.*;
import static org.junit.Assert.*;

public class GeneratorExpressionTranslationTests {

    @Test
    public void generatorExpressionAsIterator() {
        PythonOptions.OptimizeGeneratorExpressions = true;

        String source = "def foo():\n" + //
                        "    n = 5\n" + //
                        "    for i in (x for x in range(n)):\n" + //
                        "        item = i\n";

        PythonParseResult parsed = getParseResult(source);
        RootNode root = parsed.getFunctionRoot("foo");
        GeneratorExpressionNode genexp = NodeUtil.findFirstNodeInstance(root, GeneratorExpressionNode.class);
        assertTrue(genexp == null || genexp.isOptimized());
    }

    @Test
    public void generatorExpressionAsArgumentToConstructor() {
        PythonOptions.OptimizeGeneratorExpressions = true;

        String source = "def foo():\n" + //
                        "    n = 5\n" + //
                        "    return list(x for x in range(n))\n";

        String result = parseTest(source);
        assertTrue(result.contains("does not escape"));
    }

    @Test
    public void assignedToLocalVar() {
        PythonOptions.OptimizeGeneratorExpressions = true;

        String source = "def foo():\n" + //
                        "    n = 5\n" + //
                        "    ll = (x for x in range(n))\n" + //
                        "    return list(ll)\n";

        String result = parseTest(source);
        assertTrue(result.contains("does not escape"));
    }

    @Test
    public void escapeByReturn() {
        PythonOptions.OptimizeGeneratorExpressions = true;

        String source = "def foo():\n" + //
                        "    n = 5\n" + //
                        "    ll = (x for x in range(n))\n" + //
                        "    return ll\n";

        String result = parseTest(source);
        assertTrue(result.contains("escapes"));
    }

    @Test
    public void escapeByStore() {
        PythonOptions.OptimizeGeneratorExpressions = true;

        String source = "LIST = []\n" + //
                        "def foo():\n" + //
                        "    n = 5\n" + //
                        "    ll = (x for x in range(n))\n" + //
                        "    LIST[0] = ll\n";

        String result = parseTest(source);
        assertTrue(result.contains("escapes"));
    }

    @Test
    public void escapeByCall() {
        PythonOptions.OptimizeGeneratorExpressions = true;
        String source = "LIST = []\n" + //
                        "def foo():\n" + //
                        "    n = 5\n" + //
                        "    ll = (x for x in range(n))\n" + //
                        "    LIST.append(ll)\n";

        String result = parseTest(source);
        assertTrue(result.contains("escapes"));
    }

}
