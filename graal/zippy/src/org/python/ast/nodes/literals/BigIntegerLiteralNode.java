package org.python.ast.nodes.literals;

import java.math.*;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class BigIntegerLiteralNode extends TypedNode {

    private final BigInteger value;

    public BigIntegerLiteralNode(BigInteger value) {
        this.value = value;
    }

    public BigInteger getValue() {
        return value;
    }

    @Specialization
    public BigInteger doBigInteger() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ")";
    }

}
