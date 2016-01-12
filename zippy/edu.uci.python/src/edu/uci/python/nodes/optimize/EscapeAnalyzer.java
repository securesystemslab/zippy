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

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.statement.*;

public class EscapeAnalyzer {

    private final RootNode root;
    private final Node targetExpression;

    private FrameSlot localSlot; // FrameSlot that stores the value of the target expression.

    public EscapeAnalyzer(RootNode root, Node targetExpression) {
        this.root = root;
        this.targetExpression = targetExpression;
    }

    public boolean escapes() {
        return escapesCurrentFrame(targetExpression);
    }

    public boolean isBoundToLocalFrame() {
        return localSlot != null;
    }

    public FrameSlot getTargetExpressionSlot() {
        assert localSlot != null;
        return localSlot;
    }

    private void updateTargetExpressionSlot(FrameSlot newSlot) {
        localSlot = localSlot == null ? newSlot : localSlot;
    }

    private boolean escapesCurrentFrame(Node currentTarget) {
        Node current = currentTarget;

        while (!isStatementNode(current)) {
            current = current.getParent();

            if (current instanceof WriteLocalVariableNode) {
                FrameSlot slot = ((WriteLocalVariableNode) current).getSlot();
                updateTargetExpressionSlot(slot);
                return escapesCurrentFrame(slot);
            } else if (current instanceof WriteNode) {
                return true; // Other write nodes
            } else if (current instanceof PythonCallNode) {
                return !((PythonCallNode) current).isInlined();
            } else if (current instanceof ReturnNode) {
                return true;
            }
        }

        return false;
    }

    /**
     * Only local reads are effectively analyzed.<br>
     * Since any local write is a statement by it self, and the recursive call to
     * escapesCurrentFrame() always return false.
     */
    private boolean escapesCurrentFrame(FrameSlot slot) {
        if (slot.getIdentifier().equals("<return_val>")) {
            return true;
        }

        for (FrameSlotNode slotNode : NodeUtil.findAllNodeInstances(root, FrameSlotNode.class)) {
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

}
