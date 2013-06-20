package org.python.ast.nodes.statements;

import org.python.ast.*;
import org.python.ast.nodes.ConditionNode;
import org.python.ast.utils.*;

import com.oracle.truffle.api.frame.*;

public class WhileNode extends StatementNode {

    @Child
    protected ConditionNode condition;

    @Child
    protected BlockNode body;

    @Child
    protected BlockNode orelse;

    public WhileNode(ConditionNode condition, BlockNode body, BlockNode orelse) {
        this.condition = adoptChild(condition);
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }
    
    public void setInternal(BlockNode body, BlockNode orelse) {
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        //try {
        while (condition.executeCondition(frame)) {
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
        //} catch (BreakException ex) {
            // Done executing this loop, exit method to execute statement
            // following the loop.
        //    return;
        //}

        /**
         * while for might have an orelse part which is only executed when loop
         * terminates regularly(without break)
         */
        orelse.executeVoid(frame);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + condition + ")";
    }

    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitWhileNode(this);
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        condition.visualize(level);
        body.visualize(level);
    }

}
