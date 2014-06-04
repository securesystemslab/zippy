/*
 * Copyright (c) 2014, Regents of the University of California
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
package edu.uci.python.test;

import java.io.*;
import java.math.*;
import edu.uci.python.runtime.misc.*;

public class BigIntTests {

    private static void bigIntegerDiv() {
        BigInteger b1 = new BigInteger("987654321987654321000000000");
        BigInteger b2 = new BigInteger("987654321");

        long start = System.nanoTime();
        BigInteger division = null;
        for (int i = 0; i < 100000000; i++) {
            division = b1.divide(b2);
        }

        PrintStream ps = System.out;
        ps.println("division = " + division);  // prints 1
        long duration = System.nanoTime() - start;
        ps.println("time = " + (double) duration / 1000000000);
    }

    private static void bigIntDiv() {
        BigInt b1 = BigInt.valueOf("987654321987654321000000000");
        BigInt b2 = BigInt.valueOf("987654321");

        long start = System.nanoTime();
        BigInt division = null;
        for (int i = 0; i < 100000000; i++) {
            division = BigInt.quotient(b1, b2);
        }

        PrintStream ps = System.out;
        ps.println("division = " + division);  // prints 1
        long duration = System.nanoTime() - start;
        ps.println("time = " + (double) duration / 1000000000);
    }

    public static void main(String[] args) {
        bigIntegerDiv();
        bigIntDiv();
    }

}
