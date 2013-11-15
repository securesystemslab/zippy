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
package edu.uci.python.builtins;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtypes.*;

public final class StringBuiltins extends PythonBuiltins {

    // str.join(iterable)
    @Builtin(name = "join", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonStringJoinNode extends PythonBuiltinNode {

        public PythonStringJoinNode(String name) {
            super(name);
        }

        public PythonStringJoinNode(PythonStringJoinNode prev) {
            this(prev.getName());
        }

        @Specialization
        public String join(Object self, Object arg) {
            if (arg instanceof String) {
                StringBuilder sb = new StringBuilder();
                char[] joinString = ((String) arg).toCharArray();
                for (int i = 0; i < joinString.length - 1; i++) {
                    sb.append(Character.toString(joinString[i]));
                    sb.append(self.toString());
                }
                sb.append(Character.toString(joinString[joinString.length - 1]));

                return sb.toString();
            } else if (arg instanceof PSequence) {
                StringBuilder sb = new StringBuilder();
                Object[] stringList = ((PSequence) arg).getSequence();
                for (int i = 0; i < stringList.length - 1; i++) {
                    sb.append(stringList[i].toString());
                    sb.append(self.toString());
                }
                sb.append((String) stringList[stringList.length - 1]);

                return sb.toString();
            } else if (arg instanceof PCharArray) {
                StringBuilder sb = new StringBuilder();
                char[] stringList = ((PCharArray) arg).getSequence();
                for (int i = 0; i < stringList.length - 1; i++) {
                    sb.append(Character.toString(stringList[i]));
                    sb.append((String) self);
                }
                sb.append(Character.toString(stringList[stringList.length - 1]));

                return sb.toString();
            } else {
                throw new RuntimeException("invalid arguments type for join()");
            }
        }
    }

    @Override
    public void initialize() {
        Class<?>[] declaredClasses = StringBuiltins.class.getDeclaredClasses();

        for (int i = 0; i < declaredClasses.length; i++) {
            Class<?> clazz = declaredClasses[i];
            PBuiltinFunction function = findBuiltinFunction(clazz);

            if (function != null) {
                setBuiltinFunction(function.getName(), function);
            }
        }
    }

    private static PBuiltinFunction findBuiltinFunction(Class<?> clazz) {
        Builtin builtin = clazz.getAnnotation(Builtin.class);

        if (builtin != null) {
            String methodName = builtin.name();
            PythonBuiltinNode builtinNode = createBuiltin(builtin);
            BuiltinFunctionRootNode rootNode = new BuiltinFunctionRootNode(builtinNode);
            CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
            Arity arity = new Arity(methodName, builtin.fixedNumOfArguments(), builtin.fixedNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(),
                            builtin.takesVariableArguments());
            PBuiltinFunction builtinClass;

            if (builtin.hasFixedNumOfArguments()) {
                builtinClass = new PBuiltinFunction(methodName, arity, callTarget);
            } else {
                builtinClass = new PBuiltinFunction(methodName, arity, callTarget);
            }

            return builtinClass;
        }

        return null;
    }

    private static PythonBuiltinNode createBuiltin(Builtin builtin) {
        PNode[] args;
        int totalNumOfArgs;
        if (builtin.name().equals("max") || builtin.name().equals("min")) {
            totalNumOfArgs = 3;
        } else if (builtin.hasFixedNumOfArguments()) {
            totalNumOfArgs = builtin.fixedNumOfArguments();
        } else if (builtin.takesVariableArguments()) {
            totalNumOfArgs = builtin.minNumOfArguments() + 1;
        } else {
            totalNumOfArgs = builtin.maxNumOfArguments();
        }

        args = new PNode[totalNumOfArgs];
        for (int i = 0; i < totalNumOfArgs; i++) {
            args[i] = new ReadArgumentNode(i);
        }

        if (builtin.takesVariableArguments()) {
            args[totalNumOfArgs - 1] = new ReadVarArgsNode(totalNumOfArgs - 1);
        } else {
            if (builtin.takesKeywordArguments()) {
                args[totalNumOfArgs - 1] = new ReadArgumentNode(totalNumOfArgs - 1);
            }
        }

        if (builtin.name().equals("join")) {
            return StringBuiltinsFactory.PythonStringJoinNodeFactory.create(builtin.name(), args);
        } else {
            throw new RuntimeException("Unsupported/Unexpected Builtin: " + builtin);
        }
    }
}
