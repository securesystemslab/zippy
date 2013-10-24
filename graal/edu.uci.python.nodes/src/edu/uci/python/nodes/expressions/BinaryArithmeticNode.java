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
package edu.uci.python.nodes.expressions;

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

import java.math.BigInteger;

import org.python.core.*;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.Generic;

import edu.uci.python.runtime.datatypes.*;

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
            return left.concat(right);
        }

        @Specialization(order = 8)
        PTuple doPTuple(PTuple left, PTuple right) {
            return left.concat(right);
        }

        @Specialization(order = 10)
        PArray doPDoubleArray(PArray left, PArray right) {
            return left.append(right);
        }

        // TODO: type info for operands in type error message.
        @SuppressWarnings("unused")
        @Generic
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("unsupported operand type(s) for +:");
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
        PObject doIntPObject(int left, PObject right) {
            return right.multiply(left);
        }

        @Specialization(order = 7)
        PObject doPObjectInt(PObject left, int right) {
            return left.multiply(right);
        }

        // TODO: better type error message.
        @Generic
        Object doGeneric(Object left, Object right) {
            throw Py.TypeError("can't multiply " + left + " by " + right);
        }
    }

    public abstract static class DivNode extends BinaryArithmeticNode {

        @Specialization(rewriteOn = ArithmeticException.class, order = 0)
        int doInteger(int left, int right) {
            return left / right;
        }

        @Specialization(order = 1)
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.divide(right);
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

        @Generic
        Object doGeneric(Object left, Object right) {
            if (left instanceof String) {
                PyString s = new PyString((String) left);
                return unboxPyObject(s.__mod__(adaptToPyObject(right)));
            } else {
                throw new RuntimeException("Invalid generic!");
            }
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
    }

}
