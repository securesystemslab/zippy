package org.python.ast.nodes.statements;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ReturnNode extends StatementNode {

    @Override
    public void executeVoid(VirtualFrame frame) {
        funcRoot.setReturn(true, null);
        // throw new ImplicitReturnException();
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

    }

}
