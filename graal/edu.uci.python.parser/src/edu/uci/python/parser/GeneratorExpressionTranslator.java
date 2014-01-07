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

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.*;

public class GeneratorExpressionTranslator {

    private final PythonParseResult parseResult;
    private RootNode currentRoot;
    private GeneratorExpressionDefinitionNode currentDef;

    public GeneratorExpressionTranslator(PythonParseResult parseResult) {
        this.parseResult = parseResult;
    }

    public void translate() {
        for (RootNode functionRoot : parseResult.getFunctionRoots()) {
            translateRoot(functionRoot);
        }
    }

    private void translateRoot(RootNode root) {
        currentRoot = root;

        for (GeneratorExpressionDefinitionNode genExp : NodeUtil.findAllNodeInstances(root, GeneratorExpressionDefinitionNode.class)) {
            currentDef = genExp;

            // CheckStyle: stop system..print check
            if (escapesCurrentFrame(currentDef)) {
                parseResult.getContext().getStandardOut().println("[ZipPy] escapse analysis: " + currentDef + " escapes current frame");
            } else {
                parseResult.getContext().getStandardOut().println("[ZipPy] escapse analysis: " + currentDef + " does not escape current frame");
            }
            // CheckStyle: resume system..print check
        }
    }

    private boolean escapesCurrentFrame(Node target) {
        Node current = target;

        while (!isStatementNode(current)) {
            current = current.getParent();

            if (current instanceof WriteLocalVariableNode) {
                FrameSlot slot = ((WriteLocalVariableNode) current).getSlot();
                escapesCurrentFrame(slot);
            } else if (current instanceof WriteNode) {
                // Other write nodes
                return true;
            } else if (current instanceof CallFunctionNode) {
                String callee = ((CallFunctionNode) current).getCallee().toString();
                return isBuiltinConstructor(callee) ? false : true;
            }
        }

        return false;
    }

    private boolean escapesCurrentFrame(FrameSlot slot) {
        for (FrameSlotNode slotNode : NodeUtil.findAllNodeInstances(currentRoot, FrameSlotNode.class)) {
            if (slotNode.getSlot().equals(slot)) {
                return escapesCurrentFrame(slotNode);
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
}
