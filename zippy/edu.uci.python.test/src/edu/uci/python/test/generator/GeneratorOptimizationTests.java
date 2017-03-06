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

import static edu.uci.python.test.PythonTests.assertPrintContains;
import static edu.uci.python.test.PythonTests.assertPrints;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;

import edu.uci.python.nodes.generator.ComprehensionNode.ListComprehensionNode;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.PythonParseResult;

public class GeneratorOptimizationTests {

    @Test
    public void euler11() {
        String[] options = {"OptimizeGeneratorExpressions"};
        boolean[] values = {false};
        PythonOptions.setOptions(options, values);
        Path script = Paths.get("euler11-test.py");
        assertPrints("9507960\n9507960\n", script);
    }

    @Test
    public void inline() {
        String[] options = {"InlineGeneratorCalls"};
        boolean[] values = {true};
        PythonOptions.setOptions(options, values);
        Path script = Paths.get("generator-inline-test.py");
        assertPrints("99\n99\n99\n99\n99\n", script);
    }

    @Test
    public void inlineNone() {
        String[] options = {"InlineGeneratorCalls"};
        boolean[] values = {true};
        PythonOptions.setOptions(options, values);
        Path script = Paths.get("generator-inline-none-test.py");
        assertPrints("99\n99\n99\n99\n99\n", script);
    }

    @Test
    public void inlineGenexp() {
        String[] options = {"InlineGeneratorCalls", "OptimizeGeneratorExpressions"};
        boolean[] values = {true, true};
        PythonOptions.setOptions(options, values);
        Path script = Paths.get("generator-inline-genexp-test.py");
        assertPrintContains("420\n", script);
    }

    @Test
    public void inlineGenexpLocalVar() {
        String[] options = {"InlineGeneratorCalls", "OptimizeGeneratorExpressions"};
        boolean[] values = {true, true};
        PythonOptions.setOptions(options, values);
        Path script = Paths.get("generator-inline-genexp-localvar-test.py");
        assertPrintContains("420\n", script);
    }

    @Test
    public void inlineGenexpBuiltinCall() {
        String[] options = {"IntrinsifyBuiltinCalls", "InlineGeneratorCalls", "OptimizeGeneratorExpressions"};
        boolean[] values = {true, true, true};
        PythonOptions.setOptions(options, values);

        assertTrue(PythonOptions.IntrinsifyBuiltinCalls);
        Path script = Paths.get("generator-inline-genexp-builtin-test.py");
        PythonParseResult ast = assertPrintContains("420\n", script);
// Node listComp = NodeUtil.findFirstNodeInstance(ast.getFunctionRoot("call_generator_builtin"),
// ListComprehensionNode.class);
        Node listComp = NodeUtil.findAllNodeInstances(ast.getFunctionRoot("call_generator_builtin"),
                        ListComprehensionNode.class).get(0);
        assertTrue(listComp != null);
    }

    @Test
    public void getItem() {
        Path script = Paths.get("generator-special-getitem-test.py");
        assertPrints("5\n", script);
    }

    @Test
    public void polymorphic() {
        Path script = Paths.get("generator-inline-polymorphic-test.py");
        assertPrints("10\n10\n10\n10\n10\n", script);
    }

}
