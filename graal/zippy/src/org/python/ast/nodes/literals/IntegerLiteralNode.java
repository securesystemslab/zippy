package org.python.ast.nodes.literals;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

public abstract class IntegerLiteralNode extends TypedNode {

    private final int value;

    public IntegerLiteralNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Specialization
    public int doInteger(VirtualFrame frame) throws UnexpectedResultException {
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
