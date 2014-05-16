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
package edu.uci.python.nodes.function;

import java.io.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.call.CallDispatchBoxedNode.*;
import edu.uci.python.nodes.call.legacy.CallGeneratorInlinedNode.PeeledGeneratorLoopNode;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;

/**
 * RootNode of a Python Function body. It is invoked by a CallTarget.
 *
 * @author zwei
 */
public final class FunctionRootNode extends RootNode {

    private final PythonContext context;
    private final String functionName;

    @Child protected PNode body;
    private PNode uninitializedBody;

    public FunctionRootNode(PythonContext context, String functionName, FrameDescriptor frameDescriptor, PNode body) {
        super(null, frameDescriptor); // SourceSection is not supported yet.
        this.context = context;
        this.functionName = functionName;
        this.body = body;
        this.uninitializedBody = NodeUtil.cloneNode(body);
    }

    public PythonContext getContext() {
        return context;
    }

    public String getFunctionName() {
        return functionName;
    }

    public PNode getBody() {
        return body;
    }

    public InlinedFunctionRootNode getInlinedRootNode() {
        return new InlinedFunctionRootNode(this);
    }

    public PNode getUninitializedBody() {
        return uninitializedBody;
    }

    public PNode getClonedUninitializedBody() {
        return NodeUtil.cloneNode(uninitializedBody);
    }

    @Override
    public FunctionRootNode copy() {
        return new FunctionRootNode(this.context, this.functionName, this.getFrameDescriptor(), this.uninitializedBody);
    }

    @Override
    public FunctionRootNode split() {
        return new FunctionRootNode(this.context, this.functionName, this.getFrameDescriptor().shallowCopy(), this.uninitializedBody);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (CompilerDirectives.inInterpreter()) {
            optimizeGeneratorCalls();
        }
        return body.execute(frame);
    }

    protected void optimizeGeneratorCalls() {
        if (CompilerDirectives.inCompiledCode() || !PythonOptions.InlineGeneratorCalls) {
            return;
        }

        CompilerAsserts.neverPartOfCompilation();

        for (DispatchGeneratorBoxedNode dispatch : NodeUtil.findAllNodeInstances(body, DispatchGeneratorBoxedNode.class)) {
            boolean inlinable = dispatch.getCost() == NodeCost.MONOMORPHIC;
            PGeneratorFunction genfun = dispatch.getGeneratorFunction();
            int calleeNodeCount = NodeUtil.countNodes(this);
            int generatorNodeCount = NodeUtil.countNodes(genfun.getFunctionRootNode());
            inlinable &= generatorNodeCount < 500;
            inlinable &= calleeNodeCount < 2500;
            peelGeneratorLoop(inlinable, dispatch, genfun);
        }
    }

    protected void peelGeneratorLoop(boolean inlinable, DispatchGeneratorBoxedNode dispatch, PGeneratorFunction genfun) {
        CompilerAsserts.neverPartOfCompilation();

        if (!inlinable) {
            return;
        }

        Node callNode = dispatch.getTop().getParent();
        Node getIter = callNode.getParent();
        Node forNode = getIter.getParent();

        if (!(getIter instanceof GetIteratorNode) || !(forNode instanceof ForNode) || !(callNode instanceof PythonCallNode)) {
            return;
        }

        PythonCallNode call = (PythonCallNode) callNode;
        ForNode loop = (ForNode) forNode;
        PeeledGeneratorLoopNode peeled = new PeeledGeneratorLoopNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), call.getArgumentNodes());
        loop.replace(peeled);

        PNode loopBody = loop.getBody();
        FrameSlot yieldToSlotInCallerFrame;
        PNode target = loop.getTarget();
        yieldToSlotInCallerFrame = ((FrameSlotNode) target).getSlot();

        for (YieldNode yield : NodeUtil.findAllNodeInstances(peeled.getGeneratorRoot(), YieldNode.class)) {
            PNode frameTransfer = FrameTransferNodeFactory.create(yieldToSlotInCallerFrame, yield.getRhs());
            PNode frameSwapper = new FrameSwappingNode(NodeUtil.cloneNode(loopBody));
            PNode block = BlockNode.create(frameTransfer, frameSwapper);
            yield.replace(block);
        }

        /**
         * Reset generator expressions in the ungeneratorized function as declared not in generator
         * frame.
         */
        RootNode enclosingRoot = getRootNode();
        for (GeneratorExpressionNode genexp : NodeUtil.findAllNodeInstances(enclosingRoot, GeneratorExpressionNode.class)) {
            genexp.setEnclosingFrameGenerator(false);
        }

        PrintStream ps = System.out;
        ps.println("[ZipPy] transformed generator call to " + genfun.getCallTarget() + " in " + getRootNode());
    }

    @Override
    public String toString() {
        return "<function " + functionName + " at " + Integer.toHexString(hashCode()) + ">";
    }

    public static class InlinedFunctionRootNode extends PNode {

        private final String functionName;
        @Child protected PNode body;

        protected InlinedFunctionRootNode(FunctionRootNode node) {
            this.functionName = node.functionName;
            this.body = NodeUtil.cloneNode(node.uninitializedBody);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return body.execute(frame);
        }

        @Override
        public String toString() {
            return "<inlined function root " + functionName + " at " + Integer.toHexString(hashCode()) + ">";
        }
    }

}
