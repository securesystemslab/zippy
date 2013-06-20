package org.python.ast.nodes.statements;

import org.python.ast.StatementVisitor;

import com.oracle.truffle.api.frame.VirtualFrame;

public class OneStmtBlockNode extends BlockNode {

    @Child
    protected StatementNode statement;
    
    public OneStmtBlockNode(StatementNode statement) {
        super(null);
        this.statement = adoptChild(statement);
    }
    
    @Override
    public void executeVoid(VirtualFrame frame) {
        statement.executeVoid(frame);
    }

    //TODO
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

        statement.visualize(level);
    }

}
