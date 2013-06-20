package org.python.ast.nodes;

import org.python.ast.nodes.statements.*;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class LeftHandSideNode extends StatementNode {

    public abstract void patchValue(TypedNode value);

    public abstract void doLeftHandSide(VirtualFrame frame, Object value);

}
