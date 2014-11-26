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

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.sequence.*;

public abstract class BinaryBitwiseNode extends BinaryOpNode {

    @NodeInfo(shortName = "<<")
    public abstract static class LeftShiftNode extends BinaryBitwiseNode {

        @Specialization(rewriteOn = ArithmeticException.class)
        int doInteger(int left, int right) {
            return ArithmeticUtil.leftShiftExact(left, right);
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, int right) {
            return left.shiftLeft(right);
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            /**
             * Right operand may lose precision, but it is harmless for a left shift.
             */
            return left.shiftLeft(right.intValue());
        }
    }

    @NodeInfo(shortName = ">>")
    public abstract static class RightShiftNode extends BinaryBitwiseNode {

        @Specialization
        int doInteger(int left, int right) {
            return left >> right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, int right) {
            return left.shiftRight(right);
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            /**
             * Right operand may lose precision.
             */
            return left.shiftRight(right.intValue());
        }
    }

    @NodeInfo(shortName = "&")
    public abstract static class BitAndNode extends BinaryBitwiseNode {

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

    @NodeInfo(shortName = "^")
    public abstract static class BitXorNode extends BinaryBitwiseNode {

        @Specialization
        int doInteger(int left, int right) {
            return left ^ right;
        }

        @Specialization
        BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.xor(right);
        }
    }

    @NodeInfo(shortName = "|")
    public abstract static class BitOrNode extends BinaryBitwiseNode {

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
