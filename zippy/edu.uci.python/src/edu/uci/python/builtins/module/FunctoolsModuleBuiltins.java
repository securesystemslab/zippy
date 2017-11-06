/*
 * Copyright (c) 2014, Regents of the University of California
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

import java.util.List;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;

import edu.uci.python.builtins.Builtin;
import edu.uci.python.builtins.PythonBuiltins;
import edu.uci.python.nodes.function.PythonBuiltinNode;
import edu.uci.python.runtime.datatype.PIterable;
import edu.uci.python.runtime.datatype.PNone;
import edu.uci.python.runtime.exception.StopIterationException;
import edu.uci.python.runtime.function.PArguments;
import edu.uci.python.runtime.function.PFunction;
import edu.uci.python.runtime.function.PythonCallable;
import edu.uci.python.runtime.iterator.PIterator;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.standardtype.PythonBuiltinObject;

/**
 * @author myq
 *
 */
public class FunctoolsModuleBuiltins extends PythonBuiltins {

    @Override
    protected List<? extends NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return FunctoolsModuleBuiltinsFactory.getFactories();
    }

    @Builtin(name = "reduce", minNumOfArguments = 2, maxNumOfArguments = 3, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class ReduceNode extends PythonBuiltinNode {

        public Object reduceFunctionIterableAthena(PFunction function, PList iterable, PythonBuiltinObject initializer) {
            PIterator iter = iterable.__iter__();
            Object init = (initializer instanceof PNone) ? iter.__next__() : initializer;
            return doReduce(function, iter, init);
        }

        public Object reduceFunctionIterableAthena(PFunction function, PList iterable, Object initializer) {
            PIterator iter = iterable.__iter__();
            Object init = (initializer instanceof PNone) ? iter.__next__() : initializer;
            return doReduce(function, iter, init);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noInitializer(initializer)")
        public Object reduceFunctionIterableNoInit(PythonCallable function, PIterable iterable, PythonBuiltinObject initializer) {
            PIterator iter = iterable.__iter__();
            Object init = iter.__next__();
            return doReduce(function, iter, init);
        }

        @Specialization
        public Object reduceFunctionIterable(PythonCallable function, PIterable iterable, PythonBuiltinObject initializer) {
            PIterator iter = iterable.__iter__();
            Object init = initializer;
            return doReduce(function, iter, init);
        }

        @Specialization
        public Object reduceFunctionIterable(PythonCallable function, PIterable iterable, Object initializer) {
            PIterator iter = iterable.__iter__();
            Object init = initializer;
            return doReduce(function, iter, init);
        }

        private static Object doReduce(PythonCallable reduceFunction, PIterator iter, Object initializer) {
            Object ret = initializer;
            try {
                while (true) {
                    ret = reduceFunction.call(PArguments.createWithUserArguments(ret, iter.__next__()));
                }
            } catch (StopIterationException e) {

            }

            return ret;
        }

        @Specialization
        public Object reduceSequence(Object function, Object iterable, Object initializer) {
            throw new RuntimeException(String.format("reduce is not supported for %s %s iterable %s %s initializer %s %s",
                            function, function.getClass(), iterable, iterable.getClass(), initializer, initializer.getClass()));
        }

    }

}
