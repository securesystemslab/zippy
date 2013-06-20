package org.python.ast.nodes.literals;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class ComplexLiteralNode extends TypedNode {

    private final PComplex value;

    public ComplexLiteralNode(PComplex value) {
        this.value = value;
    }

    public PComplex getValue() {
        return value;
    }

    @Specialization
    public PComplex doComplex() {
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
