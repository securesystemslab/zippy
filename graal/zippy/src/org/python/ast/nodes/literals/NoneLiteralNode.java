package org.python.ast.nodes.literals;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

public abstract class NoneLiteralNode extends TypedNode {

    public NoneLiteralNode() {
    }

    @Override
    public final boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        return false;
    }

    @Specialization
    public Object doGeneric() {
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + null + ")";
    }

}
