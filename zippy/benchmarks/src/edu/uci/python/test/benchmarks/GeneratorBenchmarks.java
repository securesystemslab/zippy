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
python-graph-bench.py 200
pads-eratosthenes-timed.py 100000
pads-lyndon.py 100000000
ai-nqueen-timed.py 10
sympy-bench.py 20000
pymaging-bench.py 5000
euler11-timed.py 10000
pads-integerpartitions.py 700
simplejson-bench.py 10000
euler31-timed.py 200
whoosh-bench.py 5000
*/
public class GeneratorBenchmarks {
    @Test
    public void python_graph_bench() {
        Path script = Paths.get("python-graph-bench.py");
        assertBenchNoError(script, "200");
        }

    @Test
    public void pads_eratosthenes_timed() {
        Path script = Paths.get("pads-eratosthenes-timed.py");
        assertBenchNoError(script, "100000");
        }

    @Test
    public void pads_lyndon() {
        Path script = Paths.get("pads-lyndon.py");
        assertBenchNoError(script, "100000000");
        }

    @Test
    public void ai_nqueen_timed() {
        Path script = Paths.get("ai-nqueen-timed.py");
        assertBenchNoError(script, "10");
        }

    @Test
    public void sympy_bench() {
        Path script = Paths.get("sympy-bench.py");
        assertBenchNoError(script, "20000");
        }

    @Test
    public void pymaging_bench() {
        Path script = Paths.get("pymaging-bench.py");
        assertBenchNoError(script, "5000");
        }

    @Test
    public void euler11_timed() {
        Path script = Paths.get("euler11-timed.py");
        assertBenchNoError(script, "10000");
        }

    @Test
    public void pads_integerpartitions() {
        Path script = Paths.get("pads-integerpartitions.py");
        assertBenchNoError(script, "700");
        }

    @Test
    public void simplejson_bench() {
        Path script = Paths.get("simplejson-bench.py");
        assertBenchNoError(script, "10000");
        }

    @Test
    public void euler31_timed() {
        Path script = Paths.get("euler31-timed.py");
        assertBenchNoError(script, "200");
        }

    @Test
    public void whoosh_bench() {
        Path script = Paths.get("whoosh-bench.py");
        assertBenchNoError(script, "5000");
        }

}

