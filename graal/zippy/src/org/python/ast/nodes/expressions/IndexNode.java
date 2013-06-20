package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class IndexNode extends UnaryOpNode {

    public IndexNode(TypedNode operand) {
        super(operand);
    }

    protected IndexNode(IndexNode node) {
        super(node);
    }

    @Specialization
    public int doInteger(int index) {
        return index;
    }

    @Generic
    public Object doGeneric(Object index) {
        return index;
    }

}
