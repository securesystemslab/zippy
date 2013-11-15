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
import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtypes.*;

public final class ArrayModuleBuiltins extends PythonBuiltins {

    // array.array(typecode[, initializer])
    @Builtin(name = "array", minNumOfArguments = 1, maxNumOfArguments = 2)
    public abstract static class PythonArrayNode extends PythonBuiltinNode {

        public PythonArrayNode(String name) {
            super(name);
        }

        public PythonArrayNode(PythonArrayNode prev) {
            this(prev.getName());
        }

        @SuppressWarnings("unused")
        @Specialization(order = 1, guards = "noInitializer")
        public PArray array(String typeCode, Object initializer) {
            return makeEmptyArray(typeCode.charAt(0));
            /**
             * TODO @param typeCode should be a char, not a string
             */
        }

        @Specialization(order = 2)
        public PArray arrayWithInitializer(String typeCode, Object initializer) {
            return makeArray(typeCode.charAt(0), initializer);
        }

        @SuppressWarnings("unused")
        public static boolean noInitializer(String typeCode, Object initializer) {
            return (initializer instanceof PNone);
        }

        @SlowPath
        private static PArray makeEmptyArray(char type) {
            switch (type) {
                case 'c':
                    return new PCharArray();
                case 'i':
                    return new PIntegerArray();
                case 'd':
                    return new PDoubleArray();
                default:
                    return null;
            }
        }

        @SlowPath
        private static PArray makeArray(char type, Object initializer) {
            Object[] copyArray;
            switch (type) {
                case 'c':
                    if (initializer instanceof String) {
                        return new PCharArray(((String) initializer).toCharArray());
                    } else {
                        throw new RuntimeException("Unexpected argument type for array() ");
                    }
                case 'i':
                    copyArray = ((PSequence) initializer).getSequence();
                    int[] intArray = new int[copyArray.length];
                    for (int i = 0; i < intArray.length; i++) {
                        if (copyArray[i] instanceof Integer) {
                            intArray[i] = (int) copyArray[i];
                        } else {
                            throw new RuntimeException("Unexpected argument type for array() ");
                        }
                    }
                    return new PIntegerArray(intArray);
                case 'd':
                    copyArray = ((PSequence) initializer).getSequence();
                    double[] doubleArray = new double[copyArray.length];
                    for (int i = 0; i < doubleArray.length; i++) {
                        if (copyArray[i] instanceof Integer) {
                            doubleArray[i] = (int) copyArray[i];
                        } else if (copyArray[i] instanceof Double) {
                            doubleArray[i] = (double) copyArray[i];
                        } else {
                            throw new RuntimeException("Unexpected argument type for array() ");
                        }
                    }
                    return new PDoubleArray(doubleArray);
                default:
                    return null;
            }
        }
    }

    @Override
    public void initialize() {
        Class<?>[] declaredClasses = ArrayModuleBuiltins.class.getDeclaredClasses();

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

        if (builtin.name().equals("array")) {
            return ArrayModuleBuiltinsFactory.PythonArrayNodeFactory.create(builtin.name(), args);
        } else {
            throw new RuntimeException("Unsupported/Unexpected Builtin: " + builtin);
        }
    }
}
