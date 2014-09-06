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
package edu.uci.python.profiler;

import java.util.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.profiler.*;
import edu.uci.python.nodes.subscript.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem
 */

public class ProfilerTranslator implements NodeVisitor {

    private final PythonProfilerNodeProber profilerProber;
    private final ProfilerResultPrinter resultPrinter;
    private final PythonContext context;

    public ProfilerTranslator(PythonContext context) {
        this.context = context;
        // this.profilerProber = new PythonProfilerNodeProber(context);
        // this.profilerProber = new PythonProfilerNodeProber();
        this.profilerProber = PythonProfilerNodeProber.getInstance();
        this.resultPrinter = new ProfilerResultPrinter(this.profilerProber);
    }

    public void translate(PythonParseResult parseResult) {
        RootNode root = parseResult.getModuleRoot();
        root.accept(this);

        for (RootNode functionRoot : parseResult.getFunctionRoots()) {
            functionRoot.accept(this);
        }
    }

    @Override
    public boolean visit(Node node) {
        if (PythonOptions.ProfileCalls) {
            profileCalls(node);
        }

        if (PythonOptions.ProfileControlFlow) {
            profileControlFlow(node);
        }

        if (PythonOptions.ProfileVariableAccesses) {
            profileVariables(node);
        }

        if (PythonOptions.ProfileOperations) {
            profileOperations(node);
        }

        if (PythonOptions.ProfileAttributesElements) {
            profileAttributesElements(node);
        }

        return true;
    }

    private void profileCalls(Node node) {
        if (node instanceof FunctionRootNode) {
            FunctionRootNode rootNode = (FunctionRootNode) node;
            PNode body = rootNode.getBody();
            createCallWrapper(body);
        }
    }

    private void profileControlFlow(Node node) {
        profileLoops(node);
        profileIfs(node);
        profileBreakContinues(node);
    }

    private void profileLoops(Node node) {
        /**
         * TODO Currently generator loops are not profiled
         */
        if (node instanceof LoopNode && !(node instanceof GeneratorForNode) && !(node instanceof GeneratorWhileNode)) {
            LoopNode loopNode = (LoopNode) node;
            PNode loopBodyNode = loopNode.getBody();
            createLoopBodyWrapper(loopBodyNode);
        }
    }

    private void profileIfs(Node node) {
        if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            if (hasSourceSection(ifNode)) {
                PNode thenNode = ifNode.getThen();
                PNode elseNode = ifNode.getElse();
                /**
                 * Only create a wrapper node for the else part if the else part exists.
                 */
                if (elseNode instanceof EmptyNode) {
                    if (checkSourceSection(thenNode)) {
                        createIfWithoutElseWrappers(ifNode, thenNode);
                    }
                } else {
                    if (checkSourceSection(thenNode) && checkSourceSection(elseNode)) {
                        createIfWrappers(ifNode, thenNode, elseNode);
                    }
                }
            }
        }
    }

    private void profileBreakContinues(Node node) {
        if (node instanceof BreakNode || node instanceof ContinueNode) {
            createBreakContinueWrapper((PNode) node);
        }
    }

    private void profileVariables(Node node) {
        if (!(node.getParent() instanceof PythonCallNode)) {
            if (node instanceof WriteLocalVariableNode) {
                createReadWriteWrapper((PNode) node);
            } else if (node instanceof ReadLocalVariableNode) {
                createReadWriteWrapper((PNode) node);
            } else if (node instanceof ReadLevelVariableNode) {
                createReadWriteWrapper((PNode) node);
            } else if (node instanceof ReadGlobalNode) {
                createReadWriteWrapper((PNode) node);
            }
        }
    }

    private void profileOperations(Node node) {
        if (!(node.getParent() instanceof PythonCallNode)) {
            if (node instanceof BinaryArithmeticNode) {
                createOperationWrapper((PNode) node);
            } else if (node instanceof BinaryBitwiseNode) {
                createOperationWrapper((PNode) node);
            } else if (node instanceof BinaryBooleanNode) {
                createOperationWrapper((PNode) node);
            } else if (node instanceof BinaryComparisonNode) {
                createOperationWrapper((PNode) node);
            } else if (node instanceof UnaryArithmeticNode) {
                createOperationWrapper((PNode) node);
            } else if (node instanceof BreakNode) {
                createOperationWrapper((PNode) node);
            } else if (node instanceof ContinueNode) {
                createOperationWrapper((PNode) node);
            }
        }
    }

    private void profileAttributesElements(Node node) {
        if (!(node.getParent() instanceof PythonCallNode)) {
            if (node instanceof SetAttributeNode) {
                createAttributeElementWrapper((PNode) node);
            } else if (node instanceof GetAttributeNode) {
                createAttributeElementWrapper((PNode) node);
            } else if (node instanceof SubscriptLoadIndexNode) {
                createAttributeElementWrapper((PNode) node);
            } else if (node instanceof SubscriptLoadSliceNode) {
                createAttributeElementWrapper((PNode) node);
            } else if (node instanceof SubscriptDeleteNode) {
                createAttributeElementWrapper((PNode) node);
            } else if (node instanceof SubscriptStoreIndexNode) {
                createAttributeElementWrapper((PNode) node);
            } else if (node instanceof SubscriptStoreSliceNode) {
                createAttributeElementWrapper((PNode) node);
            }
        }
    }

    private PythonWrapperNode createCallWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = profilerProber.probeAsCall(node, context);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createLoopBodyWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = profilerProber.probeAsLoop(node, context);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private void createIfWrappers(IfNode ifNode, PNode thenNode, PNode elseNode) {
        List<PythonWrapperNode> wrappers = profilerProber.probeAsIf(ifNode, thenNode, elseNode, context);
        replaceNodeWithWrapper(ifNode, wrappers.get(0));
        replaceNodeWithWrapper(thenNode, wrappers.get(1));
        replaceNodeWithWrapper(elseNode, wrappers.get(2));
    }

    private void createIfWithoutElseWrappers(PNode ifNode, PNode thenNode) {
        List<PythonWrapperNode> wrappers = profilerProber.probeAsIfWithoutElse(ifNode, thenNode, context);
        replaceNodeWithWrapper(ifNode, wrappers.get(0));
        replaceNodeWithWrapper(thenNode, wrappers.get(1));
    }

    private PythonWrapperNode createBreakContinueWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = profilerProber.probeAsBreakContinue(node, context);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createReadWriteWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = profilerProber.probeAsVariableAccess(node, context);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createOperationWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = profilerProber.probeAsOperation(node, context);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createAttributeElementWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = profilerProber.probeAsAttributeElement(node, context);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = profilerProber.probeAsNode(node, context);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private static void replaceNodeWithWrapper(PNode node, PythonWrapperNode wrapperNode) {
        /**
         * If a node is already wrapped, then another wrapper node is not created, and existing
         * wrapper node is used. If a wrapper node is not created, do not replace the node,
         * otherwise replace the node with the new created wrapper node
         */
        if (!wrapperNode.equals(node.getParent())) {
            node.replace(wrapperNode);
            wrapperNode.adoptChildren();
        }
    }

    private boolean checkSourceSection(PNode node) {
        if (hasSourceSection(node)) {
            if (context.hasProbe(node.getSourceSection())) {
                resultPrinter.addNodeUsingExistingProbe(node);
            }

            return true;
        }

        return false;
    }

    private boolean hasSourceSection(PNode node) {
        if (node.getSourceSection() == null) {
            resultPrinter.addNodeEmptySourceSection(node);
            return false;
        }

        return true;
    }

    public ProfilerResultPrinter getProfilerResultPrinter() {
        return resultPrinter;
    }
}
