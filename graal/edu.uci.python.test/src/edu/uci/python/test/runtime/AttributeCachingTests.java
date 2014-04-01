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
package edu.uci.python.test.runtime;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.attribute.*;
import edu.uci.python.nodes.attribute.GetAttributeNode.BoxedGetAttributeNode;
import edu.uci.python.nodes.attribute.GetAttributeNode.UnboxedGetMethodNode;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;
import edu.uci.python.test.*;

public class AttributeCachingTests {

    @Test
    public void builtinObjectAttribute() {
        // environment
        PythonContext context = PythonTests.getContext();
        NodeFactory factory = new NodeFactory();

        // AST assembling
        List<PNode> values = new ArrayList<>();
        values.add(factory.createIntegerLiteral(0));
        values.add(factory.createIntegerLiteral(42));
        PNode plist = factory.createListLiteral(values);
        PNode getattr = factory.createGetAttribute(context, plist, "append");

        BlockNode body = factory.createSingleStatementBlock(getattr);
        RootNode root = new FunctionRootNode(context, "test", new FrameDescriptor(), body);
        Truffle.getRuntime().createCallTarget(root);

        // 1st execute
        VirtualFrame frame = PythonTests.createVirtualFrame();
        root.execute(frame);

        // check rewrite of UninitializedGetAttributeNode
        PNode getAttr = (PNode) NodeUtil.findNodeChildren(body).get(0);
        assertTrue(getAttr instanceof UnboxedGetMethodNode);

        // 2nd execute
        frame = PythonTests.createVirtualFrame();
        root.execute(frame);

        // check rewrite of UninitializedCachedAttributeNode
        UnboxedGetMethodNode getMethod = (UnboxedGetMethodNode) getAttr;
        AbstractUnboxedAttributeNode cache = NodeUtil.findFirstNodeInstance(getMethod, AbstractUnboxedAttributeNode.class);
        assertTrue(cache instanceof UnboxedAttributeCacheNode.CachedObjectAttributeNode);

        // 3rd execute
        frame = PythonTests.createVirtualFrame();
        root.execute(frame);

        // make sure cache node stay unchanged
        cache = NodeUtil.findFirstNodeInstance(getMethod, AbstractUnboxedAttributeNode.class);
        assertTrue(cache instanceof UnboxedAttributeCacheNode.CachedObjectAttributeNode);

        /**
         * test fall back.
         */
        // replace primary to a string
        PNode pstr = factory.createStringLiteral("yy");
        NodeUtil.findFirstNodeInstance(getMethod, ListLiteralNode.class).replace(pstr);

        // 4th execute
        frame = PythonTests.createVirtualFrame();
        try {
            root.execute(frame);
        } catch (PyException pe) {
            assertTrue(pe.value.toString().contains("no attribute"));
        }

        /**
         * At this point AST failed to rewrite itself due to unexpected attribute lookup. Since we
         * recovered from the no attribute exception, we should be able to continue.
         */
        // replace primary to a full PythonBasicObject
        PythonClass classA = new PythonClass(context, null, "A");
        PythonBasicObject pbObj = new PythonObject(classA);
        pbObj.setAttribute("append", 42);
        PNode objNode = factory.createObjectLiteral(pbObj);
        NodeUtil.findFirstNodeInstance(getMethod, StringLiteralNode.class).replace(objNode);

        // 5th execute
        frame = PythonTests.createVirtualFrame();
        root.execute(frame);

        // check rewrite of UnboxedGetAttributeNode to BoxedGetAttributeNode
        getAttr = (PNode) NodeUtil.findNodeChildren(body).get(0);
        assertTrue(getAttr instanceof BoxedGetAttributeNode);
    }

    @Test
    public void pythonObjectAttribute() {
        // environment
        PythonContext context = PythonTests.getContext();
        NodeFactory factory = new NodeFactory();

        // in object attribute
        PythonClass classA = new PythonClass(context, null, "A");
        PythonBasicObject pbObj = new PythonObject(classA);
        pbObj.setAttribute("foo", 42);

        // assemble AST
        PNode objNode = factory.createObjectLiteral(pbObj);
        PNode getattr = factory.createGetAttribute(context, objNode, "foo");

        BlockNode body = factory.createSingleStatementBlock(getattr);
        RootNode root = new FunctionRootNode(context, "test", new FrameDescriptor(), body);
        Truffle.getRuntime().createCallTarget(root);

        // 1st execute
        VirtualFrame frame = PythonTests.createVirtualFrame();
        root.execute(frame);

        // check rewrite of UninitializedGetAttributeNode
        PNode getAttr = (PNode) NodeUtil.findNodeChildren(body).get(0);
        assertTrue(getAttr instanceof BoxedGetAttributeNode);

        // 2nd execute
        frame = PythonTests.createVirtualFrame();
        root.execute(frame);

        // check rewrite of UninitializedCachedAttributeNode
        AbstractBoxedAttributeNode cache = NodeUtil.findFirstNodeInstance(getAttr, AbstractBoxedAttributeNode.class);
        assertTrue(cache instanceof BoxedAttributeCacheNode.CachedIntAttributeNode);

        // 3rd execute
        frame = PythonTests.createVirtualFrame();
        root.execute(frame);

        // make sure cache node stay unchanged
        cache = NodeUtil.findFirstNodeInstance(getAttr, AbstractBoxedAttributeNode.class);
        assertTrue(cache instanceof BoxedAttributeCacheNode.CachedIntAttributeNode);

        /**
         * Test fall back
         */
        // modify object layout
        pbObj.deleteAttribute("foo");

        // 4th execute
        frame = PythonTests.createVirtualFrame();
        try {
            root.execute(frame);
        } catch (PyException pe) {
            assertTrue(pe.value.toString().contains("no attribute"));
        }
    }
}
