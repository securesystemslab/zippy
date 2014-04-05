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

import org.junit.*;
import static edu.uci.python.test.PythonTests.*;

public class ClassTests {

    @Test
    public void emptyClass() {
        String source = "class Foo:\n" + //
                        "    pass\n";

        assertPrints("", source);
    }

    @Test
    public void simpleClass() {
        String source = "class Foo:\n" + //
                        "    def __init__(self, num):\n" + //
                        "        self.num = num\n" + //
                        "\n";

        assertPrints("", source);
    }

    @Test
    public void classInstantiate() {
        String source = "class Foo:\n" + //
                        "    def __init__(self, num):\n" + //
                        "        self.num = num\n" + //
                        "\n" + //
                        "Foo(42)\n";

        assertPrints("", source);
    }

    @Test
    public void instanceAttributes() {
        String source = "class Foo:\n" + //
                        "    def __init__(self, num):\n" + //
                        "        self.num = num\n" + //
                        "\n" + //
                        "foo = Foo(42)\n" + //
                        "print(foo.num)\n";

        assertPrints("42\n", source);
    }

    @Test
    public void classAttribute1() {
        String source = "class Foo:\n" + //
                        "    class_attr = 2\n" + //
                        "    def __init__(self, num):\n" + //
                        "       self.num = num\n" + //
                        "\n" + //
                        "foo = Foo(42)\n" + //
                        "print(foo.num)\n" + //
                        "print(Foo.class_attr)\n" + //
                        "print(foo.class_attr)\n";

        assertPrints("42\n2\n2\n", source);
    }

    @Test
    public void classAttribute2() {
        String source = "class Foo:\n" + //
                        "    class_attr = AssertionError\n" + //
                        "\n" + //
                        "print(Foo.class_attr)\n";
        assertPrints("<type 'exceptions.AssertionError'>\n", source);
    }

    @Test
    public void classAttribute3() {
        String source = "class Foo:\n" + //
                        "    def assertEqual():\n" + //
                        "        pass\n" + //
                        "    class_attr = assertEqual\n" + //
                        "\n" + //
                        "print(Foo.class_attr)\n";
        assertPrintContains("<function assertEqual", source);
    }

    @Test
    public void userClassInheritance() {
        String source = "class ClassA(object):\n" + //
                        "    pass\n" + //
                        "\n" + //
                        "class ClassB(ClassA):\n" + //
                        "    pass\n" + //
                        "";

        assertPrints("", source);
    }

    @Test
    public void scriptClassTest() {
        String source = "class Foo:\n" + //
                        "  def __init__(self, num):\n" + //
                        "    self.num = num\n" + //
                        "\n" + //
                        "  def showNum(self):\n" + //
                        "    print(self.num)\n" + //
                        "foo = Foo(42)\n" + //
                        "foo.showNum()";
        assertPrints("42\n", source);
    }

    @Test
    public void defaultArgInMethod() {
        String source = "class TestSuite():\n" + //
                        "    def assertTrue(self, arg, msg=None):\n" + //
                        "        print(\"arg\", arg)\n" + //
                        "        print(\"msg\", msg)\n" + //
                        "testSuite = TestSuite()\n" + //
                        "testSuite.assertTrue(1 < 2, \"1 is not less than 2\")\n";

        assertPrints("arg True\nmsg 1 is not less than 2\n", source);
    }

    @Test
    public void keywordArgInMethod() {
        String source = "class TestSuite():\n" + //
                        "    def assertTrue(self, arg, msg=None):\n" + //
                        "        print(\"arg\", arg)\n" + //
                        "        print(\"msg\", msg)\n" + //
                        "testSuite = TestSuite()\n" + //
                        "testSuite.assertTrue(1 < 2, msg=\"1 is not less than 2\")\n";

        assertPrints("arg True\nmsg 1 is not less than 2\n", source);
    }

}
