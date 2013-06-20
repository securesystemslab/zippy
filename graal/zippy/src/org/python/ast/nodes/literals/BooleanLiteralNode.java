package org.python.ast.nodes.literals;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class BooleanLiteralNode extends TypedNode {

    private final boolean value;

    public BooleanLiteralNode(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Specialization
    public boolean doBoolean() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ")";
    }

}
