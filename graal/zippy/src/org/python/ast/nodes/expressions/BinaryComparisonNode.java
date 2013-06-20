package org.python.ast.nodes.expressions;

import java.math.*;
import java.util.*;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class BinaryComparisonNode extends BinaryOpNode {

    public BinaryComparisonNode(TypedNode left, TypedNode right) {
        super(left, right);
    }

    protected BinaryComparisonNode(BinaryComparisonNode node) {
        super(node);
    }

    public abstract static class EqualNode extends BinaryComparisonNode {

        public EqualNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected EqualNode(EqualNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left == right;
        }

        @Specialization
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.equals(right);
        }

        @Specialization
        boolean doDouble(double left, double right) {
            return left == right;
        }

        @Specialization
        boolean doComplex(PComplex left, PComplex right) {
            return left.equals(right);
        }

        @Specialization
        boolean doString(String left, String right) {
            return left.equals(right);
        }

        @Specialization
        boolean doPList(PList left, PList right) {
            List<Object> llist = left.getList();
            List<Object> rlist = right.getList();

            if (llist.size() != rlist.size()) {
                return false;
            }

            for (int i = 0; i < llist.size(); i++) {
                Object l = llist.get(i);
                Object r = rlist.get(i);
                boolean isTheSame = ArithmeticUtil.is(l, r);

                if (!isTheSame) {
                    return false;
                }
            }

            return true;
        }

        /**
         * This is a fix for comparisons involving a PyInteger
         * 
         * @param left
         * @param right
         * @return
         */
        @Generic
        public boolean doGeneric(Object left, Object right) {
            return left.equals(right);
        }
    }

    public abstract static class NotEqualNode extends BinaryComparisonNode {

        public NotEqualNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected NotEqualNode(NotEqualNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left != right;
        }

        @Specialization
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return !left.equals(right);
        }

        @Specialization
        boolean doDouble(double left, double right) {
            return left != right;
        }

        @Specialization
        boolean doComplex(PComplex left, PComplex right) {
            return left.notEqual(right);
        }

        @Specialization
        boolean doString(String left, String right) {
            return !left.equals(right);
        }
    }

    public abstract static class LessThanNode extends BinaryComparisonNode {

        public LessThanNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected LessThanNode(LessThanNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left < right;
        }

        @Specialization
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) < 0;
        }

        @Specialization
        boolean doDouble(double left, double right) {
            return left < right;
        }

        @Specialization
        boolean doComplex(PComplex left, PComplex right) {
            return left.lessThan(right);
        }

        @Specialization
        boolean doString(String left, String right) {
            return left.compareTo(right) < 0;
        }

        @Specialization
        boolean doTruffleSequence(PSequence left, PSequence right) {
            return left.lessThan(right);
        }
    }

    public abstract static class LessThanEqualNode extends BinaryComparisonNode {

        public LessThanEqualNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected LessThanEqualNode(LessThanEqualNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left <= right;
        }

        @Specialization
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) <= 0;
        }

        @Specialization
        boolean doDouble(double left, double right) {
            return left <= right;
        }

        @Specialization
        boolean doComplex(PComplex left, PComplex right) {
            return left.lessEqual(right);
        }

        @Specialization
        boolean doString(String left, String right) {
            return left.compareTo(right) <= 0;
        }

        @Specialization
        boolean doPBaseSet(PBaseSet left, PBaseSet right) {
            return left.isSubset(right);
        }

    }

    public abstract static class GreaterThanNode extends BinaryComparisonNode {

        public GreaterThanNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected GreaterThanNode(GreaterThanNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left > right;
        }

        @Specialization
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) > 0;
        }

        @Specialization
        boolean doDouble(double left, double right) {
            return left > right;
        }

        @Specialization
        boolean doComplex(PComplex left, PComplex right) {
            return left.greaterThan(right);
        }

        @Specialization
        boolean doString(String left, String right) {
            return left.compareTo(right) > 0;
        }
    }

    public abstract static class GreaterThanEqualNode extends
            BinaryComparisonNode {

        public GreaterThanEqualNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected GreaterThanEqualNode(GreaterThanEqualNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left >= right;
        }

        @Specialization
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) >= 0;
        }

        @Specialization
        boolean doDouble(double left, double right) {
            return left >= right;
        }

        @Specialization
        boolean doComplex(PComplex left, PComplex right) {
            return left.greaterEqual(right);
        }

        @Specialization
        boolean doString(String left, String right) {
            return left.compareTo(right) >= 0;
        }
    }

    public abstract static class IsNode extends BinaryComparisonNode {

        public IsNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected IsNode(IsNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left == right;
        }

        @Specialization
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) == 0;
        }

        @Specialization
        boolean doDouble(double left, double right) {
            return left == right;
        }

        @Generic
        public boolean doGeneric(Object left, Object right) {
            return left.equals(right);
        }

    }

    public abstract static class IsNotNode extends BinaryComparisonNode {

        public IsNotNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected IsNotNode(IsNotNode node) {
            super(node);
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left != right;
        }

        @Specialization
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) != 0;
        }

        @Specialization
        boolean doDouble(double left, double right) {
            return left != right;
        }

        @Generic
        public boolean doGeneric(Object left, Object right) {
            return !left.equals(right);
        }

    }

    public abstract static class InNode extends BinaryComparisonNode {

        public InNode(TypedNode left, TypedNode right) {
            super(left, right);
        }

        protected InNode(InNode node) {
            super(node);
        }

        @Specialization
        public boolean doPSequence(Object left, PSequence right) {
            boolean has = false;
            Iterator<?> iter = right.iterator();

            while (iter.hasNext()) {
                Object item = iter.next();
                boolean equals = ArithmeticUtil.is(left, item);

                if (equals) {
                    has = true;
                    break;
                }
            }

            return has;
        }

        @Specialization
        public boolean doPDictionary(Object left, PDictionary right) {
            return right.hasKey(new Object[] { left });
        }

    }

}
