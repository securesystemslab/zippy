package org.python.ast.nodes.statements;

import java.util.*;

import org.python.ast.*;
import org.python.ast.nodes.*;
import org.python.core.*;

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

        if (Options.debug) {
            new LiearizedASTPrinter().print();
        }

        return getHead();
    }

    private StatementNode getHead() {
        if (root.statements.length != 0) {
            return root.statements[0];
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
        for (StatementNode s : node.statements) {
            last = visit(s);
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
        StatementNode thenLast = visitBlockNode(node.thenPartNode);
        visitBlockNode(node.elsePartNode);
        setNext(thenLast, merge);
        append(merge);

        return merge;
    }
    
    @Override
    public StatementNode visitIfNotNode(IfNotNode node) {
        append(node);

        StatementNode merge = getDummy();
        StatementNode thenLast = visitBlockNode(node.thenPartNode);
        visitBlockNode(node.elsePartNode);
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
    
    @Override
    public StatementNode visitWhileTrueNode(WhileTrueNode node) {
        append(node);
        StatementNode loopEnd = getDummy();
        StatementNode bodyLast = visitBlockNode(node.body);

        setNext(bodyLast, node);
        setNext(node, loopEnd);
        append(loopEnd);
        return loopEnd;
    }

    private StatementNode getDummy() {
        return (StatementNode) TypedNode.DUMMY.copy();
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
            int id = getId(node);
            int nextId = getId(node.next());
            System.out.println(id + " " + node + " -> " + nextId);
            return null;
        }

        @Override
        public StatementNode visitBlockNode(BlockNode node) {
            StatementNode last = null;
            for (StatementNode s : node.statements) {
                visit(s);
                last = s;
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
            visit(node.thenPartNode);
            StatementNode next = visit(node.elsePartNode).next();
            visitStatementNode(next);
            return null;
        }
        
        @Override
        public StatementNode visitIfNotNode(IfNotNode node) {
            visitStatementNode(node);
            visit(node.thenPartNode);
            StatementNode next = visit(node.elsePartNode).next();
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

        @Override
        public StatementNode visitWhileTrueNode(WhileTrueNode node) {
            visitStatementNode(node);
            StatementNode next = visit(node.body).next();
            visitStatementNode(next);
            return null;
        }
    }

}
