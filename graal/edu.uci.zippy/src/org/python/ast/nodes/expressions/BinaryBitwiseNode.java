package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import org.python.ast.datatypes.PBaseSet;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class BinaryBitwiseNode extends BinaryOpNode {

    public BinaryBitwiseNode(TypedNode left, TypedNode right) {
        super(left, right);
    }

    protected BinaryBitwiseNode(BinaryBitwiseNode node) {
        super(node);
    }

    public abstract static class LeftShiftNode extends BinaryBitwiseNode {

        public LeftShiftNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected LeftShiftNode(LeftShiftNode node) {
            super(node);
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        int doInteger(int left, int right) {
            return ArithmeticUtil.leftShiftExact(left, right);
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            // Right operand may lose precision, but it is harmless for a left
            // shift.
            return left.shiftLeft(right.intValue());
        }
    }

    public abstract static class RightShiftNode extends BinaryBitwiseNode {

        public RightShiftNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected RightShiftNode(RightShiftNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int left, int right) {
            return left >> right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            // Right operand may lose precision, but it is harmless for a right
            // shift.
            return left.shiftRight(right.intValue());
        }
    }

    public abstract static class BitAndNode extends BinaryBitwiseNode {

        public BitAndNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected BitAndNode(BitAndNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int left, int right) {
            return left & right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.and(right);
        }

        @Specialization
        PBaseSet doPBaseSet(PBaseSet left, PBaseSet right) {
            return left.intersection(right);
        }

    }

    public abstract static class BitXorNode extends BinaryBitwiseNode {

        public BitXorNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected BitXorNode(BitXorNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int left, int right) {
            return left ^ right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.xor(right);
        }
    }

    public abstract static class BitOrNode extends BinaryBitwiseNode {

        public BitOrNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected BitOrNode(BitOrNode node) {
            super(node);
        }

        @Specialization
        int doInteger(int left, int right) {
            return left | right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.or(right);
        }
    }

}
