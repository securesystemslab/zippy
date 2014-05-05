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

import static edu.uci.python.test.PythonTests.*;

import org.junit.*;

public class SpecialMethodTests {

    @Test
    public void __add__0() {
        String source = "class Num:\n" + //
                        "  def __init__(self, n):\n" + //
                        "    self.n = n\n" + //
                        "  def __add__(self, other):\n" + //
                        "    return Num(self.n + other.n)\n" + //
                        "  def __repr__(self):\n" + //
                        "    return self.n\n" + //
                        "" + //
                        "n0 = Num(42)\n" + //
                        "n1 = Num(1)\n" + //
                        "print(n0 + n1)\n";
        assertPrints("43\n", source);
    }

    @Test
    public void __add__1() {
        String source = "class Num:\n" + //
                        "  def __init__(self, n):\n" + //
                        "    self.n = n\n" + //
                        "  def __add__(self, other):\n" + //
                        "    return Num(self.n + other)\n" + //
                        "  def __repr__(self):\n" + //
                        "    return self.n\n" + //
                        "" + //
                        "n0 = Num(42)\n" + //
                        "print(n0 + 1)\n";
        assertPrints("43\n", source);
    }

    @Test
    public void __add__2() {
        String source = "class Num:\n" + //
                        "  def __init__(self, n):\n" + //
                        "    self.n = n\n" + //
                        "  def __radd__(self, other):\n" + //
                        "    return Num(self.n + other)\n" + //
                        "  def __repr__(self):\n" + //
                        "    return self.n\n" + //
                        "" + //
                        "n0 = Num(42)\n" + //
                        "print(1 + n0)\n";
        assertPrints("43\n", source);
    }

    @Test
    public void __add__And__rand__Polymorphic() {
        String source = "class Num:\n" + //
                        "  def __init__(self, n):\n" + //
                        "    self.n = n\n" + //
                        "  def __add__(self, other):\n" + //
                        "    return Num(self.n + other)\n" + //
                        "  def __radd__(self, other):\n" + //
                        "    return Num(self.n + other)\n" + //
                        "  def __repr__(self):\n" + //
                        "    return self.n\n" + //
                        "" + //
                        "def doAdd(left, right):\n" + //
                        "  return left + right\n" + //
                        "print(doAdd(Num(42), 1))\n" + //
                        "print(doAdd(1, Num(42)))\n";
        assertPrints("43\n43\n", source);
    }

    @Test
    public void __sub__() {
        String source = "class Num:\n" + //
                        "  def __init__(self, n):\n" + //
                        "    self.n = n\n" + //
                        "  def __sub__(self, other):\n" + //
                        "    return Num(self.n - other.n)\n" + //
                        "  def __repr__(self):\n" + //
                        "    return self.n\n" + //
                        "" + //
                        "n0 = Num(42)\n" + //
                        "n1 = Num(1)\n" + //
                        "print(n0 - n1)\n";
        assertPrints("41\n", source);
    }

    @Test
    public void __len__() {
        String source = "class Num:\n" + //
                        "  def __init__(self, n):\n" + //
                        "    self.n = n\n" + //
                        "  def __len__(self):\n" + //
                        "    return self.n\n" + //
                        "" + //
                        "n0 = Num(42)\n" + //
                        "print(len(n0))\n";
        assertPrints("42\n", source);
    }

    @Test
    public void __call__() {
        String source = "class Num:\n" + //
                        "  def __init__(self, n):\n" + //
                        "    self.n = n\n" + //
                        "  def __call__(self):\n" + //
                        "    print(self.n)\n" + //
                        "" + //
                        "n0 = Num(42)\n" + //
                        "n0()\n";
        assertPrints("42\n", source);
    }

}
