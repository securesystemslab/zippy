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
package edu.uci.python.runtime.function;

import org.python.core.*;

import com.oracle.truffle.api.CompilerDirectives.*;

public class Arity {

    private final String functionName;
    private final int minNumOfArgs;
    private final int maxNumOfArgs;

    private final boolean takesKeywordArg;
    private final boolean takesFixedNumOfArgs;
    private final boolean takesVarArgs;

    private final String[] keywordNames;

    // private final ArrayList keywordNames;

    public Arity(String functionName, int minNumOfArgs, int maxNumOfArgs, boolean takesKeywordArg, boolean takesFixedNumOfArgs, boolean takesVarArgs, String[] keywordNames) {
        this.functionName = functionName;
        this.minNumOfArgs = minNumOfArgs;
        this.maxNumOfArgs = maxNumOfArgs;
        this.takesKeywordArg = takesKeywordArg;
        this.takesFixedNumOfArgs = takesFixedNumOfArgs;
        this.takesVarArgs = takesVarArgs;
        this.keywordNames = keywordNames;
    }

    public void arityCheck(int numOfArgs, int numOfKeywords, PKeyword[] keywords) {
        if (keywords == null) {
            arityCheck(numOfArgs, numOfKeywords);
        } else {
            for (int i = 0; i < keywords.length; i++) {
                PKeyword keyword = keywords[i];
                for (int k = 0; k < keywordNames.length; k++) {
                    if (keywordNames[k].equals(keyword.getName())) {
                        break;
                    }
                }

                throw Py.TypeError(functionName + "()" + " got an unexpected keyword argument " + "'" + keyword.getName() + "'");
            }
        }
    }

    /**
     * See {@link org.python.core.PyBuiltinCallable.DefaultInfo}.
     */
    @SlowPath
    private void arityCheck(int numOfArgs, int numOfKeywords) {
        if (!takesKeywordArg && numOfKeywords > 0) {
            throw Py.TypeError(functionName + "() takes no keyword arguments");
        }

        String argMessage;
        if (takesFixedNumOfArgs) {
            assert (minNumOfArgs == maxNumOfArgs);
            if (numOfArgs != minNumOfArgs) {
                if (minNumOfArgs == 0) {
                    argMessage = "no arguments";
                } else if (minNumOfArgs == 1) {
                    argMessage = "exactly one argument";
                } else {
                    argMessage = minNumOfArgs + " arguments";
                }
                throw Py.TypeError(String.format("%s() takes %s (%d given)", functionName, argMessage, numOfArgs));
            }
        } else if (numOfArgs < minNumOfArgs) {
            /**
             * For ex, iter(object[, sentinel]) takes at least 1 argument.
             */
            throw Py.TypeError(String.format("%s() expected at least %d arguments (%d) given", functionName, minNumOfArgs, numOfArgs));
        } else if (!takesVarArgs && numOfArgs > maxNumOfArgs) {
            /**
             * For ex, complex([real[, imag]]) takes at most 2 arguments.
             */
            argMessage = "at most " + maxNumOfArgs + " arguments";
            throw Py.TypeError(String.format("%s() takes %s (%d given)", functionName, argMessage, numOfArgs));
        }

    }
}
