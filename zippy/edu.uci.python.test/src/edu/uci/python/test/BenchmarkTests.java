/*
 * Copyright (c) 2014, Regents of the University of California
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

public class BenchmarkTests {

    @Test
    public void euler31() {
        PythonOptions.OptimizeGeneratorExpressions = false;
        Path script = Paths.get("euler31-test.py");
        assertPrints("41\n", script);
    }

    @Test
    public void bisectRight() {
        Path script = Paths.get("bisect-right-test.py");
        assertPrints("3\n3\n3\n", script);
    }

    @Test
    public void richards3() {
        Path script = Paths.get("richards3.py");
        assertBenchNoError(script, "3");
    }

    @Test
    public void bm_ai() {
        Path script = Paths.get("bm-ai.py");
        assertBenchNoError(script, "0");
    }

    @Ignore
    @Test
    public void mandelbrot3_test() {
        Path script = Paths.get("mandelbrot3_test.py");
        assertPrints("byte_acc_inc = 465\n" + //
                        "       *  \n" + //
                        "          \n" + //
                        "     ** * \n" + //
                        "     ** **\n" + //
                        "  ***** **\n" + //
                        "******* * \n" + //
                        "  ***** **\n" + //
                        "     ** **\n" + //
                        "     ** * \n" + //
                        "          \n" + //
                        "\n" + //
                        "", script);
    }

    @Test
    public void mandelbrot3_300() {
        Path script = Paths.get("mandelbrot3.py");
        assertBenchNoError(script, "300");
    }

    @Test
    public void nbody_acc_test() {
        Path script = Paths.get("nbody_acc_test.py");
        assertPrints("[[-22.62210194193863, 9.618242744937728], " + //
                        "[13.263051063854693, 0.8260066918954928], " + //
                        "[25.120556640717176, -3.100621794584926], " + //
                        "[11.53806350789511, -3.5463051095119624], " + //
                        "[1.500557931736798, -24.709058028451864], " + //
                        "[-49.42915670227889, 8.651936265335848], " + //
                        "[-20.470135708339853, 15.86674631665035], " + //
                        "[-55.89760749787459, 66.65201058516145], " + //
                        "[-39.518026097616925, 48.26310880217063], " + //
                        "[95.36866152288519, -103.54666946538075], " + //
                        "[11.358044456308408, 21.5330532382369], " + //
                        "[9.325062088265275, -14.679387967839677], " + //
                        "[-0.4156364149293482, -78.34797412793763], " + //
                        "[20.425929446025247, 25.609388671743652], " + //
                        "[-7.203572107433892, -26.692863942051243], " + //
                        "[17.390387793063407, 40.63086695509797]]\n", script);

    }

    @Test
    public void blackscholes_test() {
        Path script = Paths.get("blackscholes_test.py");
        assertPrints("callResultGold [1.396983862573739E-9, 0.0, " + //
                        "7.193200293552689E-4, 0.6719761796630808, " + //
                        "4.670179620697247, 0.0, 0.006353603213258834, " + //
                        "2.7463027009490424]\n" + //
                        "putResultGold [83.16933913397106, 37.170898228963324, " + //
                        "62.33647515966393, 48.02061790368548, 1.9917883994019536, " + //
                        "76.54358491978775, 56.56440552070942, 18.991943836674068]\n", script);

    }

}
