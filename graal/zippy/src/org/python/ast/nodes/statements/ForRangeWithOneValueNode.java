package org.python.ast.nodes.statements;

import org.python.ast.nodes.LeftHandSideNode;
import org.python.ast.nodes.TypedNode;
import org.python.ast.utils.*;

import com.oracle.truffle.api.frame.*;

public class ForRangeWithOneValueNode extends StatementNode {
    @Child
    protected LeftHandSideNode target;
    
    @Child
    protected TypedNode stop;

    @Child
    protected BlockNode body;

    @Child
    protected BlockNode orelse;

    public ForRangeWithOneValueNode(LeftHandSideNode target, TypedNode stop, BlockNode body, BlockNode orelse) {
        this.target = adoptChild(target);
        this.stop = adoptChild(stop);
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }
    
    public void setInternal(BlockNode body, BlockNode orelse) {
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {        
        // TODO need to check for bigInteger
        int stop = (int) this.stop.executeGeneric(frame);
        for (int i = 0; i < stop; i++) {
            //try {
            target.doLeftHandSide(frame, i);

            try {
                body.executeVoid(frame);
                if (reachedReturn() || isBreak()) {
                    this.isBreak = false;
                    return;
                }
            } catch (ContinueException ex) {
                // Fall through to next loop iteration.
            }
            //} catch (BreakException ex) {
                // Done executing this loop.
                // If there is a break, orelse should not be executed
            //    return;
            //}
            orelse.executeVoid(frame);
        }
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
//        PythonTree p = (PythonTree) target;
//        p.visualize(level);        
        body.visualize(level);
    }
}
