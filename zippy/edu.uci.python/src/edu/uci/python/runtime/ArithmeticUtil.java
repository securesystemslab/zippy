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
package edu.uci.python.runtime;

import java.math.BigInteger;

import org.python.core.Py;

import com.oracle.truffle.api.*;

public class ArithmeticUtil {

    public static int leftShiftExact(int left, int right) {
        if (right >= Integer.SIZE || right < 0) {
            shiftError(right);
        }

        int result = left << right;

        if (left != result >> right) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new ArithmeticException("integer overflow");
        }

        return result;
    }

    public static int rightShiftExact(int left, int right) {
        if (right >= Integer.SIZE || right < 0) {
            shiftError(right);
        }

        int result = left >> right;

        if (left != result << right) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new ArithmeticException("integer overflow");
        }

        return result;
    }

    private static void shiftError(int shiftCount) {
        CompilerDirectives.transferToInterpreterAndInvalidate();

        if (shiftCount >= Integer.SIZE) {
            throw new ArithmeticException("integer overflow");
        } else if (shiftCount < 0) {
            throw Py.ValueError("negative shift count");
        }
    }

    public static boolean isZero(int value) {
        return value == 0;
    }

    public static boolean isZero(BigInteger value) {
        return value.compareTo(BigInteger.ZERO) == 0;
    }

    public static boolean isZero(double value) {
        return value == 0;
    }

    public static boolean isNotZero(int value) {
        return value != 0;
    }

    public static boolean isNotZero(BigInteger value) {
        return value.compareTo(BigInteger.ZERO) != 0;
    }

    public static boolean isNotZero(double value) {
        return value != 0;
    }

}
