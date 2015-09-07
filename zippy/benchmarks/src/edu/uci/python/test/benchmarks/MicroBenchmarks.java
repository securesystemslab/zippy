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
package edu.uci.python.test.benchmarks;

import static edu.uci.python.test.PythonTests.*;

import java.nio.file.*;

import org.junit.*;

/*
object-allocate.py 0
attribute-access.py 0
generator-notaligned.py 0
arith-binop.py 0
special-len.py 0
function-call.py 0
attribute-access-polymorphic.py 0
object-layout-change.py 0
list-iterating.py 0
generator.py 0
for-range.py 0
list-comp.py 0
attribute-bool.py 0
math-sqrt.py 0
boolean-logic.py 0
generator-expression.py 0
builtin-len.py 0
builtin-len-tuple.py 0
call-method-polymorphic.py 0
special-add-int.py 0
genexp-builtin-call.py 0
list-indexing.py 0
special-add.py 0
*/
public class MicroBenchmarks {
    @Test
    public void object_allocate() {
        Path script = Paths.get("object-allocate.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void attribute_access() {
        Path script = Paths.get("attribute-access.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void generator_notaligned() {
        Path script = Paths.get("generator-notaligned.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void arith_binop() {
        Path script = Paths.get("arith-binop.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void special_len() {
        Path script = Paths.get("special-len.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void function_call() {
        Path script = Paths.get("function-call.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void attribute_access_polymorphic() {
        Path script = Paths.get("attribute-access-polymorphic.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void object_layout_change() {
        Path script = Paths.get("object-layout-change.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void list_iterating() {
        Path script = Paths.get("list-iterating.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void generator() {
        Path script = Paths.get("generator.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void for_range() {
        Path script = Paths.get("for-range.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void list_comp() {
        Path script = Paths.get("list-comp.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void attribute_bool() {
        Path script = Paths.get("attribute-bool.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void math_sqrt() {
        Path script = Paths.get("math-sqrt.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void boolean_logic() {
        Path script = Paths.get("boolean-logic.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void generator_expression() {
        Path script = Paths.get("generator-expression.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void builtin_len() {
        Path script = Paths.get("builtin-len.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void builtin_len_tuple() {
        Path script = Paths.get("builtin-len-tuple.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void call_method_polymorphic() {
        Path script = Paths.get("call-method-polymorphic.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void special_add_int() {
        Path script = Paths.get("special-add-int.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void genexp_builtin_call() {
        Path script = Paths.get("genexp-builtin-call.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void list_indexing() {
        Path script = Paths.get("list-indexing.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void special_add() {
        Path script = Paths.get("special-add.py");
        assertBenchNoError(script, "0");
        }

}

