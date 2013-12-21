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
package edu.uci.python.test;

import static edu.uci.python.test.PythonTests.*;

import java.nio.file.*;

import org.junit.*;

public class BuiltinTests {

// @Test
    public void builtins() {
        Path script = Paths.get("builtins_test.py");
        assertPrints("False\nTrue\n10\n10.25\n2.23606797749979\nTrue\nFalse\nFalse\nTrue\n" + "True\nFalse\nA\n(2+3j)\n(3.4+4.9j)\n(2+0j)\n0j\n(0, 1000)\n(1, 2000)\n(2, 3000)\n2.0\n"
                        + "1.23\n-12345.0\n0.001\n1000000.0\n0.0\n3\n2\n4\n2147483648\n0\n5\n3\n4\n2\n"
                        + "20\n20.8\n[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]\n[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]\n[0, 5, 10, 15, 20, 25]\n" + "h\ne\nl\nl\no\n10\n20\n30\nTrue\n", script);
    }

    @Test
    public void builtin_call() {
        Path script = Paths.get("builtin_call_test.py");
        assertPrints("42\n", script);
    }

}
