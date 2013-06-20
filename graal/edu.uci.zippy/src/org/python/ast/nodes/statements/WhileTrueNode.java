package org.python.ast.nodes.statements;

import org.python.ast.*;
import org.python.ast.utils.*;

import com.oracle.truffle.api.frame.*;

public class WhileTrueNode extends StatementNode {

    @Child
    protected BlockNode body;

    public WhileTrueNode(BlockNode body) {
        this.body = adoptChild(body);
    }
    
    public void setInternal(BlockNode body) {
        this.body = adoptChild(body);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        //try {
        while (true) {
            try {
                body.executeVoid(frame);
                if (reachedReturn() || isBreak()) {
                    this.isBreak = false;
                    return;
                }
            } catch (ContinueException ex) {
                // Fall through to next loop iteration.
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + "(true)";
    }

    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitWhileTrueNode(this);
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        body.visualize(level);
    }

}
