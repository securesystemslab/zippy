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

import java.util.*;

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.dsl.NodeFactory;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.sequence.*;

public final class ArrayModuleBuiltins extends PythonBuiltins {

    @Override
    protected List<? extends NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return ArrayModuleBuiltinsFactory.getFactories();
    }

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
}
