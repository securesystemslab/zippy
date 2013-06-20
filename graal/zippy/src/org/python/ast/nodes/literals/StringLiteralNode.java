package org.python.ast.nodes.literals;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class StringLiteralNode extends TypedNode {

    private final String value;
    
    public StringLiteralNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Specialization
    public String doString() {
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
