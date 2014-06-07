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
import com.oracle.truffle.api.nodes.NodeUtil.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.profiler.*;
import edu.uci.python.nodes.subscript.*;
import edu.uci.python.nodes.argument.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.call.CallDispatchBoxedNode.GeneratorDispatchBoxedNode;
import edu.uci.python.nodes.call.CallDispatchNoneNode.GeneratorDispatchNoneNode;
import edu.uci.python.nodes.call.CallDispatchSpecialNode.GeneratorDispatchSpecialNode;
import edu.uci.python.nodes.call.PythonCallNode.BoxedCallNode;
import edu.uci.python.nodes.call.PythonCallNode.NoneCallNode;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.optimize.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import static edu.uci.python.nodes.optimize.PeeledGeneratorLoopNode.*;

/**
 * RootNode of a Python Function body. It is invoked by a CallTarget.
 *
 * @author zwei
 */
public final class FunctionRootNode extends RootNode {

    private final PythonContext context;
    private final String functionName;

    /**
     * Generator related flags.
     */
    private final boolean isGenerator;
    private boolean hasGeneratorExpression;
    private int peelingTrialCounter = 0;

    @Child protected PNode body;
    private PNode uninitializedBody;

    @Child protected PNode profiler;

    public FunctionRootNode(PythonContext context, String functionName, boolean isGenerator, FrameDescriptor frameDescriptor, PNode body) {
        super(null, frameDescriptor); // SourceSection is not supported yet.
        this.context = context;
        this.functionName = functionName;
        this.isGenerator = isGenerator;
        this.body = NodeUtil.cloneNode(body);
        this.uninitializedBody = NodeUtil.cloneNode(body);

        if (PythonOptions.ProfileCalls) {
            this.profiler = new ProfilerNode(this);
        } else {
            this.profiler = EmptyNode.create();
        }
    }

    public PythonContext getContext() {
        return context;
    }

    public boolean isGenerator() {
        return isGenerator;
    }

    public void reportGeneratorExpression() {
        hasGeneratorExpression = true;
    }

    public void reportGeneratorDispatch() {
        peelingTrialCounter = 0;
    }

    public String getFunctionName() {
        return functionName;
    }

    public PNode getBody() {
        return body;
    }

    public PNode getUninitializedBody() {
        return uninitializedBody;
    }

    @Override
    public FunctionRootNode split() {
        return new FunctionRootNode(context, functionName, isGenerator, getFrameDescriptor().shallowCopy(), uninitializedBody);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (CompilerDirectives.inInterpreter()) {
            if (hasGeneratorExpression || peelingTrialCounter++ < 5) {
                optimizeHelper();
            }
        }
        if (PythonOptions.ProfileCalls) {
            profiler.execute(frame);
        }
        return body.execute(frame);
    }

    @Override
    public boolean applyTransformation() {
        peelingTrialCounter = 0;
        return optimizeHelper();
    }

    private boolean optimizeHelper() {
        CompilerAsserts.neverPartOfCompilation();

        if (CompilerDirectives.inCompiledCode() || !PythonOptions.InlineGeneratorCalls || isGenerator) {
            return false;
        }

        if (PythonOptions.OptimizeGeneratorExpressions) {
            new GeneratorExpressionOptimizer(this).optimize();
        }

        boolean succeed = false;
        for (GeneratorDispatchBoxedNode dispatch : NodeUtil.findAllNodeInstances(body, GeneratorDispatchBoxedNode.class)) {
            PGeneratorFunction genfun = dispatch.getGeneratorFunction();
            boolean inlinable = isInlinable(dispatch, genfun);
            succeed = peelGeneratorLoop(inlinable, dispatch, genfun);
        }

        for (GeneratorDispatchNoneNode dispatch : NodeUtil.findAllNodeInstances(body, GeneratorDispatchNoneNode.class)) {
            PGeneratorFunction genfun = dispatch.getGeneratorFunction();
            boolean inlinable = isInlinable(dispatch, genfun);
            succeed = peelGeneratorLoop(inlinable, dispatch, genfun);
        }

        for (GeneratorDispatchSpecialNode dispatch : NodeUtil.findAllNodeInstances(body, GeneratorDispatchSpecialNode.class)) {
            PGeneratorFunction genfun = dispatch.getGeneratorFunction();
            boolean inlinable = isInlinable(dispatch, genfun);
            succeed = peelGeneratorLoop(inlinable, dispatch, genfun);
        }

        return succeed;
    }

    private boolean isInlinable(CallDispatchNode dispatch, PGeneratorFunction genfun) {
        boolean inlinable = dispatch.getCost() == NodeCost.MONOMORPHIC;

        Node current = dispatch;
        while (!(current instanceof PeeledGeneratorLoopNode) && !(current instanceof FunctionRootNode)) {
            current = current.getParent();
        }

        String callerName;
        if (current instanceof PeeledGeneratorLoopNode) {
            callerName = ((PeeledGeneratorLoopNode) current).getName();
        } else {
            callerName = ((FunctionRootNode) current).getFunctionName();
        }

        if (callerName.equals(genfun.getName())) {
            inlinable = false;
        }

        int callerNodeCount = getDeepNodeCount();
        int generatorNodeCount = NodeUtil.countNodes(genfun.getFunctionRootNode());
        inlinable &= generatorNodeCount < 300;
        inlinable &= callerNodeCount < 500;
        return inlinable;
    }

    /**
     * See OptimizedCallUtils.
     */
    protected int getDeepNodeCount() {
        return NodeUtil.countNodes(this, new NodeCountFilter() {
            public boolean isCounted(Node node) {
                NodeCost cost = node.getCost();
                if (cost != null && cost != NodeCost.NONE && cost != NodeCost.UNINITIALIZED) {
                    return true;
                }
                return false;
            }
        }, true);
    }

    protected boolean peelGeneratorLoop(boolean inlinable, GeneratorDispatch dispatch, PGeneratorFunction genfun) {
        CompilerAsserts.neverPartOfCompilation();

        if (!inlinable) {
            return false;
        }

        Node callNode = dispatch.getCallNode();
        Node getIter = callNode.getParent();
        Node forNode = getIter.getParent();

        if (!(getIter instanceof GetIteratorNode) || !(forNode instanceof ForNode)) {
            return false; // Loop nodes
        }

        if (!(callNode instanceof GeneratorDispatchSpecialNode) && !(callNode instanceof PythonCallNode) && !(callNode instanceof SubscriptLoadIndexNode)) {
            return false; // Call nodes
        }

        ForNode loop = (ForNode) forNode;
        PNode orignalLoop = NodeUtil.cloneNode(loop);
        PeeledGeneratorLoopNode peeled;

        if (callNode instanceof BoxedCallNode) {
            GeneratorDispatchBoxedNode boxedDispatch = (GeneratorDispatchBoxedNode) dispatch;
            BoxedCallNode call = (BoxedCallNode) callNode;
            peeled = new PeeledGeneratorLoopBoxedNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), call.getPrimaryNode(), call.getArgumentsNode(),
                            boxedDispatch.getCheckNode(), orignalLoop);
        } else if (callNode instanceof NoneCallNode) {
            NoneCallNode call = (NoneCallNode) callNode;
            peeled = new PeeledGeneratorLoopNoneNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), call.getCalleeNode(), call.getArgumentsNode(),
                            dispatch.getGeneratorFunction(), orignalLoop);
        } else if (callNode instanceof GeneratorDispatchSpecialNode) {
            GeneratorDispatchSpecialNode generatorDispatch = (GeneratorDispatchSpecialNode) callNode;
            GetIteratorNode getIterNode = (GetIteratorNode) getIter;
            peeled = new PeeledGeneratorLoopSpecialNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), getIterNode.getOperand(), new ArgumentsNode(new PNode[]{}),
                            generatorDispatch.getCheckNode(), orignalLoop);
        } else if (callNode instanceof SubscriptLoadIndexNode) {
            SubscriptLoadIndexNode indexLoad = (SubscriptLoadIndexNode) callNode;
            GeneratorDispatchSpecialNode generatorDispatch = (GeneratorDispatchSpecialNode) indexLoad.getSpecialMethodDispatch();
            peeled = new PeeledGeneratorLoopSpecialNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), indexLoad.getPrimary(), new ArgumentsNode(
                            new PNode[]{indexLoad.getSlice()}), generatorDispatch.getCheckNode(), orignalLoop);
        } else {
            return false;
        }

        loop.replace(peeled);

        PNode loopBody = loop.getBody();
        FrameSlot yieldToSlotInCallerFrame;
        PNode target = loop.getTarget();

        /**
         * PythonWrapperNode check is added for profiling.
         */
        if (target instanceof PythonWrapperNode) {
            PythonWrapperNode wrapper = (PythonWrapperNode) target;
            target = wrapper.getChild();
        }

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
        ps.println("[ZipPy] peeled generator " + genfun.getCallTarget() + " in " + getRootNode());
        return true;
    }

    @Override
    public String toString() {
        return "<function " + functionName + " at " + Integer.toHexString(hashCode()) + ">";
    }

}
