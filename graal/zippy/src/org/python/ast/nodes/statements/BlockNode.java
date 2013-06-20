package org.python.ast.nodes.statements;

import org.python.ast.*;

import com.oracle.truffle.api.frame.VirtualFrame;

public class BlockNode extends StatementNode {

    @Children
    protected final StatementNode[] statements;

    public BlockNode(StatementNode[] statements) {
        this.statements = adoptChildren(statements);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        for (int i = 0; i< statements.length; i++) {
            statements[i].executeVoid(frame);
            if (reachedReturn() || reachedBreak())
                break;
        }
    }

    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitBlockNode(this);
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;

        for (StatementNode statement : statements) {
            statement.visualize(level);
        }
    }

}
