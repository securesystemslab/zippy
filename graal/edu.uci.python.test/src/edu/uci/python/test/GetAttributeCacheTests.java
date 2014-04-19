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
import static org.junit.Assert.*;

import java.nio.file.*;
import java.util.*;

import org.junit.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.attribute.DispatchUnboxedNode.*;
import edu.uci.python.runtime.*;

public class GetAttributeCacheTests {

    @Test
    public void bimorphicCallSite() {
        Path script = Paths.get("call-bimorphic-test.py");
        assertPrints("do stuff A\ndo stuff B\n", script);
    }

    @Test
    public void bimorphicInObjectAttribute() {
        Path script = Paths.get("getattribute-bimorphic-inobject-test.py");
        assertPrints("42\n24\n42\n24\n", script);
    }

    @Test
    public void bimorphicCachedClassAttribute() {
        Path script = Paths.get("getattribute-bimorphic-cachedclass-test.py");
        assertPrints("42\n24\n42\n24\n", script);
    }

    @Test
    public void bimorphicInClassAttribute() {
        Path script = Paths.get("getattribute-bimorphic-inclass-test.py");
        assertPrints("42\n24\n42\n24\n", script);
    }

    @Test
    public void classChainChecks() {
        Path script = Paths.get("class-chain-check-test.py");
        assertPrints("do stuff\ndo stuff\n", script);
    }

    @Test
    public void cachedModuleAttr() {
        String source = "import time\n" + //
                        "for i in range(2):\n" + //
                        "  time.foo = 42\n" + //
                        "  print(time.foo)\n";
        assertPrints("42\n42\n", source);
    }

    @Test
    public void booleanAttr() {
        String source = "class A:\n" + //
                        "    def __init__(self, bool):" + //
                        "        self.bool = bool" + //
                        "\n" + //
                        "a = A(True)\n" + //
                        "print(a.bool)\n";
        assertPrints("True\n", source);
    }

    @Test
    public void inObjectAttributeStore() {
        String source = "class A:\n" + //
                        "    pass\n" + //
                        "a = A()\n" + //
                        "for i in range(3):\n" + //
                        "    a.attr = int\n" + //
                        "    print(a.attr)\n";
        assertPrints("<class 'int'>\n<class 'int'>\n<class 'int'>\n", source);
    }

    @Test
    public void unboxedAttributePolymorphic() {
        String source = "l = [1, 3.0, 'oo']\n" + //
                        "for i in l:\n" + //
                        "    i.__str__\n";
        PythonParseResult result = assertPrints("", source);
        RootNode root = result.getModuleRoot();

        // depth of dispatch chain
        List<AttributeDispatchUnboxedNode> dispatchNodes = NodeUtil.findAllNodeInstances(root, AttributeDispatchUnboxedNode.class);
        assertTrue(dispatchNodes.size() == 3);

        // bottom unitialized dispatch node
        List<UninitializedDispatchUnboxedNode> uninitialized = NodeUtil.findAllNodeInstances(root, UninitializedDispatchUnboxedNode.class);
        assertTrue(uninitialized.size() == 1);
    }

    @Test
    public void unboxedAttributeMegamorphic() {
        String source = "l = [1, 3.0, 'oo', []]\n" + //
                        "for i in l:\n" + //
                        "    i.__str__\n";
        PythonParseResult result = assertPrints("", source);
        RootNode root = result.getModuleRoot();

        // depth of dispatch chain
        List<AttributeDispatchUnboxedNode> dispatchNodes = NodeUtil.findAllNodeInstances(root, AttributeDispatchUnboxedNode.class);
        assertTrue(dispatchNodes.size() == 0);

        // generic dispatch node
        List<GenericDispatchUnboxedNode> uninitialized = NodeUtil.findAllNodeInstances(root, GenericDispatchUnboxedNode.class);
        assertTrue(uninitialized.size() == 1);
    }

}
