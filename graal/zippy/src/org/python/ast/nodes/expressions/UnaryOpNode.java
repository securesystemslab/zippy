package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.ExecuteChildren;

@ExecuteChildren({ "operand" })
public abstract class UnaryOpNode extends TypedNode {

    @Child
    protected TypedNode operand;

    public UnaryOpNode() {}

    public UnaryOpNode(TypedNode operand) {
        this.operand = adoptChild(operand);
    }

    protected UnaryOpNode(UnaryOpNode node) {
        this(node.operand);
        copyNext(node);
    }

    public TypedNode getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + operand + ")";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        operand.visualize(level);
    }

}
