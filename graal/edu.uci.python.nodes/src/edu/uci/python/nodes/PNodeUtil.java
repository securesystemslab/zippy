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
package edu.uci.python.nodes;

import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.nodes.NodeUtil.*;

public class PNodeUtil {

    @SuppressWarnings("unchecked")
    public static <T> T getParentFor(PNode child, Class<T> parentClass) {
        if (parentClass.isInstance(child)) {
            throw new IllegalArgumentException();
        }

        Node current = child.getParent();
        while (!(current instanceof RootNode)) {
            if (parentClass.isInstance(current)) {
                return (T) current;
            }

            current = current.getParent();
        }

        throw new IllegalStateException();
    }

    public static List<PNode> getListOfSubExpressionsInOrder(PNode root) {
        List<PNode> expressions = new ArrayList<>();

        root.accept(new NodeVisitor() {
            public boolean visit(Node node) {
                if (node instanceof PNode) {
                    PNode pnode = (PNode) node;
                    for (Node child : pnode.getChildren()) {
                        if (child != null) {
                            expressions.add((PNode) child);
                        }
                    }
                }
                return true;
            }
        });

        return expressions;
    }

    /**
     * Added by zwei to makes it easier to find a matching node between a tree and its cloned tree.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Node> T findMatchingNodeIn(T toMatch, Node root) {
        for (Node candidate : NodeUtil.findAllNodeInstances(root, toMatch.getClass())) {
            if (matchNodes(toMatch, candidate)) {
                return (T) candidate;
            }
        }

        throw new IllegalStateException();
    }

    public static boolean matchNodes(Node toMatch, Node candidate) {
        if (!toMatch.getClass().equals(candidate.getClass())) {
            return false; // not the same exact class
        }

        NodeClass nodeClass = NodeClass.get(toMatch.getClass());

        /**
         * zwei: only compares parent node and data fields for now.
         */
        if (!nodeClassEquals(toMatch, candidate, nodeClass.getParentOffset())) {
            return false;
        }

        for (NodeField nfield : nodeClass.getFields()) {
            if (nfield.getKind() != NodeFieldKind.DATA) {
                continue;
            }

            /**
             * boolean fields are ignored.
             */
            if (nfield.getType().equals(boolean.class)) {
                continue;
            }

            if (!nodeFieldEquals(toMatch, candidate, nfield.getOffset())) {
                return false;
            }
        }

        return true;
    }

    private static boolean nodeClassEquals(Node toMatch, Node candidate, long nodeOffset) {
        Object left = CompilerDirectives.unsafeGetObject(toMatch, nodeOffset, false, null);
        Object right = CompilerDirectives.unsafeGetObject(candidate, nodeOffset, false, null);
        return left.getClass().equals(right.getClass());
    }

    private static boolean nodeFieldEquals(Node toMatch, Node candidate, long fieldOffset) {
        Object left = CompilerDirectives.unsafeGetObject(toMatch, fieldOffset, false, null);
        Object right = CompilerDirectives.unsafeGetObject(candidate, fieldOffset, false, null);
        return left == right;
    }

}
