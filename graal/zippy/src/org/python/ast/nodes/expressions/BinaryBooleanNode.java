package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.Specialization;

import static org.python.ast.nodes.expressions.ArithmeticUtil.*;

public abstract class BinaryBooleanNode extends BinaryOpNode {

    public BinaryBooleanNode(TypedNode left, TypedNode right) {
        super(left, right);
    }

    protected BinaryBooleanNode(BinaryBooleanNode node) {
        super(node);
    }

    public abstract static class AndNode extends BinaryBooleanNode {

        public AndNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected AndNode(AndNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int left, int right) {
            return isZero(left) ? left : right;
        }

        @Specialization
        BigInteger doBitInteger(BigInteger left, BigInteger right) {
            return isZero(left) ? left : right;
        }

        @Specialization
        double doDouble(double left, double right) {
            return isZero(left) ? left : right;
        }

        @Specialization
        boolean doBoolean(boolean left, boolean right) {
            return left && right;
        }
    }

    public abstract static class OrNode extends BinaryBooleanNode {

        public OrNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected OrNode(OrNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int left, int right) {
            return isNotZero(left) ? left : right;
        }

        @Specialization
        BigInteger doBitInteger(BigInteger left, BigInteger right) {
            return isNotZero(left) ? left : right;
        }

        @Specialization
        double doDouble(double left, double right) {
            return isNotZero(left) ? left : right;
        }

        @Specialization
        boolean doBoolean(boolean left, boolean right) {
            return left || right;
        }
    }

}