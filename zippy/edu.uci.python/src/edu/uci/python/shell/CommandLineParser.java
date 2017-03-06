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

import java.util.ArrayList;

import edu.uci.python.runtime.PythonOptions;

public class CommandLineParser {

    public static String[] parse(String[] args) {
        int index = 0;
        ArrayList<String> pythonArgs = new ArrayList<>();

        while (index < args.length) {
            String arg = args[index++];

            if (!arg.startsWith("-")) {
                pythonArgs.add(arg);
                continue;
            }

            if (arg.equals("-print-ast")) {
                PythonOptions.setOptions(new String[]{"PrintAST"}, new boolean[]{true});

                continue;
            }

            if (arg.equals("-visualize-ast")) {
                PythonOptions.setOptions(new String[]{"VisualizedAST"}, new boolean[]{true});

                continue;
            }

            if (arg.equals("-print-function")) {
                PythonOptions.setOptions(new String[]{"UsePrintFunction"}, new boolean[]{true});

                continue;
            }

            if (arg.equals("-flexible-object-storage")) {
                PythonOptions.setOptions(new String[]{"FlexibleObjectStorage"}, new boolean[]{true});

                continue;
            }

            if (arg.equals("-flexible-storage-evolution")) {
                PythonOptions.setOptions(new String[]{"FlexibleObjectStorage", "FlexibleObjectStorageEvolution"}, new boolean[]{true, true});

                continue;
            }

            if (arg.equals("-inline-generator")) {
                PythonOptions.setOptions(new String[]{"InlineGeneratorCalls"}, new boolean[]{true});

                continue;
            }

            if (arg.equals("-optimize-genexp")) {
                PythonOptions.setOptions(new String[]{"OptimizeGeneratorExpressions"}, new boolean[]{true});

                continue;
            }

            if (arg.equals("-no-generator-peeling")) {
                PythonOptions.setOptions(new String[]{"InlineGeneratorCalls", "OptimizeGeneratorExpressions"}, new boolean[]{false, false});

                continue;
            }

            pythonArgs.add(arg);
        }

        return pythonArgs.toArray(new String[pythonArgs.size()]);
    }
}
