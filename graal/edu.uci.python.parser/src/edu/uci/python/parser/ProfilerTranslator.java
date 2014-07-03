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
package edu.uci.python.parser;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.profiler.*;
import edu.uci.python.nodes.subscript.*;
import edu.uci.python.runtime.*;

public class ProfilerTranslator implements NodeVisitor {

    private final PythonNodeProber astProber;

    public ProfilerTranslator(PythonContext context) {
        this.astProber = new PythonNodeProber(context);
    }

    public void translate(PythonParseResult parseResult, RootNode root) {
        root.accept(this);

        for (RootNode functionRoot : parseResult.getFunctionRoots()) {
            functionRoot.accept(this);
        }
    }

    @Override
    public boolean visit(Node node) {
        if (PythonOptions.ProfileCalls) {
            if (node instanceof FunctionRootNode) {
                FunctionRootNode rootNode = (FunctionRootNode) node;
                PNode body = rootNode.getBody();
                createCallNodeWrapper(body);
            }
        } else if (PythonOptions.ProfileIfNodes) {
            if (node instanceof IfNode) {
                IfNode ifNode = (IfNode) node;
                /**
                 * 1) If node has a condition node which is a castToBooleanNode. <br>
                 * CastToBooleanNode has a child which is the actual condition. So be careful while
                 * profiling if nodes. Do not profile if nodes and condition nodes together, because
                 * prober increments counter twice for the same node. <br>
                 * 2) If nodes in a comprehension does not yet have a source section, so such if
                 * nodes are not profiled.
                 */
                if (hasSourceSection(ifNode)) {
                    createIfWrapper(ifNode);
                    PNode thenNode = ifNode.getThen();
                    createThenNodeWrapper(thenNode);
                    PNode elseNode = ifNode.getElse();
                    /**
                     * Only create a wrapper node if an else part exists.
                     */
                    if (!(elseNode instanceof EmptyNode)) {
                        createElseNodeWrapper(elseNode);

                    }

                }
            }
        } else if (PythonOptions.ProfileNodes) {
            /**
             * Profile binary operations:BinaryArithmeticNode, BinaryBitwiseNode, BinaryBooleanNode,
             * BinaryComparisonNode, SubscriptLoadIndexNode, SubscriptLoadSliceNode,
             * SubscriptDeleteNode
             */
            if (!(node.getParent() instanceof PythonCallNode)) {
                /**
                 * PythonCallNode has primaryNode and calleeNode. primaryNode is extracted from
                 * calleeNode, so primary should not be profiled twice
                 */
                if (node instanceof BinaryArithmeticNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof BinaryBitwiseNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof BinaryBooleanNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof BinaryComparisonNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof SubscriptLoadIndexNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof SubscriptLoadSliceNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof SubscriptDeleteNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof SubscriptStoreIndexNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof SubscriptStoreSliceNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof WriteLocalVariableNode) {
                    createWriteNodeWrapper((PNode) node);
                } else if (node instanceof ReadLocalVariableNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof SetAttributeNode) {
                    createWriteNodeWrapper((PNode) node);
                } else if (node instanceof GetAttributeNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof BreakNode) {
                    createWrapper((PNode) node);
                } else if (node instanceof ContinueNode) {
                    createWrapper((PNode) node);
                }
            }
        }

        return true;
    }

    private PythonWrapperNode createWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = astProber.probeAsStatement(node);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createIfWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = astProber.probeAsIfStatement(node);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWriteNodeWrapperNode createWriteNodeWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWriteNodeWrapperNode wrapperNode = astProber.probeAsWriteNode(node);
            node.replace(wrapperNode);
            wrapperNode.adoptChildren();
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createCallNodeWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = astProber.probeAsCall(node);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createThenNodeWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = astProber.probeAsThen(node);
            replaceNodeWithWrapper(node, wrapperNode);
            return wrapperNode;
        }

        return null;
    }

    private PythonWrapperNode createElseNodeWrapper(PNode node) {
        if (checkSourceSection(node)) {
            PythonWrapperNode wrapperNode = astProber.probeAsElse(node);
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
            if (astProber.getContext().hasProbe(node.getSourceSection())) {
                ProfilerResultPrinter.addNodeUsingExistingProbe(node);
            }

            return true;
        }

        return false;
    }

    private static boolean hasSourceSection(PNode node) {
        if (node.getSourceSection() == null) {
            ProfilerResultPrinter.addNodeEmptySourceSection(node);
            return false;
        }

        return true;
    }
}
