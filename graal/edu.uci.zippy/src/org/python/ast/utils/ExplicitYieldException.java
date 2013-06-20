package org.python.ast.utils;

import org.python.ast.nodes.statements.*;

import com.oracle.truffle.api.nodes.*;

public class ExplicitYieldException extends ControlFlowException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Object value;

    private final StatementNode resumingNode;

    public ExplicitYieldException(StatementNode resumingNode, Object value) {
        this.resumingNode = resumingNode;
        this.value = value;
    }

    public StatementNode getResumingNode() {
        return resumingNode;
    }

    public Object getValue() {
        return value;
    }

}
