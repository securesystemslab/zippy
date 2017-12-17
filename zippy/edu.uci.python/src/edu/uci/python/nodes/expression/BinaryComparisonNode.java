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

import edu.uci.python.ast.VisitorIF;
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

        @Specialization
        boolean doBoolean(boolean left, boolean right) {
            return left == right;
        }

        @Specialization
        boolean doInteger(int left, int right) {
            return left == right;
        }

        @Specialization
        boolean doLong(long left, long right) {
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
        boolean doChar(char left, char right) {
            return left == right;
        }

        @SuppressWarnings("unused")
        @Specialization
        boolean doIntString(int left, String right) {
            return false;
        }

        @SuppressWarnings("unused")
        @Specialization
        boolean doIntString(String left, int right) {
            return false;
        }

        @Specialization
        boolean doString(String left, String right) {
            return left.equals(right);
        }

        @Specialization
        boolean doPTuple(PTuple left, PTuple right) {
            return left.equals(right);
        }

        @Specialization(guards = "areBothIntStorage(left,right)")
        boolean doPListInt(PList left, PList right) {
            IntSequenceStorage leftStore = (IntSequenceStorage) left.getStorage();
            IntSequenceStorage rightStore = (IntSequenceStorage) right.getStorage();
            return leftStore.equals(rightStore);
        }

        @Specialization(guards = "areBothObjectStorage(left,right)")
        boolean doPListObject(PList left, PList right) {
            ObjectSequenceStorage leftStore = (ObjectSequenceStorage) left.getStorage();
            ObjectSequenceStorage rightStore = (ObjectSequenceStorage) right.getStorage();
            return leftStore.equals(rightStore);
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
            return left == right;
        }

        @Specialization
        Object doPythonObject(VirtualFrame frame, PythonObject left, PythonObject right) {
            return doSpecialMethodCall(frame, "__eq__", left, right);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "isNotPythonObject(left)")
        Object doPythonObject(Object left, PythonObject right) {
            return false;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "is2ndNotPythonObject(left,right)")
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
        boolean doLong(long left, long right) {
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
        boolean doLong(long left, long right) {
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
        boolean doLong(long left, long right) {
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

        @Specialization()
        boolean doInteger(int left, int right) {
            return left > right;
        }

        @Specialization
        boolean doLong(long left, long right) {
            return left > right;
        }

        @Specialization()
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) > 0;
        }

        @Specialization
        boolean doIntDouble(int left, double right) {
            return left > right;
        }

        @Specialization
        boolean doIntDouble(double left, int right) {
            return left > right;
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

    @NodeInfo(shortName = ">=")
    @GenerateNodeFactory
    public abstract static class GreaterThanEqualNode extends BinaryComparisonNode {

        @Specialization()
        boolean doInteger(int left, int right) {
            return left >= right;
        }

        @Specialization
        boolean doLong(long left, long right) {
            return left >= right;
        }

        @Specialization()
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) >= 0;
        }

        @Specialization
        boolean doIntDouble(int left, double right) {
            return left >= right;
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

        @Specialization
        boolean doTuple(PTuple left, PTuple right) {
            return left.compareTo(right) >= 0;
        }
    }

    @NodeInfo(shortName = "is")
    @GenerateNodeFactory
    public abstract static class IsNode extends BinaryComparisonNode {

        @SuppressWarnings("unused")
        @Specialization()
        boolean doInteger(int left, boolean right) {
            return false;
        }

        @SuppressWarnings("unused")
        @Specialization()
        boolean doInteger(boolean left, int right) {
            return false;
        }

        @Specialization()
        boolean doInteger(int left, int right) {
            return left == right;
        }

        @Specialization
        boolean doLong(long left, long right) {
            return left == right;
        }

        @Specialization()
        boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) == 0;
        }

        @Specialization()
        boolean doDouble(double left, double right) {
            return left == right;
        }

        @SuppressWarnings("unused")
        @Specialization
        boolean doLeftPNone(PNone left, Object right) {
            return PNone.NONENode == right || PNone.NONE == right;
        }

        @SuppressWarnings("unused")
        @Specialization
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

        @Specialization
        boolean doInteger(int left, int right) {
            return left != right;
        }

        @Specialization
        boolean doLong(long left, long right) {
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

        @SuppressWarnings("unused")
        @Specialization
        public boolean doLeftPNone(PNone left, Object right) {
            return PNone.NONENode != right && PNone.NONE != right;
        }

        @SuppressWarnings("unused")
        @Specialization
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
        @Specialization(guards = "isEmptyDict(left,right)")
        public boolean doPDictionaryEmpty(Object left, PDict right) {
            return false;
        }

        @Specialization
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

        @Specialization
        public boolean doBaseSet(Object left, PBaseSet right) {
            return !right.contains(left);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "is2ndEmptyStorage(left,right)")
        public boolean doPListEmpty(Object left, PList right) {
            return true;
        }

        @Specialization(guards = "is2ndIntStorage(left,right)")
        public boolean doPListInt(int left, PList right) {
            IntSequenceStorage store = (IntSequenceStorage) right.getStorage();
            return store.indexOfInt(left) == -1;
        }

        @Specialization(guards = "is2ndDoubleStorage(left,right)")
        public boolean doPListDouble(double left, PList right) {
            DoubleSequenceStorage store = (DoubleSequenceStorage) right.getStorage();
            return store.indexOfDouble(left) == -1;
        }

        @Specialization(guards = "is2ndObjectStorage(left,right)")
        public boolean doPListObject(Object left, PList right) {
            ObjectSequenceStorage store = (ObjectSequenceStorage) right.getStorage();
            return store.index(left) == -1;
        }

        @Specialization
        public boolean doPList(Object left, PList right) {
            return right.index(left) == -1;
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

    @Override
    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitBinaryComparisonNode(this);
    }

}
