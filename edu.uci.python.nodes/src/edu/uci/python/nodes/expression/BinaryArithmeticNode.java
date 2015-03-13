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
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.misc.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

public abstract class BinaryArithmeticNode extends BinaryOpNode {

    @NodeInfo(shortName = "__add__")
    public abstract static class AddNode extends BinaryArithmeticNode {

        @Specialization
        int doBoolean(boolean left, boolean right) {
            final int leftInt = left ? 1 : 0;
            final int rightInt = right ? 1 : 0;
            return leftInt + rightInt;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        int doInteger(int left, int right) {
            return ExactMath.addExact(left, right);
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.add(right);
        }

        @Specialization
        double doDoubleBoolean(double left, boolean right) {
            final double rightDouble = right ? 1.0 : 0.0;
            return left + rightDouble;
        }

        @Specialization
        double doDoubleBoolean(boolean left, double right) {
            final double leftDouble = left ? 1.0 : 0.0;
            return leftDouble + right;
        }

        @Specialization
        double doDouble(double left, double right) {
            return left + right;
        }

        @Specialization
        PComplex doComplexBoolean(PComplex left, boolean right) {
            final double rightDouble = right ? 1.0 : 0.0;
            PComplex result = new PComplex(left.getReal() + rightDouble, left.getImag());
            return result;
        }

        @Specialization
        PComplex doComplexInt(PComplex left, int right) {
            PComplex result = new PComplex(left.getReal() + right, left.getImag());
            return result;
        }

        @Specialization
        PComplex doDoubleComplex(double left, PComplex right) {
            PComplex result = new PComplex(left + right.getReal(), right.getImag());
            return result;
        }

        @Specialization
        PComplex doComplexDouble(PComplex left, double right) {
            PComplex result = new PComplex(left.getReal() + right, left.getImag());
            return result;
        }

        @Specialization
        PComplex doComplex(PComplex left, PComplex right) {
            return left.add(right);
        }

        @Specialization
        String doString(String left, String right) {
            return left + right;
        }

        @Specialization(guards = "areBothIntStorage")
        PList doPListInt(PList left, PList right) {
            IntSequenceStorage leftStore = (IntSequenceStorage) left.getStorage().copy();
            IntSequenceStorage rightStore = (IntSequenceStorage) right.getStorage();
            leftStore.extendWithIntStorage(rightStore);
            return new PList(leftStore);
        }

        @Specialization(guards = "areBothObjectStorage")
        PList doPListObject(PList left, PList right) {
            ObjectSequenceStorage leftStore = (ObjectSequenceStorage) left.getStorage().copy();
            ObjectSequenceStorage rightStore = (ObjectSequenceStorage) right.getStorage();
            leftStore.extend(rightStore);
            return new PList(leftStore);
        }

        @Specialization
        PList doPList(PList left, PList right) {
            return left.__add__(right);
        }

        @Specialization
        PTuple doPTuple(PTuple left, PTuple right) {
            return left.__add__(right);
        }

        @Specialization
        PArray doPArray(PArray left, PArray right) {
            return left.__add__(right);
        }

        @SuppressWarnings("unused")
        @Specialization
        int doNoneInt(PNone left, int right) {
            return right;
        }

        @Specialization(guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__add__", left, right);
        }

        // TODO: type info for operands in type error message.
        @Fallback
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("unsupported operand type(s) for +: " + left + " " + right);
        }
    }

    @NodeInfo(shortName = "__sub__")
    public abstract static class SubNode extends BinaryArithmeticNode {

        @Specialization(rewriteOn = ArithmeticException.class)
        int doInteger(int left, int right) {
            return ExactMath.subtractExact(left, right);
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.subtract(right);
        }

        @Specialization
        double doDouble(double left, double right) {
            return left - right;
        }

        @Specialization
        PComplex doDoubleComplex(double left, PComplex right) {
            PComplex result = new PComplex(left - right.getReal(), -right.getImag());
            return result;
        }

        @Specialization
        PComplex doComplexDoulbe(PComplex left, double right) {
            PComplex result = new PComplex(left.getReal() - right, left.getImag());
            return result;
        }

        @Specialization
        PComplex doComplex(PComplex left, PComplex right) {
            return left.sub(right);
        }

        @Specialization
        PBaseSet doPBaseSet(PBaseSet left, PBaseSet right) {
            return left.difference(right);
        }

        @Specialization(guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__sub__", left, right);
        }
    }

    @NodeInfo(shortName = "__mul__")
    public abstract static class MulNode extends BinaryArithmeticNode {

        @Specialization(rewriteOn = ArithmeticException.class)
        int doInteger(int left, int right) {
            return ExactMath.multiplyExact(left, right);
        }

        @Specialization
        BigInteger doIntegerBigInteger(int left, BigInteger right) {
            return doBigInteger(BigInteger.valueOf(left), right);
        }

        @Specialization
        BigInteger doIntegerBigInteger(BigInteger left, int right) {
            return doBigInteger(left, BigInteger.valueOf(right));
        }

        @TruffleBoundary
        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.multiply(right);
        }

        @Specialization
        double doDouble(double left, double right) {
            return left * right;
        }

        @Specialization
        PComplex doDoubleComplex(double left, PComplex right) {
            return new PComplex(left * right.getReal(), left * right.getImag());
        }

        @Specialization
        PComplex doComplexDouble(PComplex left, double right) {
            return new PComplex(left.getReal() * right, left.getImag() * right);
        }

        @Specialization
        PComplex doComplex(PComplex left, PComplex right) {
            return left.mul(right);
        }

        @Specialization
        PList doIntPList(int left, PList right) {
            return right.__mul__(left);
        }

        @Specialization
        PList doPListInt(PList left, int right) {
            return left.__mul__(right);
        }

        @Specialization
        PTuple doIntPTuple(int left, PTuple right) {
            return right.__mul__(left);
        }

        @Specialization
        PTuple doPTupleInt(PTuple left, int right) {
            return left.__mul__(right);
        }

        @Specialization
        PArray doIntPArray(int left, PArray right) {
            return right.__mul__(left);
        }

        @Specialization
        PArray doPArrayInt(PArray left, int right) {
            return left.__mul__(right);
        }

        @Specialization
        String doIntString(int left, String right) {
            String str = right;
            for (int i = 0; i < left - 1; i++) {
                str = str + right;
            }

            return str;
        }

        @Specialization
        String doStringInt(String left, int right) {
            String str = left;
            for (int i = 0; i < right - 1; i++) {
                str = str + left;
            }

            return str;
        }

        @Specialization(guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__mul__", left, right);
        }

        // TODO: better type error message.
        @Fallback
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("can't multiply " + left + left.getClass() + " by " + right);
        }
    }

    @NodeInfo(shortName = "__div__")
    public abstract static class DivNode extends BinaryArithmeticNode {

        /*
         * double division by zero in Java doesn't throw an exception, instead it yield Infinity
         * (NaN).
         */
        @Specialization(rewriteOn = ArithmeticException.class)
        double doInteger(int left, int right) {
            if (right == 0) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new ArithmeticException("divide by zero");
            }

            return (double) left / right;
        }

        @Specialization
        double doBigInteger(BigInteger left, BigInteger right) {
            return FastMathUtil.slowPathDivide(left, right).doubleValue();
        }

        @Specialization
        double doDouble(double left, double right) {
            return left / right;
        }

        @Specialization
        PComplex doDoubleComplex(double left, PComplex right) {
            double opNormSq = right.getReal() * right.getReal() + right.getImag() * right.getImag();
            PComplex conjugate = right.getConjugate();
            double realPart = left * conjugate.getReal();
            double imagPart = left * conjugate.getImag();
            return new PComplex(realPart / opNormSq, imagPart / opNormSq);
        }

        @Specialization
        PComplex doComplexDouble(PComplex left, double right) {
            double opNormSq = right * right;
            double realPart = left.getReal() * right;
            double imagPart = left.getImag() * right;
            return new PComplex(realPart / opNormSq, imagPart / opNormSq);
        }

        @Specialization
        PComplex doComplex(PComplex left, PComplex right) {
            return left.div(right);
        }

        @Specialization(guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__truediv__", left, right);
        }

        @Fallback
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("Unsupported operand type for /: " + left + " and " + right);
        }
    }

    @NodeInfo(shortName = "__floordiv__")
    public abstract static class FloorDivNode extends BinaryArithmeticNode {

        @Specialization
        int doInteger(int left, int right) {
            return left / right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return FastMathUtil.slowPathDivide(left, right);
        }

        @Specialization
        double doDouble(double left, double right) {
            return Math.floor(left / right);
        }

        @Specialization(guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__floordiv__", left, right);
        }

        @Fallback
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("Unsupported operand type for //: " + left + " and " + right);
        }
    }

    @NodeInfo(shortName = "__mod__")
    public abstract static class ModuloNode extends BinaryArithmeticNode {

        @Specialization(guards = "isLeftPositive")
        int doInteger(int left, int right) {
            return left % right;
        }

        @Specialization
        int doIntegerNegative(int left, int right) {
            return (left + right) % right;
        }

        @SuppressWarnings("unused")
        protected static boolean isLeftPositive(int left, int right) {
            return left >= 0;
        }

        @SuppressWarnings("unused")
        protected static boolean isLeftNegative(int left, int right) {
            return left < 0;
        }

        @TruffleBoundary
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
        @TruffleBoundary
        @Specialization
        Object doString(String left, Object right) {
            PyString sleft = new PyString(left);
            return unboxPyObject(sleft.__mod__(adaptToPyObject(right)));
        }

        @Specialization(order = 20, guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__mod__", left, right);
        }

        @Fallback
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("Unsupported operand type for %: " + left + " and " + right);
        }
    }

    @NodeInfo(shortName = "__pow__")
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

        @Specialization(guards = "isEitherOperandPythonObject")
        Object doPythonObject(VirtualFrame frame, Object left, Object right) {
            return doSpecialMethodCall(frame, "__pow__", left, right);
        }
    }

}
