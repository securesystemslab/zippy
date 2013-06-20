package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.ExecuteChildren;

@ExecuteChildren({ "leftNode", "rightNode" })
public abstract class BinaryOpNode extends TypedNode {

    @Child
    protected TypedNode leftNode;

    @Child
    protected TypedNode rightNode;

    public BinaryOpNode(TypedNode left, TypedNode right) {
        this.leftNode = adoptChild(left);
        this.rightNode = adoptChild(right);
    }

    public BinaryOpNode(BinaryOpNode node) {
        this(node.leftNode, node.rightNode);
        copyNext(node);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + leftNode + ", " + rightNode + ")";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        leftNode.visualize(level);
        rightNode.visualize(level);
    }

}
