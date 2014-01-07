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
package edu.uci.python.parser;

import java.util.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

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

    private final PythonParseResult parseResult;
    private RootNode currentRoot;

    public GeneratorExpressionOptimizer(PythonParseResult parseResult) {
        this.parseResult = parseResult;
    }

    public void optimize() {
        for (RootNode functionRoot : parseResult.getFunctionRoots()) {
            doRoot(functionRoot);
        }
    }

    private void doRoot(RootNode root) {
        currentRoot = root;

        for (GeneratorExpressionDefinitionNode genExp : NodeUtil.findAllNodeInstances(root, GeneratorExpressionDefinitionNode.class)) {
            if (!genExp.needsDeclarationFrame()) {
                continue; // No need to optimize
            }

            if (escapesCurrentFrame(genExp)) {
                parseResult.getContext().getStandardOut().println("[ZipPy] escapse analysis: " + genExp + " escapes current frame");
            } else {
                parseResult.getContext().getStandardOut().println("[ZipPy] escapse analysis: " + genExp + " does not escape current frame");
                transform(genExp);
            }
        }
    }

    private boolean escapesCurrentFrame(Node target) {
        Node current = target;

        while (!isStatementNode(current)) {
            current = current.getParent();

            if (current instanceof WriteLocalVariableNode) {
                FrameSlot slot = ((WriteLocalVariableNode) current).getSlot();
                return escapesCurrentFrame(slot);
            } else if (current instanceof WriteNode) {
                return true; // Other write nodes
            } else if (current instanceof CallFunctionNode) {
                PNode calleeNode = ((CallFunctionNode) current).getCallee();

                if (calleeNode instanceof ReadGlobalScopeNode) {
                    return isBuiltinConstructor(((ReadGlobalScopeNode) calleeNode).getAttributeId()) ? false : true;
                }

                return true;
            } else if (current instanceof ReturnNode) {
                return true;
            }
        }

        return false;
    }

    /**
     * Only local reads are effectively analyzed.<br>
     * Since any local write is a statement by it self, and the recursive call always return false.
     */
    private boolean escapesCurrentFrame(FrameSlot slot) {
        if (slot.getIdentifier().equals(TranslationEnvironment.RETURN_SLOT_ID)) {
            return true;
        }

        for (FrameSlotNode slotNode : NodeUtil.findAllNodeInstances(currentRoot, FrameSlotNode.class)) {
            if (!slotNode.getSlot().equals(slot)) {
                continue;
            }

            boolean escapse = escapesCurrentFrame(slotNode);
            if (escapse) {
                return true;
            }
        }

        return false;
    }

    private static boolean isStatementNode(Node node) {
        return node instanceof StatementNode || node instanceof WriteNode;
    }

    /**
     * A trivial way to identify if a call constructs a collection (allocates memory).
     */
    private static boolean isBuiltinConstructor(String name) {
        return name.equals("frozenset") || name.equals("set") || name.equals("list") || name.equals("dict");
    }

    private static void transform(GeneratorExpressionDefinitionNode genExp) {
        PNode parent = (PNode) genExp.getParent();

        /**
         * The simplest case in micro bench: generator-expression.
         */
        if (parent instanceof GetIteratorNode) {
            FrameDescriptor fd = genExp.getFrameDescriptor();
            FunctionRootNode root = (FunctionRootNode) genExp.getFunctionRootNode();
            List<FrameSlot> arguments = addParameterSlots(root, fd);
            replaceParameters(arguments, root);
            replaceReadLevels(arguments, root);
            CallableGeneratorExpressionDefinition callableGenExp = new CallableGeneratorExpressionDefinition(genExp);
            PNode[] argReads = getArguments(arguments, genExp);
            genExp.replace(new CallGeneratorNode(callableGenExp, argReads, callableGenExp, root));
        }
    }

    private static PNode[] getArguments(List<FrameSlot> params, PNode genExp) {
        String[] argumentIds = new String[params.size()];

        for (int i = 0; i < argumentIds.length; i++) {
            argumentIds[i] = (String) params.get(i).getIdentifier();
        }

        PNode[] reads = new PNode[argumentIds.length];
        FrameDescriptor enclosingFrame = getEnclosingFrameDescriptor(genExp);
        for (int i = 0; i < argumentIds.length; i++) {
            FrameSlot argSlot = enclosingFrame.findFrameSlot(argumentIds[i]);
            reads[i] = ReadLocalVariableNodeFactory.create(argSlot);
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

    private static List<FrameSlot> addParameterSlots(RootNode root, FrameDescriptor genExpFrameDescriptor) {
        List<String> parametersToAdd = new ArrayList<>();
        List<FrameSlot> parameterSlotsToAdd = new ArrayList<>();

        for (ReadLevelVariableNode read : NodeUtil.findAllNodeInstances(root, ReadLevelVariableNode.class)) {
            String id = (String) read.getSlot().getIdentifier();
            if (!parametersToAdd.contains(id)) {
                parametersToAdd.add(id);
                parameterSlotsToAdd.add(genExpFrameDescriptor.findOrAddFrameSlot(id));
            }
        }

        return parameterSlotsToAdd;
    }

    private static void replaceParameters(List<FrameSlot> slots, FunctionRootNode root) {
        GeneratorReturnTargetNode body = NodeUtil.findFirstNodeInstance(root, GeneratorReturnTargetNode.class);
        PNode params = body.getParameters();
        PNode[] writes = new PNode[slots.size()];

        for (int i = 0; i < slots.size(); i++) {
            FrameSlot slot = slots.get(i);
            ReadArgumentNode read = new ReadArgumentNode(i);
            writes[i] = WriteLocalVariableNodeFactory.create(slot, read);
        }

        params.replace(new BlockNode(writes));

        /**
         * Uninitialized body.
         */
        ReturnTargetNode unitializedbody = (ReturnTargetNode) root.getUninitializedBody();
        PNode innerBody = unitializedbody.getBody();
        writes = new PNode[slots.size()];

        for (int i = 0; i < slots.size(); i++) {
            FrameSlot slot = slots.get(i);
            ReadArgumentNode read = new ReadArgumentNode(i);
            writes[i] = WriteLocalVariableNodeFactory.create(slot, read);
        }

        innerBody.replace(new BlockNode(new PNode[]{new BlockNode(writes), (PNode) innerBody.copy()}));
    }

    private static void replaceReadLevels(List<FrameSlot> localizedSlots, FunctionRootNode root) {
        for (FrameSlot slot : localizedSlots) {
            for (ReadLevelVariableNode read : NodeUtil.findAllNodeInstances(root, ReadLevelVariableNode.class)) {
                String id = (String) read.getSlot().getIdentifier();

                if (id.equals(slot.getIdentifier())) {
                    read.replace(ReadLocalVariableNodeFactory.create(slot));
                }
            }

            /**
             * Need to replace all read level in uninitialized body too. Make sure that after
             * possible inlining, real levels are still redirected to localized parameters.
             */
            for (ReadLevelVariableNode read : NodeUtil.findAllNodeInstances(root.getUninitializedBody(), ReadLevelVariableNode.class)) {
                String id = (String) read.getSlot().getIdentifier();

                if (id.equals(slot.getIdentifier())) {
                    read.replace(ReadLocalVariableNodeFactory.create(slot));
                }
            }
        }
    }
}
