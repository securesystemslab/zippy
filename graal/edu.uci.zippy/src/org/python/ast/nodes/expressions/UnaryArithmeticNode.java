package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import org.python.ast.datatypes.PDictionary;
import org.python.ast.datatypes.PList;
import org.python.ast.datatypes.PTuple;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.Generic;
import com.oracle.truffle.api.codegen.Specialization;

public abstract class UnaryArithmeticNode extends UnaryOpNode {

    public UnaryArithmeticNode(TypedNode operand) {
        super(operand);
    }

    protected UnaryArithmeticNode(UnaryArithmeticNode node) {
        super(node);
    }

    public abstract static class PlusNode extends UnaryArithmeticNode {

        public PlusNode(TypedNode operand) {
            super(operand);
        }

        protected PlusNode(PlusNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int operand) {
            return operand;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger operand) {
            return operand;
        }

        @Specialization
        double doDouble(double operand) {
            return operand;
        }
    }

    public abstract static class MinusNode extends UnaryArithmeticNode {

        public MinusNode(TypedNode operand) {
            super(operand);
        }

        protected MinusNode(MinusNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int operand) {
            return -operand;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger operand) {
            return operand.negate();
        }

        @Specialization
        double doDouble(double operand) {
            return -operand;
        }
    }

    public abstract static class InvertNode extends UnaryArithmeticNode {

        public InvertNode(TypedNode operand) {
            super(operand);
        }

        protected InvertNode(InvertNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int operand) {
            return ~operand;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger operand) {
            return operand.not();
        }
    }

    /**
     * Please note that this is actually not an arithmetic operation, but rather
     * a unary logical operation.
     * 
     * @author zwei
     * 
     */
    public abstract static class NotNode extends UnaryArithmeticNode {

        public NotNode(TypedNode operand) {
            super(operand);
        }

        protected NotNode(NotNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int operand) {
            return operand == 0;
        }

        @Specialization
        boolean doBigInteger(BigInteger operand) {
            return operand.compareTo(BigInteger.ZERO) == 0;
        }

        @Specialization
        boolean doDouble(double operand) {
            return operand == 0;
        }

        @Specialization
        boolean doBoolean(boolean operand) {
            return !operand;
        }

        @Specialization
        boolean doString(String operand) {
            return operand.length() == 0;
        }

        @Specialization
        boolean doPTuple(PTuple operand) {
            return operand.len() == 0;
        }

        @Specialization
        boolean doPList(PList operand) {
            return operand.len() == 0;
        }

        @Specialization
        boolean doPDictionary(PDictionary operand) {
            return operand.len() == 0;
        }

        @Generic
        boolean doGeneric(Object operand) {
            // anything except for 0 and None is true
            if (operand == null) {
                return true;
            }

            return false;
        }

    }

    public abstract static class YesNode extends UnaryArithmeticNode {

        public YesNode(TypedNode operand) {
            super(operand);
        }

        protected YesNode(YesNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int operand) {
            return operand != 0;
        }

        @Specialization
        boolean doBigInteger(BigInteger operand) {
            return operand.compareTo(BigInteger.ZERO) != 0;
        }

        @Specialization
        boolean doDouble(double operand) {
            return operand != 0;
        }

        @Specialization
        boolean doBoolean(boolean operand) {
            return operand;
        }

        @Specialization
        boolean doString(String operand) {
            return operand.length() != 0;
        }

        @Specialization
        boolean doPTuple(PTuple operand) {
            return operand.len() != 0;
        }

        @Specialization
        boolean doPList(PList operand) {
            return operand.len() != 0;
        }

        @Specialization
        boolean doPDictionary(PDictionary operand) {
            return operand.len() != 0;
        }

        @Generic
        boolean doGeneric(Object operand) {
            // anything except for 0 and None is true
            if (operand != null) {
                return true;
            }

            return false;
        }
    }

}
