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
package edu.uci.python.test.grammar;

import static edu.uci.python.test.PythonTests.*;

import org.junit.*;

public class CallTests {

    @Test
    public void simple() {
        String source = "def foo():\n" + //
                        "  return 1\n" + //
                        "a = foo() - foo()\n" + //
                        "print(a)\n";
        assertPrints("0\n", source);
    }

    @Test
    public void defaultArgs() {
        String source = "foo = 1\n" + //
                        "def bar(f=foo):\n" + //
                        "    print(f)\n" + //
                        "bar()\n" + //
                        "foo = 2\n" + //
                        "bar(4)\n";

        assertPrints("1\n4\n", source);
    }

    @Test
    public void classFunction() {
        String source = "class Foo:\n" + //
                        "  def __init__(self, num):\n" + //
                        "    self.num = num\n" + //
                        "  def func(self):\n" + //
                        "    print(self.num)\n" + //
                        "\n" + //
                        "foo = Foo(42)\n" + //
                        "Foo.func(foo)\n";

        assertPrints("42\n", source);
    }

    @Test
    public void objectMethod() {
        String source = "class Foo:\n" + //
                        "  def __init__(self, num):\n" + //
                        "    self.num = num\n" + //
                        "  def func(self):\n" + //
                        "    print(self.num)\n" + //
                        "\n" + //
                        "foo = Foo(42)\n" + //
                        "foo.func()\n";

        assertPrints("42\n", source);
    }

}
