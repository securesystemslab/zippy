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
richards3.py 3
binarytrees3.py 12
fannkuchredux3.py 9
pidigits.py 0
bm-ai.py 0
meteor3.py 2098
spectralnorm3.py 500
mandelbrot3.py 600
pypy-go.py 1
fasta3.py 250000
nbody3.py 100000
*/
public class TestBenchmarks {
    @Test
    public void richards3() {
        Path script = Paths.get("richards3.py");
        assertBenchNoError(script, "3");
        }

    @Test
    public void binarytrees3() {
        Path script = Paths.get("binarytrees3.py");
        assertBenchNoError(script, "12");
        }

    @Test
    public void fannkuchredux3() {
        Path script = Paths.get("fannkuchredux3.py");
        assertBenchNoError(script, "9");
        }

    @Test
    public void pidigits() {
        Path script = Paths.get("pidigits.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void bm_ai() {
        Path script = Paths.get("bm-ai.py");
        assertBenchNoError(script, "0");
        }

    @Test
    public void meteor3() {
        Path script = Paths.get("meteor3.py");
        assertBenchNoError(script, "2098");
        }

    @Test
    public void spectralnorm3() {
        Path script = Paths.get("spectralnorm3.py");
        assertBenchNoError(script, "500");
        }

    @Test
    public void mandelbrot3() {
        Path script = Paths.get("mandelbrot3.py");
        assertBenchNoError(script, "600");
        }

    @Test
    public void pypy_go() {
        Path script = Paths.get("pypy-go.py");
        assertBenchNoError(script, "1");
        }

    @Test
    public void fasta3() {
        Path script = Paths.get("fasta3.py");
        assertBenchNoError(script, "250000");
        }

    @Test
    public void nbody3() {
        Path script = Paths.get("nbody3.py");
        assertBenchNoError(script, "100000");
        }

}

