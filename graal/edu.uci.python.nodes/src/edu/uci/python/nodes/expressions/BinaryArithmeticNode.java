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
import java.util.ArrayList;
import java.util.List;

import org.python.core.*;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.Generic;

import edu.uci.python.nodes.translation.*;
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
            List<Object> list = new ArrayList<>();
            List<Object> leftList = left.getList();
            for (int i = 0; i < leftList.size(); i++) {
                list.add(leftList.get(i));
            }
            List<Object> rightList = right.getList();
            for (int i = 0; i < rightList.size(); i++) {
                list.add(rightList.get(i));
            }
            return new PList(list);
        }

        @Specialization(order = 8)
        PTuple doPTuple(PTuple left, PTuple right) {
            Object[] newArray = new Object[left.len() + right.len()];
            int index = 0;
            Object[] leftArray = left.getArray();
            for (int i = 0; i < leftArray.length; i++) {
                newArray[index++] = leftArray[i];
            }
            Object[] rightArray = right.getArray();
            for (int i = 0; i < rightArray.length; i++) {
                newArray[index++] = rightArray[i];
            }
            return new PTuple(newArray);
        }

        @Specialization(order = 9)
        PIntegerArray doPIntegerArray(PIntegerArray left, PIntegerArray right) {
            int[] newArray = new int[left.len() + right.len()];
            int index = 0;
            int[] leftArray = left.getSequence();
            for (int i = 0; i < leftArray.length; i++) {
                newArray[index++] = leftArray[i];
            }
            int[] rightArray = right.getSequence();
            for (int i = 0; i < rightArray.length; i++) {
                newArray[index++] = rightArray[i];
            }
            return new PIntegerArray(newArray);
        }

        @Specialization(order = 10)
        PDoubleArray doPDoubleArray(PDoubleArray left, PDoubleArray right) {
            double[] newArray = new double[left.len() + right.len()];
            int index = 0;
            double[] leftArray = left.getSequence();
            for (int i = 0; i < leftArray.length; i++) {
                newArray[index++] = leftArray[i];
            }
            double[] rightArray = right.getSequence();
            for (int i = 0; i < rightArray.length; i++) {
                newArray[index++] = rightArray[i];
            }
            return new PDoubleArray(newArray);
        }

        @Specialization(order = 11)
        PCharArray doPCharArray(PCharArray left, PCharArray right) {
            char[] newArray = new char[left.len() + right.len()];
            int index = 0;
            char[] leftArray = left.getSequence();
            for (int i = 0; i < leftArray.length; i++) {
                newArray[index++] = leftArray[i];
            }
            char[] rightArray = right.getSequence();
            for (int i = 0; i < rightArray.length; i++) {
                newArray[index++] = rightArray[i];
            }
            return new PCharArray(newArray);
        }

        @SuppressWarnings("unused")
        @Generic
        Object doGeneric(Object left, Object right) {
            throw new RuntimeException("Invalid generic!");
        }

        // TODO doSequenceConcatenation

        @Override
        public void accept(PNodeVisitor visitor) {
            visitor.visitAddNode(this);
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

        @Override
        public void accept(PNodeVisitor visitor) {
            visitor.visitSubNode(this);
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

        @Generic
        Object doGeneric(Object left, Object right) {
            if (left instanceof PObject && right instanceof Integer) {
                return ((PObject) left).multiply((int) right);
            } else if (left instanceof Integer && right instanceof PObject) {
                return ((PObject) right).multiply((int) left);
            } else {
                throw new RuntimeException("Invalid generic!");
            }
        }

        @Override
        public void accept(PNodeVisitor visitor) {
            visitor.visitMulNode(this);
        }

        // TODO doSequenceRepetition
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
            // return left.div(right);
            double opNormSq = right.getReal() * right.getReal() + right.getImag() * right.getImag();
            // result.setReal(result.getReal() / opNormSq);
            // result.setImag(result.getImag() / opNormSq);

            PComplex conjugate = right.getConjugate();
            double realPart = left * conjugate.getReal();
            double imagPart = left * conjugate.getImag();
            return new PComplex(realPart / opNormSq, imagPart / opNormSq);
        }

        @Specialization(order = 4)
        PComplex doComplexDouble(PComplex left, double right) {
            // return left.div(right);
            double opNormSq = right * right;
            // result.setReal(result.getReal() / opNormSq);
            // result.setImag(result.getImag() / opNormSq);
            double realPart = left.getReal() * right;
            double imagPart = left.getImag() * right;
            return new PComplex(realPart / opNormSq, imagPart / opNormSq);
        }

        @Specialization(order = 5)
        PComplex doComplex(PComplex left, PComplex right) {
            // return left.div(right);
            double opNormSq = right.getReal() * right.getReal() + right.getImag() * right.getImag();
            // result.setReal(result.getReal() / opNormSq);
            // result.setImag(result.getImag() / opNormSq);
            PComplex conjugate = right.getConjugate();
            double realPart = left.getReal() * conjugate.getReal() - left.getImag() * conjugate.getImag();
            double imagPart = left.getReal() * conjugate.getImag() + left.getImag() * conjugate.getReal();
            return new PComplex(realPart / opNormSq, imagPart / opNormSq);
        }

        @Override
        public void accept(PNodeVisitor visitor) {
            visitor.visitDivNode(this);
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

        @Override
        public void accept(PNodeVisitor visitor) {
            visitor.visitFloorDivNode(this);
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

        @Override
        public void accept(PNodeVisitor visitor) {
            visitor.visitModuloNode(this);
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

        @Override
        public void accept(PNodeVisitor visitor) {
            visitor.visitPowerNode(this);
        }
    }

}
