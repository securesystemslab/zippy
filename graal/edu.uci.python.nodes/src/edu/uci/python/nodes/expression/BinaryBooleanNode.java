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

import static edu.uci.python.runtime.ArithmeticUtil.*;

public abstract class BinaryBooleanNode extends BinaryOpNode {

    public abstract static class AndNode extends BinaryBooleanNode {

        @Specialization(order = 0)
        boolean doBoolean(boolean left, boolean right) {
            return left && right;
        }

        @Specialization(order = 1)
        int doInteger(int left, int right) {
            return isZero(left) ? left : right;
        }

        @Specialization(order = 2)
        BigInteger doBitInteger(BigInteger left, BigInteger right) {
            return isZero(left) ? left : right;
        }

        @Specialization(order = 3)
        double doDouble(double left, double right) {
            return isZero(left) ? left : right;
        }
    }

    public abstract static class OrNode extends BinaryBooleanNode {

        @Specialization(order = 0)
        boolean doBoolean(boolean left, boolean right) {
            return left || right;
        }

        @Specialization(order = 1)
        int doInteger(int left, int right) {
            return isNotZero(left) ? left : right;
        }

        @Specialization(order = 2)
        BigInteger doBitInteger(BigInteger left, BigInteger right) {
            return isNotZero(left) ? left : right;
        }

        @Specialization(order = 3)
        double doDouble(double left, double right) {
            return isNotZero(left) ? left : right;
        }
    }

}
