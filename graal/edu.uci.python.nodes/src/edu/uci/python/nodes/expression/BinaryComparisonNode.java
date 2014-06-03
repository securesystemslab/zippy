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

import java.math.*;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.Specialization;

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

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
            return left.equals(right);
        }

        @Specialization
        boolean doPDict(PDict left, PDict right) {
            return left.equals(right);
        }

        @Specialization
        public boolean doPythonClass(PythonClass left, PythonClass right) {
            return left.equals(right);
        }

        @SuppressWarnings("unused")
        @Specialization
        public boolean doNone(Object left, PNone none) {
            return left == PNone.NONE;
        }

        /**
         * This is a fix for comparisons involving a PyInteger.
         *
         * @param left
         * @param right
         * @return comparison result
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

        @Specialization
        boolean doPList(PList left, PList right) {
            return !left.equals(right);
        }

        @Specialization
        boolean doPDict(PDict left, PDict right) {
            return !left.equals(right);
        }

        @Specialization
        public boolean doPythonClass(PythonClass left, PythonClass right) {
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

        @Specialization(order = 3)
        boolean doInteger(int left, int right) {
            return left == right;
        }

        @Specialization(order = 4)
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) == 0;
        }

        @Specialization(order = 5)
        boolean doDouble(double left, double right) {
            return left == right;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 10)
        boolean doLeftPNone(PNone left, Object right) {
            return PNone.NONE == right;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 11)
        boolean doRightPNone(Object left, PNone right) {
            return left == PNone.NONE;
        }

        @Generic
        public boolean doGeneric(Object left, Object right) {
            return left.equals(right);
        }
    }

    public abstract static class IsNotNode extends BinaryComparisonNode {

        @Specialization(order = 1)
        boolean doInteger(int left, int right) {
            return left != right;
        }

        @Specialization(order = 2)
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) != 0;
        }

        @Specialization(order = 3)
        boolean doDouble(double left, double right) {
            return left != right;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 10)
        public boolean doLeftPNone(PNone left, Object right) {
            return PNone.NONE != right;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 11)
        public boolean doRightPNone(Object left, PNone right) {
            return left != PNone.NONE;
        }

        @Generic
        public boolean doGeneric(Object left, Object right) {
            return !left.equals(right);
        }
    }

    public abstract static class InNode extends BinaryComparisonNode {

        @Specialization
        public boolean doBaseSet(Object left, PBaseSet right) {
            return right.contains(left);
        }

        @Specialization
        public boolean doPSequence(Object left, PSequence right) {
            return right.index(left) != -1;
        }

        @Specialization
        public boolean doPDictionary(Object left, PDict right) {
            return right.hasKey(left);
        }
    }

    public abstract static class NotInNode extends BinaryComparisonNode {

        @Specialization
        public boolean doBaseSet(Object left, PBaseSet right) {
            return !right.contains(left);
        }

        @Specialization
        public boolean doPSequence(Object left, PSequence right) {
            return right.index(left) == -1;
        }

        @Specialization
        public boolean doPDictionary(Object left, PDict right) {
            return !right.hasKey(left);
        }
    }

}
