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

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;
import edu.uci.python.runtime.standardtype.*;

@GenerateNodeFactory
public abstract class BinaryComparisonNode extends BinaryOpNode {

    @NodeInfo(shortName = "==")
    @GenerateNodeFactory
    public abstract static class EqualNode extends BinaryComparisonNode {

        @Specialization(order = 0)
        boolean doBoolean(boolean left, boolean right) {
            return left == right;
        }

        @Specialization(order = 5)
        boolean doInteger(int left, int right) {
            return left == right;
        }

        @Specialization(order = 10)
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.equals(right);
        }

        @Specialization(order = 15)
        boolean doDouble(double left, double right) {
            return left == right;
        }

        @Specialization(order = 20)
        boolean doComplex(PComplex left, PComplex right) {
            return left.equals(right);
        }

        @Specialization(order = 21)
        boolean doChar(char left, char right) {
            return left == right;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 22)
        boolean doIntString(int left, String right) {
            return false;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 23)
        boolean doIntString(String left, int right) {
            return false;
        }

        @Specialization(order = 25)
        boolean doString(String left, String right) {
            return left.equals(right);
        }

        @Specialization(order = 30)
        boolean doPTuple(PTuple left, PTuple right) {
            return left.equals(right);
        }

        @Specialization(order = 36, guards = "areBothIntStorage(left,right)")
        boolean doPListInt(PList left, PList right) {
            IntSequenceStorage leftStore = (IntSequenceStorage) left.getStorage();
            IntSequenceStorage rightStore = (IntSequenceStorage) right.getStorage();
            return leftStore.equals(rightStore);
        }

        @Specialization(order = 37, guards = "areBothObjectStorage(left,right)")
        boolean doPListObject(PList left, PList right) {
            ObjectSequenceStorage leftStore = (ObjectSequenceStorage) left.getStorage();
            ObjectSequenceStorage rightStore = (ObjectSequenceStorage) right.getStorage();
            return leftStore.equals(rightStore);
        }

        @Specialization(order = 40)
        boolean doPList(PList left, PList right) {
            return left.equals(right);
        }

        @Specialization(order = 50)
        boolean doPDict(PDict left, PDict right) {
            return left.equals(right);
        }

        @Specialization(order = 60)
        public boolean doPythonClass(PythonClass left, PythonClass right) {
            return left == right;
        }

        @Specialization(order = 70)
        Object doPythonObject(VirtualFrame frame, PythonObject left, PythonObject right) {
            return doSpecialMethodCall(frame, "__eq__", left, right);
        }

        @SuppressWarnings("unused")
        @Specialization(order = 100, guards = "isNotPythonObject(left)")
        Object doPythonObject(Object left, PythonObject right) {
            return false;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 110, guards = "is2ndNotPythonObject(left,right)")
        Object doPythonObject(PythonObject left, Object right) {
            return false;
        }

        /**
         * This is a fix for comparisons involving a PyInteger.
         */
        @Fallback
        public boolean doGeneric(Object left, Object right) {
            return left.equals(right);
        }
    }

    @NodeInfo(shortName = "!=")
    @GenerateNodeFactory
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

        @SuppressWarnings("unused")
        @Specialization
        public boolean doObjectNone(PythonObject left, PNone right) {
            return true;
        }

        @Specialization
        public boolean doPythonClass(PythonClass left, PythonClass right) {
            return left != right;
        }

        @Specialization
        public boolean doPythonObject(PythonObject left, PythonObject right) {
            return left != right;
        }
    }

    @NodeInfo(shortName = "<")
    @GenerateNodeFactory
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

    @NodeInfo(shortName = "<=")
    @GenerateNodeFactory
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

    @NodeInfo(shortName = ">")
    @GenerateNodeFactory
    public abstract static class GreaterThanNode extends BinaryComparisonNode {

        @Specialization(order = 1)
        boolean doInteger(int left, int right) {
            return left > right;
        }

        @Specialization(order = 5)
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) > 0;
        }

        @Specialization(order = 10)
        boolean doIntDouble(int left, double right) {
            return left > right;
        }

        @Specialization(order = 11)
        boolean doIntDouble(double left, int right) {
            return left > right;
        }

        @Specialization(order = 15)
        boolean doDouble(double left, double right) {
            return left > right;
        }

        @Specialization(order = 20)
        boolean doComplex(PComplex left, PComplex right) {
            return left.greaterThan(right);
        }

        @Specialization(order = 30)
        boolean doString(String left, String right) {
            return left.compareTo(right) > 0;
        }
    }

    @NodeInfo(shortName = ">=")
    @GenerateNodeFactory
    public abstract static class GreaterThanEqualNode extends BinaryComparisonNode {

        @Specialization(order = 1)
        boolean doInteger(int left, int right) {
            return left >= right;
        }

        @Specialization(order = 5)
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) >= 0;
        }

        @Specialization(order = 10)
        boolean doIntDouble(int left, double right) {
            return left >= right;
        }

        @Specialization(order = 15)
        boolean doDouble(double left, double right) {
            return left >= right;
        }

        @Specialization(order = 20)
        boolean doComplex(PComplex left, PComplex right) {
            return left.greaterEqual(right);
        }

        @Specialization(order = 30)
        boolean doString(String left, String right) {
            return left.compareTo(right) >= 0;
        }

        @Specialization(order = 40)
        boolean doTuple(PTuple left, PTuple right) {
            return left.compareTo(right) >= 0;
        }
    }

    @NodeInfo(shortName = "is")
    @GenerateNodeFactory
    public abstract static class IsNode extends BinaryComparisonNode {

        @SuppressWarnings("unused")
        @Specialization(order = 1)
        boolean doInteger(int left, boolean right) {
            return false;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 2)
        boolean doInteger(boolean left, int right) {
            return false;
        }

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
            return PNone.NONENode == right || PNone.NONE == right;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 11)
        boolean doRightPNone(Object left, PNone right) {
            return left == PNone.NONENode || left == PNone.NONE;
        }

        @Fallback
        public boolean doGeneric(Object left, Object right) {
            return left.equals(right);
        }
    }

    @NodeInfo(shortName = "is not")
    @GenerateNodeFactory
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
            return PNone.NONENode != right && PNone.NONE != right;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 11)
        public boolean doRightPNone(Object left, PNone right) {
            return left != PNone.NONENode && left != PNone.NONE;
        }

        @Fallback
        public boolean doGeneric(Object left, Object right) {
            return !left.equals(right);
        }
    }

    @NodeInfo(shortName = "in")
    @GenerateNodeFactory
    public abstract static class InNode extends BinaryComparisonNode {

        @Specialization
        public boolean doString(String left, String right) {
            return right.contains(left);
        }

        @Specialization
        public boolean doBaseSet(Object left, PBaseSet right) {
            return right.contains(left);
        }

        @Specialization
        public boolean doPSequence(Object left, PSequence right) {
            return right.index(left) != -1;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 10, guards = "isEmptyDict(left,right)")
        public boolean doPDictionaryEmpty(Object left, PDict right) {
            return false;
        }

        @Specialization(order = 11)
        public boolean doPDictionary(Object left, PDict right) {
            return right.hasKey(left);
        }

        protected static boolean isEmptyDict(@SuppressWarnings("unused") Object first, PDict dict) {
            return dict.len() == 0;
        }
    }

    @NodeInfo(shortName = "not in")
    @GenerateNodeFactory
    public abstract static class NotInNode extends BinaryComparisonNode {

        @Specialization(order = 5)
        public boolean doBaseSet(Object left, PBaseSet right) {
            return !right.contains(left);
        }

        @SuppressWarnings("unused")
        @Specialization(order = 10, guards = "is2ndEmptyStorage(left,right)")
        public boolean doPListEmpty(Object left, PList right) {
            return true;
        }

        @Specialization(order = 11, guards = "is2ndIntStorage(left,right)")
        public boolean doPListInt(int left, PList right) {
            IntSequenceStorage store = (IntSequenceStorage) right.getStorage();
            return store.indexOfInt(left) == -1;
        }

        @Specialization(order = 12, guards = "is2ndDoubleStorage(left,right)")
        public boolean doPListDouble(double left, PList right) {
            DoubleSequenceStorage store = (DoubleSequenceStorage) right.getStorage();
            return store.indexOfDouble(left) == -1;
        }

        @Specialization(order = 13, guards = "is2ndObjectStorage(left,right)")
        public boolean doPListObject(Object left, PList right) {
            ObjectSequenceStorage store = (ObjectSequenceStorage) right.getStorage();
            return store.index(left) == -1;
        }

        @Specialization(order = 15)
        public boolean doPList(Object left, PList right) {
            return right.index(left) == -1;
        }

        @Specialization(order = 19)
        public boolean doPSequence(Object left, PSequence right) {
            return right.index(left) == -1;
        }

        @Specialization(order = 20)
        public boolean doPDictionary(Object left, PDict right) {
            return !right.hasKey(left);
        }
    }

}
