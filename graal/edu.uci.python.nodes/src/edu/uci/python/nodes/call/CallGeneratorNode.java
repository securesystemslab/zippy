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
package edu.uci.python.nodes.call;

import java.io.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.call.CallFunctionNoKeywordNode.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.loop.*;
import edu.uci.python.nodes.optimize.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;

public class CallGeneratorNode extends CallFunctionCachedNode implements InlinableCallSite {

    private final FunctionRootNode generatorRoot;
    private int callCount;

    public CallGeneratorNode(PNode callee, PNode[] arguments, PGeneratorFunction generator, Assumption globalScopeUnchanged) {
        super(callee, arguments, generator, globalScopeUnchanged);
        this.generatorRoot = (FunctionRootNode) generator.getFunctionRootNode();
    }

    public CallGeneratorNode(PNode callee, PNode[] arguments, PythonCallable cachedCallee, FunctionRootNode rootNode) {
        super(callee, arguments, cachedCallee, AlwaysValidAssumption.INSTANCE);
        this.generatorRoot = rootNode;
    }

    public int getCallCount() {
        return callCount;
    }

    public void resetCallCount() {
        callCount = 0;
    }

    public Node getInlineTree() {
        return generatorRoot;
    }

    public CallTarget getCallTarget() {
        return generatorRoot.getCallTarget();
    }

    public void invokeGeneratorExpressionOptimizer() {
        RootNode current = getRootNode();
        assert current != null;
        new GeneratorExpressionOptimizer((FunctionRootNode) current).optimize();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (CompilerDirectives.inInterpreter()) {
            callCount += 10000000;
        }

        return super.execute(frame);
    }

    public boolean inline(FrameFactory factory) {
        CompilerAsserts.neverPartOfCompilation();
        assert this.getParent() != null;
        PNode parent = (PNode) getParent();
        assert parent.getParent() != null;
        PNode grandpa = (PNode) parent.getParent();

        if (PythonOptions.UseSimpleGeneratorInlining) {
            return simpleGeneratorLoopTransformation((ForWithLocalTargetNode) grandpa, factory);
        }

        if (parent instanceof GetIteratorNode && grandpa instanceof ForWithLocalTargetNode) {
            transformLoopGeneratorCall((ForWithLocalTargetNode) grandpa, factory);
            invokeGeneratorExpressionOptimizer();
            return true;
        }

        return false;
    }

    private void transformLoopGeneratorCall(ForWithLocalTargetNode loop, FrameFactory factory) {
        CallGeneratorInlinedNode inlinedNode = new CallGeneratorInlinedNode(callee, arguments, cached, generatorRoot, globalScopeUnchanged, factory);
        loop.replace(inlinedNode);

        PNode body = loop.getBody();
        FrameSlot yieldToSlotInCallerFrame;
        AdvanceIteratorNode next = (AdvanceIteratorNode) loop.getTarget();
        PNode target = next.getTarget();
        yieldToSlotInCallerFrame = ((FrameSlotNode) target).getSlot();

        for (YieldNode yield : NodeUtil.findAllNodeInstances(inlinedNode.getGeneratorRoot(), YieldNode.class)) {
            PNode frameTransfer = FrameTransferNodeFactory.create(yieldToSlotInCallerFrame, yield.getRhs());
            PNode frameSwapper = new FrameSwappingNode(NodeUtil.cloneNode(body));
            BlockNode block = new BlockNode(new PNode[]{frameTransfer, frameSwapper});
            yield.replace(block);
        }

        /**
         * Reset generator expressions in the ungeneratorized function as declared not in generator
         * frame.
         */
        RootNode enclosingRoot = getRootNode();
        for (GeneratorExpressionDefinitionNode genexp : NodeUtil.findAllNodeInstances(enclosingRoot, GeneratorExpressionDefinitionNode.class)) {
            genexp.setEnclosingFrameGenerator(false);
        }

        PrintStream ps = System.out;
        ps.println("[ZipPy] transformed generator call to " + cached.getCallTarget() + " in " + getRootNode());
    }

    private boolean simpleGeneratorLoopTransformation(ForWithLocalTargetNode loop, FrameFactory factory) {
        GetGeneratorArgumentsNode getGenArgs = new GetGeneratorArgumentsNode(callee, arguments, (PGeneratorFunction) cached, globalScopeUnchanged);
        FrameSlotNode target = ((AdvanceIteratorNode) loop.getTarget()).getTarget();
        AdvanceInlinedGeneratorNode next = AdvanceInlinedGeneratorNodeFactory.create(factory, cached.getFrameDescriptor(), generatorRoot.getInlinedRootNode(), EMPTYNODE);
        ForOnInlinedGeneratorNode newFor = new ForOnInlinedGeneratorNode(loop.getBody(), target, getGenArgs, next);
        loop.replace(newFor);
        return true;
    }

}
