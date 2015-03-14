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
import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.nodes.NodeUtil.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.subscript.*;
import edu.uci.python.nodes.argument.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.call.CallDispatchBoxedNode.GeneratorDispatchBoxedNode;
import edu.uci.python.nodes.call.CallDispatchNoneNode.GeneratorDispatchNoneNode;
import edu.uci.python.nodes.call.CallDispatchSpecialNode.GeneratorDispatchSpecialNode;
import edu.uci.python.nodes.call.PythonCallNode.BoxedCallNode;
import edu.uci.python.nodes.call.PythonCallNode.NoneCallNode;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.control.GetIteratorNode.GetGeneratorIteratorNode;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.optimize.*;
import edu.uci.python.nodes.optimize.PeeledGeneratorLoopNode.PeeledGeneratorLoopBoxedNode;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
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
    private final Set<GeneratorDispatch> optimizedGeneratorDispatches = new HashSet<>();

    @Child protected PNode body;
    private PNode uninitializedBody;

    public FunctionRootNode(PythonContext context, String functionName, boolean isGenerator, FrameDescriptor frameDescriptor, PNode body) {
        super(null, frameDescriptor); // SourceSection is not supported yet.
        this.context = context;
        this.functionName = functionName;
        this.isGenerator = isGenerator;
        this.body = NodeUtil.cloneNode(body);
        this.uninitializedBody = NodeUtil.cloneNode(body);
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

        return body.execute(frame);
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
            boolean inlinable = isInlinable(dispatch, genfun.getCallTarget());
            succeed = peelGeneratorLoop(inlinable, dispatch, genfun);
        }

        for (GeneratorDispatchNoneNode dispatch : NodeUtil.findAllNodeInstances(body, GeneratorDispatchNoneNode.class)) {
            PGeneratorFunction genfun = dispatch.getGeneratorFunction();
            boolean inlinable = isInlinable(dispatch, genfun.getCallTarget());
            succeed = peelGeneratorLoop(inlinable, dispatch, genfun);
        }

        for (GeneratorDispatchSpecialNode dispatch : NodeUtil.findAllNodeInstances(body, GeneratorDispatchSpecialNode.class)) {
            PGeneratorFunction genfun = dispatch.getGeneratorFunction();
            boolean inlinable = isInlinable(dispatch, genfun.getCallTarget());
            succeed = peelGeneratorLoop(inlinable, dispatch, genfun);
        }

        for (GetGeneratorIteratorNode getIter : NodeUtil.findAllNodeInstances(body, GetGeneratorIteratorNode.class)) {
            PGenerator gen = getIter.getGenerator();

            if (gen == null) {
                continue;
            }

            boolean inlinable = isInlinable(getIter, gen.getCallTarget());
            succeed = peelGeneratorLoopNotAligned(inlinable, getIter, gen);
        }

        return succeed;
    }

    private boolean isInlinable(Node dispatch, RootCallTarget generatorCallTarget) {
        if (PythonOptions.TraceGeneratorInlining) {
            PrintStream ps = System.out;
            ps.println("[ZipPy] try to optimize " + generatorCallTarget + " in " + getRootNode());
        }

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

        String calleeName = ((FunctionRootNode) generatorCallTarget.getRootNode()).getFunctionName();
        if (callerName.equals(calleeName)) {
            inlinable = false;
        }

        int callerNodeCount = getDeepNodeCount(this);
        int generatorNodeCount = getDeepNodeCount(generatorCallTarget.getRootNode());
        inlinable &= generatorNodeCount < 330;
        inlinable &= callerNodeCount < 21000;

        if (callerNodeCount / generatorNodeCount < 5) {
            inlinable = true;
        }

        if (PythonOptions.TraceGeneratorInlining) {
            PrintStream ps = System.out;

            if (inlinable) {
                ps.println("[ZipPy] decide to inline " + generatorCallTarget + " in " + getRootNode() + " gen: " + generatorNodeCount + " caller: " + callerNodeCount);
            } else {
                ps.println("[ZipPy] failed to inline " + generatorCallTarget + " in " + getRootNode() + " gen: " + generatorNodeCount + " caller: " + callerNodeCount);
            }
        }

        return inlinable;
    }

    /**
     * See OptimizedCallUtils.
     */
    protected static int getDeepNodeCount(RootNode root) {
        return NodeUtil.countNodes(root, new NodeCountFilter() {
            public boolean isCounted(Node node) {
                NodeCost cost = node.getCost();
                if (cost != null && cost != NodeCost.NONE && cost != NodeCost.UNINITIALIZED) {
                    return true;
                }
                return false;
            }
        });
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

        if (optimizedGeneratorDispatches.contains(dispatch)) {
            return false;
        }

        ForNode loop = (ForNode) forNode;
        PNode originalLoop = loop;
        PeeledGeneratorLoopNode peeled;

        if (callNode instanceof BoxedCallNode) {
            GeneratorDispatchBoxedNode boxedDispatch = (GeneratorDispatchBoxedNode) dispatch;
            BoxedCallNode call = (BoxedCallNode) callNode;
            peeled = new PeeledGeneratorLoopBoxedNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), call.getPrimaryNode(), call.passPrimaryAsArgument(),
                            call.getArgumentsNode(), boxedDispatch.getCheckNode(), originalLoop);
        } else if (callNode instanceof NoneCallNode) {
            NoneCallNode call = (NoneCallNode) callNode;
            peeled = new PeeledGeneratorLoopNoneNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), call.getCalleeNode(), call.getArgumentsNode(),
                            dispatch.getGeneratorFunction(), originalLoop);
        } else if (callNode instanceof GeneratorDispatchSpecialNode) {
            GeneratorDispatchSpecialNode generatorDispatch = (GeneratorDispatchSpecialNode) callNode;
            GetIteratorNode getIterNode = (GetIteratorNode) getIter;
            peeled = new PeeledGeneratorLoopSpecialNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), getIterNode.getOperand(), new ArgumentsNode(new PNode[]{}),
                            generatorDispatch.getCheckNode(), originalLoop);
        } else if (callNode instanceof SubscriptLoadIndexNode) {
            SubscriptLoadIndexNode indexLoad = (SubscriptLoadIndexNode) callNode;
            GeneratorDispatchSpecialNode generatorDispatch = (GeneratorDispatchSpecialNode) indexLoad.getSpecialMethodDispatch();
            peeled = new PeeledGeneratorLoopSpecialNode((FunctionRootNode) genfun.getFunctionRootNode(), genfun.getFrameDescriptor(), indexLoad.getPrimary(), new ArgumentsNode(
                            new PNode[]{indexLoad.getSlice()}), generatorDispatch.getCheckNode(), originalLoop);
        } else {
            return false;
        }

        PNode loopParent = (PNode) loop.getParent();
        if (loopParent instanceof PeeledGeneratorLoopBoxedNode) {
            ((PeeledGeneratorLoopBoxedNode) loopParent).insertNext((PeeledGeneratorLoopBoxedNode) peeled);
        } else {
            loop.replace(peeled);
        }

        peeled.adoptOriginalLoop();
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

        optimizedGeneratorDispatches.add(dispatch);
        PrintStream ps = System.out;
        ps.println("[ZipPy] peeled generator " + genfun.getCallTarget() + " in " + getRootNode());
        return true;
    }

    protected boolean peelGeneratorLoopNotAligned(boolean inlinable, GetGeneratorIteratorNode getIter, PGenerator generator) {
        CompilerAsserts.neverPartOfCompilation();

        if (!inlinable) {
            return false;
        }

        PNode forNode = (PNode) getIter.getParent();

        if (!(forNode instanceof ForNode)) {
            return false; // Loop nodes
        }

        if (forNode.getParent() instanceof PeeledGeneratorLoopNode) {
            return false; // Optimized
        }

        ForNode loop = (ForNode) forNode;
        PeeledGeneratorLoopNode peeled = new PeeledGeneratorLoopNoCallNode((FunctionRootNode) generator.getCallTarget().getRootNode(), generator.getFrameDescriptor(), getIter.getOperand(), generator,
                        forNode);

        loop.replace(peeled);

        peeled.adoptOriginalLoop();
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
        ps.println("[ZipPy] peeled generator not aligned " + generator.getCallTarget() + " in " + getRootNode());
        return true;
    }

    @Override
    public String toString() {
        return "<function root " + functionName + " at " + Integer.toHexString(hashCode()) + ">";
    }

}
