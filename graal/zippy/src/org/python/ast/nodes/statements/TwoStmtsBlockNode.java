package org.python.ast.nodes.statements;

import org.python.ast.StatementVisitor;

import com.oracle.truffle.api.frame.VirtualFrame;

public class TwoStmtsBlockNode extends BlockNode {

    @Child
    protected StatementNode statement0;
    
    @Child
    protected StatementNode statement1;
    
    public TwoStmtsBlockNode(StatementNode statement0, StatementNode statement1) {
        super(null);
        this.statement0 = adoptChild(statement0);
        this.statement1 = adoptChild(statement1);
    }
    
    @Override
    public void executeVoid(VirtualFrame frame) {
        statement0.executeVoid(frame);
        if (!(reachedReturn() || reachedBreak()))
            statement1.executeVoid(frame);
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

        statement0.visualize(level);
        statement1.visualize(level);
    }

}
