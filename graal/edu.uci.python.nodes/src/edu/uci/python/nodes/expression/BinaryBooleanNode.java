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

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.expression.CastToBooleanNode.YesNode;
import edu.uci.python.nodes.expression.CastToBooleanNodeFactory.YesNodeFactory;
import edu.uci.python.runtime.misc.*;
import static edu.uci.python.runtime.ArithmeticUtil.*;

public abstract class BinaryBooleanNode extends BinaryOpNode {

    public abstract static class AndNode extends BinaryBooleanNode {

        @Child protected YesNode booleanCast;

        public AndNode() {
            this.booleanCast = YesNodeFactory.create(EmptyNode.create());
        }

        protected AndNode(@SuppressWarnings("unused") AndNode prev) {
            this();
        }

        @ShortCircuit("rightNode")
        public boolean needsRightNode(VirtualFrame frame, Object left) {
            return booleanCast.executeBoolean(frame, left);
        }

        @Specialization(order = 0)
        boolean doBoolean(boolean left, boolean hasRight, boolean right) {
            return hasRight ? right : left;
        }

        @Specialization(order = 1)
        public int doInteger(int left, boolean hasRight, int right) {
            return hasRight ? right : left;
        }

        @Specialization(order = 2)
        public BigInteger doBigInteger(BigInteger left, boolean hasRight, BigInteger right) {
            return hasRight ? right : left;
        }

        @Specialization(order = 3)
        public double doDouble(double left, boolean hasRight, double right) {
            return hasRight ? right : left;
        }
    }

    public abstract static class OrNode extends BinaryBooleanNode {

        @ShortCircuit("rightNode")
        public boolean needsRightNode(boolean left) {
            return !left;
        }

        @ShortCircuit("rightNode")
        public boolean needsRightNode(int left) {
            return isZero(left);
        }

        @ShortCircuit("rightNode")
        public boolean needsRightNode(BigInteger left) {
            return isZero(left);
        }

        @ShortCircuit("rightNode")
        public boolean needsRightNode(double left) {
            return isZero(left);
        }

        @ShortCircuit("rightNode")
        public boolean needsRightNode(Object left) {
            return !JavaTypeConversions.toBoolean(left);
        }

        @Specialization(order = 0)
        public boolean doBoolean(boolean left, boolean hasRight, boolean right) {
            return hasRight ? right : left;
        }

        @Specialization(order = 1)
        public int doInteger(int left, boolean hasRight, int right) {
            return hasRight ? right : left;
        }

        @Specialization(order = 2)
        public BigInteger doBigInteger(BigInteger left, boolean hasRight, BigInteger right) {
            return hasRight ? right : left;
        }

        @Specialization(order = 3)
        public double doDouble(double left, boolean hasRight, double right) {
            return hasRight ? right : left;
        }
    }

}
