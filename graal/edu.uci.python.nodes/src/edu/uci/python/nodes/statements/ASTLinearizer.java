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
package edu.uci.python.nodes.statements;

import java.util.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.translation.*;
import edu.uci.python.nodes.truffle.*;

public class ASTLinearizer implements StatementVisitor<StatementNode> {

    private final BlockNode root;

    private StatementNode current;

    private Map<StatementNode, Integer> nodeToId = new HashMap<>();

    private int id = 0;

    public ASTLinearizer(BlockNode root) {
        this.root = root;
    }

    public StatementNode linearize() {
        visit(root);
        return getHead();
    }

    private StatementNode getHead() {
        if (root.statements.length != 0) {
            return (StatementNode) root.statements[0];
        }

        return root;
    }

    protected StatementNode visit(StatementNode node) {
        return node.accept(this);
    }

    @Override
    public StatementNode visitStatementNode(StatementNode node) {
        append(node);
        return node;
    }

    @Override
    public StatementNode visitBlockNode(BlockNode node) {
        StatementNode last = null;

        // expands a Block if it has child statements.
        for (PNode s : node.statements) {
            last = visit((StatementNode) s);
        }

        // keep Block as a place holder if it does not have child statements.
        if (node.statements.length == 0) {
            last = visitStatementNode(node);
        }

        return last;
    }

    @Override
    public StatementNode visitForNode(ForNode node) {
        ForNode newNode = new ForNode.GeneratorForNode(node);
        node.replace(newNode);
        append(newNode);

        StatementNode loopEnd = getDummy();
        StatementNode bodyLast = visitBlockNode(node.body);
        visitBlockNode(node.orelse);

        setNext(bodyLast, newNode);
        setNext(newNode, loopEnd);
        append(loopEnd);
        return loopEnd;
    }

    @Override
    public StatementNode visitIfNode(IfNode node) {
        append(node);

        StatementNode merge = getDummy();
        StatementNode thenLast = visitBlockNode(node.then);
        visitBlockNode(node.orelse);
        setNext(thenLast, merge);
        append(merge);

        return merge;
    }

    @Override
    public StatementNode visitWhileNode(WhileNode node) {
        append(node);
        StatementNode loopEnd = getDummy();
        StatementNode bodyLast = visitBlockNode(node.body);
        visitBlockNode(node.orelse);

        setNext(bodyLast, node);
        setNext(node, loopEnd);
        append(loopEnd);
        return loopEnd;
    }

    private static StatementNode getDummy() {
        return (StatementNode) PNode.EMPTYNODE.copy();
    }

    void append(StatementNode next) {
        if (current != null) {
            current.setNext(next);
        }

        if (next != null) {
            current = next;
            nodeToId.put(next, id++);
        }
    }

    void setNext(StatementNode current, StatementNode next) {
        if (next != null && current != null) {
            current.setNext(next);
        }
    }

    class LiearizedASTPrinter implements StatementVisitor<StatementNode> {

        int getId(StatementNode node) {
            if (node == null) {
                return -1;
            }

            return nodeToId.get(node);
        }

        public void print() {
            visit(root);
        }

        protected StatementNode visit(StatementNode node) {
            return node.accept(this);
        }

        @Override
        public StatementNode visitStatementNode(StatementNode node) {
            int sid = getId(node);
            int nextId = getId(node.next());
            ASTInterpreter.trace(sid + " " + node + " -> " + nextId);
            return null;
        }

        @Override
        public StatementNode visitBlockNode(BlockNode node) {
            StatementNode last = null;
            for (PNode s : node.statements) {
                visit((StatementNode) s);
                last = (StatementNode) s;
            }

            if (node.statements.length == 0) {
                visitStatementNode(node);
                last = node;
            }

            return last;
        }

        @Override
        public StatementNode visitForNode(ForNode node) {
            visitStatementNode(node);
            visit(node.body);
            StatementNode next = visit(node.orelse).next();
            visitStatementNode(next);
            return null;
        }

        @Override
        public StatementNode visitIfNode(IfNode node) {
            visitStatementNode(node);
            visit(node.then);
            StatementNode next = visit(node.orelse).next();
            visitStatementNode(next);
            return null;
        }

        @Override
        public StatementNode visitWhileNode(WhileNode node) {
            visitStatementNode(node);
            visit(node.body);
            StatementNode next = visit(node.orelse).next();
            visitStatementNode(next);
            return null;
        }
    }
}
