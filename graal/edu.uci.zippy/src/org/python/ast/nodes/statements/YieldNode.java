package org.python.ast.nodes.statements;

import org.python.ast.nodes.*;
import org.python.ast.utils.*;

import com.oracle.truffle.api.frame.*;

/**
 * Yield doesn't work yet.
 * 
 * @author zwei
 * 
 */
public class YieldNode extends StatementNode {

    @Child
    TypedNode right;

    public YieldNode(TypedNode right) {
        this.right = adoptChild(right);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        Object returnValue = right.executeGeneric(frame);
        throw new ExplicitYieldException(next(), returnValue);
    }

}
