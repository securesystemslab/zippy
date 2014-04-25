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
package edu.uci.python.builtins.module;

import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

/**
 * @author Gulfem
 * @author zwei
 */

public final class ArrayModuleBuiltins extends PythonBuiltins {

    @Override
    protected List<? extends NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return ArrayModuleBuiltinsFactory.getFactories();
    }

    // array.array(typecode[, initializer])
    @Builtin(name = "array", minNumOfArguments = 1, maxNumOfArguments = 2)
    public abstract static class PythonArrayNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(order = 1, guards = "noInitializer")
        public PArray array(String typeCode, Object initializer) {
            /**
             * TODO @param typeCode should be a char, not a string
             */
            return makeEmptyArray(typeCode.charAt(0));
        }

        @Specialization(order = 2)
        public PArray arrayWithRangeInitializer(String typeCode, PRange range) {
            if (!typeCode.equals("i")) {
                typeError(typeCode, range);
            }

            int[] intArray = new int[range.len()];

            int start = range.getStart();
            int stop = range.getStop();
            int step = range.getStep();

            int index = 0;
            for (int i = start; i < stop; i += step) {
                intArray[index++] = i;
            }

            return new PIntArray(intArray);
        }

        @Specialization(order = 3)
        public PArray arrayWithSequenceInitializer(String typeCode, String str) {
            if (!typeCode.equals("c")) {
                typeError(typeCode, str);
            }

            return new PCharArray(str.toCharArray());
        }

        @Specialization(order = 4)
        public PArray arrayWithSequenceInitializer(String typeCode, PSequence initializer) {
            return makeArray(typeCode.charAt(0), initializer);
        }

        @SuppressWarnings("unused")
        @Specialization(order = 5)
        public PArray arrayWithObjectInitializer(String typeCode, Object initializer) {
            throw new RuntimeException("Unsupported initializer " + initializer);
        }

        @SuppressWarnings("unused")
        public static boolean noInitializer(String typeCode, Object initializer) {
            return (initializer instanceof PNone);
        }

        private static PArray makeEmptyArray(char type) {
            switch (type) {
                case 'c':
                    return new PCharArray();
                case 'i':
                    return new PIntArray();
                case 'd':
                    return new PDoubleArray();
                default:
                    return null;
            }
        }

        private static PArray makeArray(char type, PSequence sequence) {
            SequenceStorage store;
            switch (type) {
                case 'i':
                    PIterator iter = sequence.__iter__();
                    int[] intArray = new int[sequence.len()];
                    int i = 0;

                    try {
                        while (true) {
                            try {
                                intArray[i++] = PythonTypesGen.PYTHONTYPES.expectInteger(iter.__next__());
                            } catch (UnexpectedResultException e) {
                                operandTypeError();
                            }
                        }
                    } catch (StopIterationException e) {
                        // fall through
                    }

                    return new PIntArray(intArray);
                case 'd':
                    store = sequence.getStorage();
                    double[] doubleArray = new double[store.length()];

                    for (i = 0; i < doubleArray.length; i++) {
                        doubleArray[i] = PythonTypesGen.PYTHONTYPES.asImplicitDouble(store.getItemInBound(i));
                    }

                    return new PDoubleArray(doubleArray);
                default:
                    return null;
            }
        }

        @SlowPath
        private static void typeError(String typeCode, Object initializer) {
            throw Py.TypeError("unsupported operand type:" + typeCode.charAt(0) + " " + initializer + " and 'array.array'");
        }

        @SlowPath
        private static void operandTypeError() {
            throw new RuntimeException("Unexpected argument type for array() ");
        }
    }

}
