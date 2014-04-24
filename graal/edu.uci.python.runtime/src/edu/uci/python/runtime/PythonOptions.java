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
package edu.uci.python.runtime;

import java.io.*;

public class PythonOptions {

    // Debug flags
    public static boolean PrintAST = false;

    public static boolean VisualizedAST = false;

    public static String PrintASTFilter = null;

    public static boolean TraceJythonRuntime = false;

    public static boolean TraceImports = false;

    // Translation flags
    public static boolean PrintFunction = false;

    // Runtime flags
    public static final boolean UseUnsafe = true;

    public static final boolean InlineFunctionCalls = true;

    public static boolean InlineBuiltinFunctionCalls = true;

    public static final boolean AttributeAccessInlineCaching = true;

    public static final boolean UnboxSequenceStorage = true;

    public static final boolean UnboxSequenceIteration = true;

    public static final boolean IntrinsifyBuiltinCalls = true;

    public static final int AttributeAccessInlineCacheMaxDepth = 5;

    public static final int CallSiteInlineCacheMaxDepth = 5;

    // Generators
    public static boolean InlineGeneratorCalls = false;

    public static boolean OptimizeGeneratorExpressions = false;

    // Parallel Generators
    public static final boolean ParallelizeGeneratorCalls = false;

    public static final boolean ProfileGeneratorCalls = false;

    static {
        if (ParallelizeGeneratorCalls) {
            InlineGeneratorCalls = false;
            OptimizeGeneratorExpressions = false;
        }
    }

    public static final boolean CatchZippyExceptionForUnitTesting = false;

    private PrintStream standardOut = System.out;

    private PrintStream standardErr = System.err;

    public void setStandardOut(PrintStream stdout) {
        standardOut = stdout;
    }

    public PrintStream getStandardOut() {
        return standardOut;
    }

    public void setStandardErr(PrintStream stderr) {
        standardErr = stderr;
    }

    public PrintStream getStandardErr() {
        return standardErr;
    }

}
