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
package edu.uci.python.nodes.expression;

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

import java.math.BigInteger;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;

public abstract class BinaryArithmeticNode extends BinaryOpNode {

    public abstract static class AddNode extends BinaryArithmeticNode {

        @Specialization(rewriteOn = ArithmeticException.class, order = 0)
        int doInteger(int left, int right) {
            return ExactMath.addExact(left, right);
        }

        @Specialization(order = 1)
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.add(right);
        }

        @Specialization(order = 2)
        double doDouble(double left, double right) {
            return left + right;
        }

        @Specialization(order = 3)
        PComplex doDoubleComplex(double left, PComplex right) {
            PComplex result = new PComplex(left + right.getReal(), right.getImag());
            return result;
        }

        @Specialization(order = 4)
        PComplex doComplexDouble(PComplex left, double right) {
            PComplex result = new PComplex(left.getReal() + right, left.getImag());
            return result;
        }

        @Specialization(order = 5)
        PComplex doComplex(PComplex left, PComplex right) {
            return left.add(right);
        }

        @Specialization(order = 6)
        String doString(String left, String right) {
            return left + right;
        }

        @Specialization(order = 7)
        PList doPList(PList left, PList right) {
            return left.__add__(right);
        }

        @Specialization(order = 8)
        PTuple doPTuple(PTuple left, PTuple right) {
            return left.__add__(right);
        }

        @Specialization(order = 9)
        PArray doPArray(PArray left, PArray right) {
            return left.__add__(right);
        }

        @SuppressWarnings("unused")
        @Specialization(order = 10)
        int doNoneInt(PNone left, int right) {
            return right;
        }

        @Specialization(order = 20, guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__add__", left, right);
        }

        // TODO: type info for operands in type error message.
        @Generic
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("unsupported operand type(s) for +: " + left + " " + right);
        }
    }

    public abstract static class SubNode extends BinaryArithmeticNode {

        @Specialization(rewriteOn = ArithmeticException.class, order = 0)
        int doInteger(int left, int right) {
            return ExactMath.subtractExact(left, right);
        }

        @Specialization(order = 1)
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.subtract(right);
        }

        @Specialization(order = 2)
        double doDouble(double left, double right) {
            return left - right;
        }

        @Specialization(order = 3)
        PComplex doDoubleComplex(double left, PComplex right) {
            PComplex result = new PComplex(left - right.getReal(), -right.getImag());
            return result;
        }

        @Specialization(order = 4)
        PComplex doComplexDoulbe(PComplex left, double right) {
            PComplex result = new PComplex(left.getReal() - right, left.getImag());
            return result;
        }

        @Specialization(order = 5)
        PComplex doComplex(PComplex left, PComplex right) {
            return left.sub(right);
        }

        @Specialization(order = 6)
        PBaseSet doPBaseSet(PBaseSet left, PBaseSet right) {
            return left.difference(right);
        }

        @Specialization(order = 20, guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__sub__", left, right);
        }
    }

    public abstract static class MulNode extends BinaryArithmeticNode {

        @Specialization(rewriteOn = ArithmeticException.class, order = 0)
        int doInteger(int left, int right) {
            return ExactMath.multiplyExact(left, right);
        }

        @Specialization(order = 1)
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.multiply(right);
        }

        @Specialization(order = 2)
        double doDouble(double left, double right) {
            return left * right;
        }

        @Specialization(order = 3)
        PComplex doDoubleComplex(double left, PComplex right) {
            PComplex result = new PComplex(left * right.getReal(), left * right.getImag());
            return result;
        }

        @Specialization(order = 4)
        PComplex doComplexDouble(PComplex left, double right) {
            PComplex result = new PComplex(left.getReal() * right, left.getImag() * right);
            return result;
        }

        @Specialization(order = 5)
        PComplex doComplex(PComplex left, PComplex right) {
            return left.mul(right);
        }

        @Specialization(order = 6)
        PList doIntPList(int left, PList right) {
            return right.__mul__(left);
        }

        @Specialization(order = 7)
        PList doPListInt(PList left, int right) {
            return left.__mul__(right);
        }

        @Specialization(order = 8)
        PTuple doIntPTuple(int left, PTuple right) {
            return right.__mul__(left);
        }

        @Specialization(order = 9)
        PTuple doPTupleInt(PTuple left, int right) {
            return left.__mul__(right);
        }

        @Specialization(order = 10)
        PArray doIntPArray(int left, PArray right) {
            return right.__mul__(left);
        }

        @Specialization(order = 11)
        PArray doPArrayInt(PArray left, int right) {
            return left.__mul__(right);
        }

        @Specialization(order = 12)
        String doIntString(int left, String right) {
            String str = right;
            for (int i = 0; i < left - 1; i++) {
                str = str + right;
            }

            return str;
        }

        @Specialization(order = 13)
        String doStringInt(String left, int right) {
            String str = left;
            for (int i = 0; i < right - 1; i++) {
                str = str + left;
            }

            return str;
        }

        @Specialization(order = 20, guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__mul__", left, right);
        }

        // TODO: better type error message.
        @Generic
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("can't multiply " + left + left.getClass() + " by " + right);
        }
    }

    public abstract static class DivNode extends BinaryArithmeticNode {

        /*
         * double division by zero in Java doesn't throw an exception, instead it yield Infinity
         * (NaN).
         */
        @Specialization(rewriteOn = ArithmeticException.class, order = 0)
        double doInteger(int left, int right) {
            if (right == 0) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new ArithmeticException("divide by zero");
            }

            return (double) left / right;
        }

        @Specialization(order = 1)
        double doBigInteger(BigInteger left, BigInteger right) {
            return left.divide(right).doubleValue();
        }

        @Specialization(order = 2)
        double doDouble(double left, double right) {
            return left / right;
        }

        @Specialization(order = 3)
        PComplex doDoubleComplex(double left, PComplex right) {
            double opNormSq = right.getReal() * right.getReal() + right.getImag() * right.getImag();
            PComplex conjugate = right.getConjugate();
            double realPart = left * conjugate.getReal();
            double imagPart = left * conjugate.getImag();
            return new PComplex(realPart / opNormSq, imagPart / opNormSq);
        }

        @Specialization(order = 4)
        PComplex doComplexDouble(PComplex left, double right) {
            double opNormSq = right * right;
            double realPart = left.getReal() * right;
            double imagPart = left.getImag() * right;
            return new PComplex(realPart / opNormSq, imagPart / opNormSq);
        }

        @Specialization(order = 5)
        PComplex doComplex(PComplex left, PComplex right) {
            return left.div(right);
        }

        @Specialization(order = 20, guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__truediv__", left, right);
        }

        @Generic
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("Unsupported operand type for /: " + left + " and " + right);
        }
    }

    public abstract static class FloorDivNode extends BinaryArithmeticNode {

        @Specialization
        int doInteger(int left, int right) {
            return left / right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.divide(right);
        }

        @Specialization
        double doDouble(double left, double right) {
            return Math.floor(left / right);
        }

        @Specialization(order = 20, guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__floordiv__", left, right);
        }

        @Generic
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("Unsupported operand type for //: " + left + " and " + right);
        }
    }

    public abstract static class ModuloNode extends BinaryArithmeticNode {

        @Specialization
        int doInteger(int left, int right) {
            return left % right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.mod(right);
        }

        @Specialization
        double doDouble(double left, double right) {
            return left % right;
        }

        /**
         * Delegate to Jython for String formatting.
         */
        @SlowPath
        @Specialization(order = 10)
        Object doString(String left, Object right) {
            PyString sleft = new PyString(left);
            return unboxPyObject(sleft.__mod__(adaptToPyObject(right)));
        }

        @Specialization(order = 20, guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__mod__", left, right);
        }

        @Generic
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("Unsupported operand type for %: " + left + " and " + right);
        }
    }

    public abstract static class PowerNode extends BinaryArithmeticNode {

        @Specialization
        int doInteger(int left, int right) {
            return (int) Math.pow(left, right);
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            double value = Math.pow(left.doubleValue(), right.doubleValue());
            return BigInteger.valueOf((long) value);
        }

        @Specialization
        double doDouble(double left, double right) {
            return Math.pow(left, right);
        }

        @Specialization(order = 20, guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__pow__", left, right);
        }
    }

}
