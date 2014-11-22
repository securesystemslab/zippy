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

import java.util.*;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.function.*;

/**
 * @author zwei
 * @author myq
 * 
 */
public class MathModuleBuiltins extends PythonBuiltins {

    @Override
    protected List<? extends NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return MathModuleBuiltinsFactory.getFactories();
    }

    // math.sqrt
    @Builtin(name = "sqrt", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class SqrtNode extends PythonBuiltinNode {

        @Specialization
        public double sqrt(int value) {
            return Math.sqrt(value);
        }

        @Specialization
        public double sqrt(double value) {
            return Math.sqrt(value);
        }
    }

    @Builtin(name = "exp", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class ExpNode extends PythonBuiltinNode {

        @Specialization
        public double exp(int value) {
            return Math.exp(value);
        }

        @Specialization
        public double exp(double value) {
            return Math.exp(value);
        }
    }

    @Builtin(name = "pi", fixedNumOfArguments = 0, hasFixedNumOfArguments = true)
    public abstract static class PiNode extends PythonBuiltinNode {

        @Specialization
        public double pi() {
            return Math.PI;
        }
    }

    @Builtin(name = "e", fixedNumOfArguments = 0, hasFixedNumOfArguments = true)
    public abstract static class ENode extends PythonBuiltinNode {

        @Specialization
        public double e() {
            return Math.E;
        }

    }

    @Builtin(name = "ceil", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class CeilNode extends PythonBuiltinNode {

        @Specialization
        public double ceil(double value) {
            return Math.ceil(value);
        }
    }

    @Builtin(name = "acos", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class AcosNode extends PythonBuiltinNode {

        @Specialization
        public double acos(int value) {
            return Math.acos(value);
        }

        @Specialization
        public double acos(double value) {
            return Math.acos(value);
        }
    }

    @Builtin(name = "cos", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class CosNode extends PythonBuiltinNode {

        @Specialization
        public double cos(int value) {
            return Math.cos(value);
        }

        @Specialization
        public double cos(double value) {
            return Math.cos(value);
        }
    }

    @Builtin(name = "sin", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class SinNode extends PythonBuiltinNode {

        @Specialization
        public double sin(int value) {
            return Math.sin(value);
        }

        @Specialization
        public double sin(double value) {
            return Math.sin(value);
        }
    }

    @Builtin(name = "log", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class LogNode extends PythonBuiltinNode {

        @Specialization
        public double log(int value) {
            return Math.log(value);
        }

        @Specialization
        public double log(double value) {
            return Math.log(value);
        }
    }
}
