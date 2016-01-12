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
pypy-go-timed.py 50
pypy-chaos-timed.py 1000
bm-float-timed.py 1000
richards3-timed.py 200
pypy-deltablue.py 2000
*/
public class ObjectBenchmarks {
    @Test
    public void pypy_go_timed() {
        Path script = Paths.get("pypy-go-timed.py");
        assertBenchNoError(script, "50");
        }

    @Test
    public void pypy_chaos_timed() {
        Path script = Paths.get("pypy-chaos-timed.py");
        assertBenchNoError(script, "1000");
        }

    @Test
    public void bm_float_timed() {
        Path script = Paths.get("bm-float-timed.py");
        assertBenchNoError(script, "1000");
        }

    @Test
    public void richards3_timed() {
        Path script = Paths.get("richards3-timed.py");
        assertBenchNoError(script, "200");
        }

    @Test
    public void pypy_deltablue() {
        Path script = Paths.get("pypy-deltablue.py");
        assertBenchNoError(script, "2000");
        }

}

