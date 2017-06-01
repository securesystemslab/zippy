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

import java.io.OutputStream;
import java.io.PrintStream;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import edu.uci.python.PythonLanguage;

public class PythonOptions {

    protected static final String propPkgName = PythonLanguage.class.getPackage().getName();
    // Debug flags
    public static boolean PrintAST = Boolean.getBoolean(propPkgName + ".PrintAST"); // false

    public static boolean VisualizedAST = Boolean.getBoolean(propPkgName + ".VisualizedAST"); // false

    public static String PrintASTFilter = System.getProperty(propPkgName + ".PrintASTFilter"); // null

    public static boolean TraceJythonRuntime = Boolean.getBoolean(propPkgName + ".TraceJythonRuntime"); // false

    public static boolean TraceImports = Boolean.getBoolean(propPkgName + ".TraceImports"); // false

    public static boolean TraceSequenceStorageGeneralization = Boolean.getBoolean(propPkgName + ".TraceSequenceStorageGeneralization"); // false

    public static boolean TraceObjectLayoutCreation = Boolean.getBoolean(propPkgName + ".TraceObjectLayoutCreation"); // false

    // Object storage allocation
    public static boolean InstrumentObjectStorageAllocation = Boolean.getBoolean(propPkgName + ".InstrumentObjectStorageAllocation"); // false

    // Translation flags
    public static boolean UsePrintFunction = Boolean.getBoolean(propPkgName + ".UsePrintFunction"); // false

    // Runtime flags
    public static boolean UnboxSequenceStorage = !Boolean.getBoolean(propPkgName + ".disableUnboxSequenceStorage"); // true

    public static boolean UnboxSequenceIteration = !Boolean.getBoolean(propPkgName + ".disableUnboxSequenceIteration"); // true

    public static boolean IntrinsifyBuiltinCalls = !Boolean.getBoolean(propPkgName + ".disableIntrinsifyBuiltinCalls"); // true

    public static final int AttributeAccessInlineCacheMaxDepth = 20;

    public static final int CallSiteInlineCacheMaxDepth = 20;

    public static boolean FlexibleObjectStorageEvolution = Boolean.getBoolean(propPkgName + ".FlexibleObjectStorageEvolution"); // false

    public static boolean FlexibleObjectStorage = Boolean.getBoolean(propPkgName + ".FlexibleObjectStorage"); // false

    // Generators
    public static boolean InlineGeneratorCalls = !Boolean.getBoolean(propPkgName + ".disableInlineGeneratorCalls"); // true

    public static boolean OptimizeGeneratorExpressions = !Boolean.getBoolean(propPkgName + ".disableOptimizeGeneratorExpressions"); // true

    public static boolean TraceGeneratorInlining = Boolean.getBoolean(propPkgName + ".TraceGeneratorInlining"); // false

    public static boolean TraceNodesWithoutSourceSection = Boolean.getBoolean(propPkgName + ".TraceNodesWithoutSourceSection"); // false

    public static boolean TraceNodesUsingExistingProbe = Boolean.getBoolean(propPkgName + ".TraceNodesUsingExistingProbe"); // false

    public static boolean CatchZippyExceptionForUnitTesting = Boolean.getBoolean(propPkgName + ".CatchZippyExceptionForUnitTesting"); // false

    public static boolean forceLongType = Boolean.getBoolean(propPkgName + ".forceLongType"); // false

    private OutputStream standardOut = System.out;

    private OutputStream standardErr = System.err;

    public PythonOptions() {
        standardOut = System.out;
        standardErr = System.err;
    }

    public PythonOptions(PrintStream standardOut, PrintStream standardErr) {
        this();
        if (standardOut != null)
            this.standardOut = standardOut;

        if (standardErr != null)
            this.standardErr = standardErr;
    }

    public void setStandardOut(OutputStream stdout) {
        standardOut = stdout;
    }

    public OutputStream getStandardOut() {
        return standardOut;
    }

    public void setStandardErr(OutputStream stderr) {
        standardErr = stderr;
    }

    public OutputStream getStandardErr() {
        return standardErr;
    }

    @TruffleBoundary
    public static void setEnvOptions(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.setProperty(propPkgName + "." + options[i], "true");
        }
    }

}
