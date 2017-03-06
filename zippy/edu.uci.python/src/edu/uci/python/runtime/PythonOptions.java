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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import edu.uci.python.PythonLanguage;

public class PythonOptions {

    protected static final String propPkgName = PythonLanguage.class.getPackage().getName();
    // Debug flags
    public static final boolean PrintAST = Boolean.getBoolean(propPkgName + ".PrintAST"); // false

    public static final boolean VisualizedAST = Boolean.getBoolean(propPkgName + ".VisualizedAST"); // false

    public static final String PrintASTFilter = System.getProperty(propPkgName + ".PrintASTFilter"); // null

    public static final boolean TraceJythonRuntime = Boolean.getBoolean(propPkgName + ".TraceJythonRuntime"); // false

    public static final boolean TraceImports = Boolean.getBoolean(propPkgName + ".TraceImports"); // false

    public static final boolean TraceSequenceStorageGeneralization = Boolean.getBoolean(propPkgName + ".TraceSequenceStorageGeneralization"); // false

    public static final boolean TraceObjectLayoutCreation = Boolean.getBoolean(propPkgName + ".TraceObjectLayoutCreation"); // false

    // Object storage allocation
    public static final boolean InstrumentObjectStorageAllocation = Boolean.getBoolean(propPkgName + ".InstrumentObjectStorageAllocation"); // false

    // Translation flags
    public static final boolean UsePrintFunction = Boolean.getBoolean(propPkgName + ".UsePrintFunction"); // false

    // Runtime flags
    public static final boolean UnboxSequenceStorage = !Boolean.getBoolean(propPkgName + ".disableUnboxSequenceStorage"); // true

    public static final boolean UnboxSequenceIteration = !Boolean.getBoolean(propPkgName + ".disableUnboxSequenceIteration"); // true

    public static final boolean IntrinsifyBuiltinCalls = !Boolean.getBoolean(propPkgName + ".disableIntrinsifyBuiltinCalls"); // true

    public static final int AttributeAccessInlineCacheMaxDepth = 20;

    public static final int CallSiteInlineCacheMaxDepth = 20;

    public static final boolean FlexibleObjectStorageEvolution = Boolean.getBoolean(propPkgName + ".FlexibleObjectStorageEvolution"); // false

    public static final boolean FlexibleObjectStorage = Boolean.getBoolean(propPkgName + ".FlexibleObjectStorage"); // false

    // Generators
    public static final boolean InlineGeneratorCalls = !Boolean.getBoolean(propPkgName + ".disableInlineGeneratorCalls"); // true

    public static boolean OptimizeGeneratorExpressions = !Boolean.getBoolean(propPkgName + ".disableOptimizeGeneratorExpressions"); // true

    public static final boolean TraceGeneratorInlining = Boolean.getBoolean(propPkgName + ".TraceGeneratorInlining"); // false

    public static final boolean TraceNodesWithoutSourceSection = Boolean.getBoolean(propPkgName + ".TraceNodesWithoutSourceSection"); // false

    public static final boolean TraceNodesUsingExistingProbe = Boolean.getBoolean(propPkgName + ".TraceNodesUsingExistingProbe"); // false

    public static final boolean CatchZippyExceptionForUnitTesting = Boolean.getBoolean(propPkgName + ".CatchZippyExceptionForUnitTesting"); // false

    public static final boolean forceLongType = Boolean.getBoolean(propPkgName + ".forceLongType"); // false

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

    @TruffleBoundary
    public static void setOptions(String[] options, boolean[] newValue) {
        for (int i = 0; i < options.length && i < newValue.length; i++) {
            Field field;
            try {
                field = PythonOptions.class.getField(options[i]);
                field.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                field.set(null, newValue[i]);
                modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
            } catch (Exception e) {
                System.err.println("Unable to set option " + options[i]);
                e.printStackTrace();
            }
        }
    }

}
