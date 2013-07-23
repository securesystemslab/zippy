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
package org.python.ast.nodes.expressions;

import java.math.*;
import java.util.*;

import org.python.ast.datatypes.*;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class BinaryComparisonNode extends BinaryOpNode {

    public abstract static class EqualNode extends BinaryComparisonNode {

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

    public abstract static class GreaterThanEqualNode extends BinaryComparisonNode {

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
            return right.hasKey(new Object[]{left});
        }

    }

}
