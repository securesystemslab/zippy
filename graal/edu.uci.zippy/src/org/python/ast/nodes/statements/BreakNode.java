package org.python.ast.nodes.statements;

import com.oracle.truffle.api.frame.*;

public class BreakNode extends StatementNode {

    @Override
    public void executeVoid(VirtualFrame frame) {
        this.loopHeader.setBreak(true);
        // throw BreakException.breakException;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
