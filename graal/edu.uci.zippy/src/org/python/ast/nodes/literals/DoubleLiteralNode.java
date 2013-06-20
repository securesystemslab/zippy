package org.python.ast.nodes.literals;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class DoubleLiteralNode extends TypedNode {

    private final double value;

    public DoubleLiteralNode(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Specialization
    public double doDouble() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ")";
    }
    
    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }

        System.out.println(this);
    }

}
