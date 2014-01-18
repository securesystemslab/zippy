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
package edu.uci.python.nodes.optimize;

import java.util.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.argument.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.function.GeneratorExpressionDefinitionNode.CallableGeneratorExpressionDefinition;
import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.loop.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.*;

public class GeneratorExpressionOptimizer {

    private final PythonContext context;
    private final FunctionRootNode functionRoot;

    public GeneratorExpressionOptimizer(FunctionRootNode functionRoot) {
        this.context = functionRoot.getContext();
        this.functionRoot = functionRoot;
    }

    public void optimize() {
        for (GeneratorExpressionDefinitionNode genExp : NodeUtil.findAllNodeInstances(functionRoot, GeneratorExpressionDefinitionNode.class)) {
            if (!genExp.needsDeclarationFrame()) {
                continue; // No need to optimize
            }

            EscapeAnalyzer escapeAnalyzer = new EscapeAnalyzer(functionRoot, genExp);
            if (escapeAnalyzer.escapes()) {
                context.getStandardOut().println("[ZipPy] escapse analysis: " + genExp + " escapes current frame");
            } else {
                context.getStandardOut().println("[ZipPy] escapse analysis: " + genExp + " does not escape current frame");
                transform(genExp, escapeAnalyzer);
            }
        }
    }

    private void transform(GeneratorExpressionDefinitionNode genExp, EscapeAnalyzer escapeAnalyzer) {

        if (!escapeAnalyzer.isBoundToLocalFrame()) {
            /**
             * The simplest case in micro bench: generator-expression.
             */
            if (genExp.getParent() instanceof GetIteratorNode) {
                transformGetIterToInlineableGeneratorCall(genExp, (GetIteratorNode) genExp.getParent(), false);
            } else if (genExp.getParent() instanceof CallFunctionNode) {
                transformToGeneratorCall(genExp, genExp);
            } else if (genExp.getParent() instanceof InlinedCallNode) {
                GetIteratorNode getIter = NodeUtil.findFirstNodeInstance(genExp.getParent(), GetIteratorNode.class);
                transformGetIterToInlineableGeneratorCall(genExp, getIter, true);
            }

            return;
        }

        FrameSlot genExpSlot = escapeAnalyzer.getTargetExpressionSlot();
        for (FrameSlotNode read : NodeUtil.findAllNodeInstances(functionRoot, ReadLocalVariableNode.class)) {
            if (!read.getSlot().equals(genExpSlot)) {
                continue;
            }

            if (read.getParent() instanceof GetIteratorNode) {
                transformGetIterToInlineableGeneratorCall(genExp, (GetIteratorNode) read.getParent(), false);
            }
        }
    }

    private void transformGetIterToInlineableGeneratorCall(GeneratorExpressionDefinitionNode genExp, GetIteratorNode getIterator, boolean isTargetCallSiteInInlinedFrame) {
        FrameDescriptor fd = genExp.getFrameDescriptor();
        FunctionRootNode root = (FunctionRootNode) genExp.getFunctionRootNode();

        try {
            List<FrameSlot> arguments = addParameterSlots(root, fd, getEnclosingFrameDescriptor(genExp));
            replaceParameters(arguments, root);
            replaceReadLevels(arguments, root);
            PNode[] argReads = assembleArgumentReads(arguments, genExp, isTargetCallSiteInInlinedFrame);
            CallableGeneratorExpressionDefinition callableGenExp = new CallableGeneratorExpressionDefinition(genExp);
            getIterator.getOperand().replace(new CallGeneratorNode(callableGenExp, argReads, callableGenExp, root));
            functionRoot.updateUninitializedBody();
        } catch (IllegalStateException e) {
            return;
        }

        context.getStandardOut().println("[ZipPy] genexp optimizer: transform " + genExp + " to inlineable generator call");
    }

    private void transformToGeneratorCall(GeneratorExpressionDefinitionNode genExp, PNode loadGenerator) {
        FrameDescriptor fd = genExp.getFrameDescriptor();
        FunctionRootNode root = (FunctionRootNode) genExp.getFunctionRootNode();

        try {
            List<FrameSlot> arguments = addParameterSlots(root, fd, getEnclosingFrameDescriptor(genExp));
            replaceParameters(arguments, root);
            replaceReadLevels(arguments, root);
            PNode[] argReads = assembleArgumentReads(arguments, genExp, false);
            CallableGeneratorExpressionDefinition callableGenExp = new CallableGeneratorExpressionDefinition(genExp);
            loadGenerator.replace(new CallFunctionNoKeywordNode.CallFunctionCachedNode(callableGenExp, argReads, callableGenExp, AlwaysValidAssumption.INSTANCE));
            functionRoot.updateUninitializedBody();
        } catch (IllegalStateException e) {
            return;
        }

        context.getStandardOut().println("[ZipPy] genexp optimizer: transform " + genExp + " to regular generator call");
    }

    /**
     * Assembles nodes that read the arguments to be passed to the transformed generator call.
     */
    private static PNode[] assembleArgumentReads(List<FrameSlot> genExpParams, GeneratorExpressionDefinitionNode genExp, boolean readFromCargoFrame) {
        String[] argumentIds = new String[genExpParams.size()];

        for (int i = 0; i < argumentIds.length; i++) {
            argumentIds[i] = (String) genExpParams.get(i).getIdentifier();
        }

        PNode[] reads = new PNode[argumentIds.length];
        FrameDescriptor enclosingFrame = getEnclosingFrameDescriptor(genExp);

        for (int i = 0; i < argumentIds.length; i++) {
            FrameSlot argSlot = enclosingFrame.findFrameSlot(argumentIds[i]);
            PNode read = genExp.isDeclarationFrameGenerator() ? ReadGeneratorFrameVariableNode.create(argSlot) : NodeFactory.getInstance().createReadLocal(argSlot);

            if (readFromCargoFrame) {
                read = new FrameSwappingNode(read);
            }

            reads[i] = read;
            assert reads[i] != null;
        }

        return reads;
    }

    private static FrameDescriptor getEnclosingFrameDescriptor(PNode genExp) {
        Node current = genExp;
        while (!(current instanceof RootNode)) {
            current = current.getParent();
        }

        FrameSlotNode slotNode = NodeUtil.findFirstNodeInstance(current, FrameSlotNode.class);
        return slotNode.getSlot().getFrameDescriptor();
    }

    /**
     * Extends the generator expression frame to accommodate localized parameters.
     */
    private static List<FrameSlot> addParameterSlots(RootNode root, FrameDescriptor genExpFrameDescriptor, FrameDescriptor enclosingFrame) {
        List<String> parametersToAdd = new ArrayList<>();
        List<FrameSlot> parameterSlotsToAdd = new ArrayList<>();

        for (ReadLevelVariableNode read : NodeUtil.findAllNodeInstances(root, ReadLevelVariableNode.class)) {
            String id = (String) read.getSlot().getIdentifier();

            if (parametersToAdd.contains(id)) {
                continue;
            }

            if (enclosingFrame.findFrameSlot(id) == null) {
                throw new IllegalStateException(); // reference goes beyond enclosing frame.
            }

            parametersToAdd.add(id);
            parameterSlotsToAdd.add(genExpFrameDescriptor.findOrAddFrameSlot(id));
        }

        return parameterSlotsToAdd;
    }

    /**
     * Replace with empty parameter load in generator expression with real ones.
     */
    private static void replaceParameters(List<FrameSlot> slots, FunctionRootNode root) {
        GeneratorReturnTargetNode body = NodeUtil.findFirstNodeInstance(root, GeneratorReturnTargetNode.class);
        BlockNode parameters = (BlockNode) body.getParameters();

        if (!parameters.isEmpty()) {
            throw new IllegalStateException();
        }

        body.getParameters().replace(assembleParameterWrites(slots, false));

        // Uninitialized body.
        ReturnTargetNode unitializedbody = (ReturnTargetNode) root.getUninitializedBody();
        PNode innerBody = unitializedbody.getBody();
        innerBody.replace(new BlockNode(new PNode[]{assembleParameterWrites(slots, true), (PNode) innerBody.copy()}));
    }

    private static BlockNode assembleParameterWrites(List<FrameSlot> argumentSlots, boolean writeToLocalFrame) {
        PNode[] writes = new PNode[argumentSlots.size()];

        for (int i = 0; i < argumentSlots.size(); i++) {
            FrameSlot slot = argumentSlots.get(i);
            ReadArgumentNode read = new ReadArgumentNode(i);
            writes[i] = writeToLocalFrame ? WriteLocalVariableNodeFactory.create(slot, read) : WriteGeneratorFrameVariableNodeFactory.create(slot, read);
        }

        return new BlockNode(writes);
    }

    /**
     * Redirect read levels to read from local frame instead. <br>
     * Need to replace all read level in uninitialized body too. Make sure that after possible
     * inlining, read levels are still redirected to localized parameters.
     */
    private static void replaceReadLevels(List<FrameSlot> localizedSlots, FunctionRootNode root) {
        for (FrameSlot slot : localizedSlots) {
            replaceReadLevelsWith(slot, root, false);
            replaceReadLevelsWith(slot, root.getUninitializedBody(), true);
        }
    }

    private static void replaceReadLevelsWith(FrameSlot targetSlot, Node root, boolean replaceWithReadLocals) {
        for (ReadLevelVariableNode read : NodeUtil.findAllNodeInstances(root, ReadLevelVariableNode.class)) {
            String id = (String) read.getSlot().getIdentifier();

            if (!id.equals(targetSlot.getIdentifier())) {
                continue;
            }

            if (replaceWithReadLocals) {
                read.replace(NodeFactory.getInstance().createReadLocal(targetSlot));
            } else {
                read.replace(ReadGeneratorFrameVariableNode.create(targetSlot));
            }
        }
    }

}
