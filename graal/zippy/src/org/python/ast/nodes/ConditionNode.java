package org.python.ast.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class ConditionNode extends LeftHandSideNode {

    public abstract boolean executeCondition(VirtualFrame frame);

}
