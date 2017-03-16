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

import java.io.PrintStream;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import edu.uci.python.PythonLanguage;

public class PythonOptions {

    protected static final String propPkgName = PythonLanguage.class.getPackage().getName();
    // Debug flags
    public final boolean PrintAST; // false

    public final boolean VisualizedAST; // false

    public final String PrintASTFilter; // null

    public final boolean TraceJythonRuntime; // false

    public final boolean TraceImports; // false

    public final boolean TraceSequenceStorageGeneralization; // false

    public final boolean TraceObjectLayoutCreation; // false

    // Object storage allocation
    public final boolean InstrumentObjectStorageAllocation; // false

    // Translation flags
    public final boolean UsePrintFunction; // false

    // Runtime flags
    public final boolean UnboxSequenceStorage; // true

    public final boolean UnboxSequenceIteration; // true

    public final boolean IntrinsifyBuiltinCalls; // true

    public static final int AttributeAccessInlineCacheMaxDepth = 20;

    public static final int CallSiteInlineCacheMaxDepth = 20;

    public final boolean FlexibleObjectStorageEvolution; // false

    public final boolean FlexibleObjectStorage; // false

    // Generators
    public final boolean InlineGeneratorCalls; // true

    public final boolean OptimizeGeneratorExpressions; // true

    public final boolean TraceGeneratorInlining; // false

    public final boolean TraceNodesWithoutSourceSection; // false

    public final boolean TraceNodesUsingExistingProbe; // false

    public final boolean CatchZippyExceptionForUnitTesting; // false

    public final boolean forceLongType; // false

    private PrintStream standardOut = System.out;

    private PrintStream standardErr = System.err;

    public PythonOptions() {
        standardOut = System.out;
        standardErr = System.err;

        this.PrintAST = Boolean.getBoolean(propPkgName + ".PrintAST"); // false
        this.VisualizedAST = Boolean.getBoolean(propPkgName + ".VisualizedAST"); // false
        this.PrintASTFilter = System.getProperty(propPkgName + ".PrintASTFilter"); // null
        this.TraceJythonRuntime = Boolean.getBoolean(propPkgName + ".TraceJythonRuntime"); // false
        this.TraceImports = Boolean.getBoolean(propPkgName + ".TraceImports"); // false
        this.TraceSequenceStorageGeneralization = Boolean.getBoolean(propPkgName + ".TraceSequenceStorageGeneralization"); // false
        this.TraceObjectLayoutCreation = Boolean.getBoolean(propPkgName + ".TraceObjectLayoutCreation"); // false

        // Object storage allocation
        this.InstrumentObjectStorageAllocation = Boolean.getBoolean(propPkgName + ".InstrumentObjectStorageAllocation"); // false

        // Translation flags
        this.UsePrintFunction = Boolean.getBoolean(propPkgName + ".UsePrintFunction"); // false

        // Runtime flags
        this.UnboxSequenceStorage = !Boolean.getBoolean(propPkgName + ".disableUnboxSequenceStorage"); // true
        this.UnboxSequenceIteration = !Boolean.getBoolean(propPkgName + ".disableUnboxSequenceIteration"); // true
        this.IntrinsifyBuiltinCalls = !Boolean.getBoolean(propPkgName + ".disableIntrinsifyBuiltinCalls"); // true
        this.FlexibleObjectStorageEvolution = Boolean.getBoolean(propPkgName + ".FlexibleObjectStorageEvolution"); // false
        this.FlexibleObjectStorage = Boolean.getBoolean(propPkgName + ".FlexibleObjectStorage"); // false

        // Generators
        this.InlineGeneratorCalls = !Boolean.getBoolean(propPkgName + ".disableInlineGeneratorCalls"); // true
        this.OptimizeGeneratorExpressions = !Boolean.getBoolean(propPkgName + ".disableOptimizeGeneratorExpressions"); // true
        this.TraceGeneratorInlining = Boolean.getBoolean(propPkgName + ".TraceGeneratorInlining"); // false
        this.TraceNodesWithoutSourceSection = Boolean.getBoolean(propPkgName + ".TraceNodesWithoutSourceSection"); // false
        this.TraceNodesUsingExistingProbe = Boolean.getBoolean(propPkgName + ".TraceNodesUsingExistingProbe"); // false
        this.CatchZippyExceptionForUnitTesting = Boolean.getBoolean(propPkgName + ".CatchZippyExceptionForUnitTesting"); // false
        this.forceLongType = Boolean.getBoolean(propPkgName + ".forceLongType"); // false
    }

    public PythonOptions(PrintStream standardOut, PrintStream standardErr) {
        this();
        if (standardOut != null)
            this.standardOut = standardOut;

        if (standardErr != null)
            this.standardErr = standardErr;
    }

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

    @TruffleBoundary
    public static void setEnvOptions(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.setProperty(propPkgName + "." + options[i], "true");
        }
    }

    @TruffleBoundary
    public static void unsetEnvOptions(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.getProperties().remove(propPkgName + "." + options[i]);
        }
    }

    public static boolean isEnvOptionSet(String option) {
        return Boolean.getBoolean(propPkgName + "." + option);
    }

}
