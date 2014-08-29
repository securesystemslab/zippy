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
package edu.uci.python.shell;

import edu.uci.python.runtime.*;

public class CommandLineParser {

    public static void parse(String[] args) {
        int index = 0;

        while (index < args.length) {
            String arg = args[index++];

            if (!arg.startsWith("-")) {
                continue;
            }

            if (arg.equals("-print-ast")) {
                PythonOptions.PrintAST = true;
                continue;
            }

            if (arg.equals("-visualize-ast")) {
                PythonOptions.VisualizedAST = true;
                continue;
            }

            if (arg.equals("-print-function")) {
                PythonOptions.UsePrintFunction = true;
                continue;
            }

            if (arg.equals("-inline-generator")) {
                PythonOptions.InlineGeneratorCalls = true;
                continue;
            }

            if (arg.equals("-optimize-genexp")) {
                PythonOptions.OptimizeGeneratorExpressions = true;
                continue;
            }

            if (arg.equals("-no-generator-peeling")) {
                PythonOptions.InlineGeneratorCalls = false;
                PythonOptions.OptimizeGeneratorExpressions = false;
                continue;
            }

            if (arg.equals("-profile")) {
                PythonOptions.ProfileCalls = true;
                PythonOptions.ProfileControlFlow = true;
                PythonOptions.ProfileReadsWrites = true;
                PythonOptions.ProfileOperations = true;
                PythonOptions.ProfileAttributesElements = true;
                PythonOptions.SortProfilerResults = true;
                continue;
            }

            if (arg.equals("-profile-calls")) {
                PythonOptions.ProfileCalls = true;
                continue;
            }

            if (arg.equals("-profile-control-flow")) {
                PythonOptions.ProfileControlFlow = true;
                continue;
            }

            if (arg.equals("-profile-reads-writes")) {
                PythonOptions.ProfileReadsWrites = true;
                continue;
            }

            if (arg.equals("-profile-operations")) {
                PythonOptions.ProfileOperations = true;
                continue;
            }

            if (arg.equals("-profile-attributes-elements")) {
                PythonOptions.ProfileAttributesElements = true;
                continue;
            }

            if (arg.equals("-profile-type-distribution")) {
                PythonOptions.ProfileTypeDistribution = true;
                PythonOptions.ProfileReadsWrites = true;
                PythonOptions.ProfileOperations = true;
                PythonOptions.ProfileAttributesElements = true;
                continue;
            }

            if (arg.equals("-sort")) {
                PythonOptions.SortProfilerResults = true;
                continue;
            }

            throw new IllegalStateException("Unknown options " + arg);
        }

    }
}
