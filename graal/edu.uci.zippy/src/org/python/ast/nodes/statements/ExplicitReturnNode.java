package org.python.ast.nodes.statements;

import org.python.ast.nodes.*;

import com.oracle.truffle.api.frame.*;

public class ExplicitReturnNode extends ReturnNode {

    @Child
    protected final TypedNode right;

    public ExplicitReturnNode(TypedNode right) {
        this.right = adoptChild(right);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        Object returnValue = right.executeGeneric(frame);
        // throw new ExplicitReturnException(returnValue);
        funcRoot.setReturn(true, returnValue);
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        right.visualize(level);
    }

}
