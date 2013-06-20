package org.python.ast.utils;

import com.oracle.truffle.api.nodes.ControlFlowException;

public class ExplicitReturnException extends ControlFlowException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Object value;

    public ExplicitReturnException(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

}
