package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

@ExecuteChildren({ "first", "second", "third" })
public abstract class TernaryOpNode extends TypedNode {

    @Child
    protected TypedNode first;

    @Child
    protected TypedNode second;

    @Child
    protected TypedNode third;

    public TernaryOpNode(TypedNode first, TypedNode second, TypedNode third) {
        this.first = adoptChild(first);
        this.second = adoptChild(second);
        this.third = adoptChild(third);
    }

    protected TernaryOpNode(TernaryOpNode node) {
        this(node.first, node.second, node.third);
        copyNext(node);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + first + ", " + second + ", " + third + ")";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        if (first != null) {
            first.visualize(level);
        }
        if (second != null) {
            second.visualize(level);
        }
        if (third != null) {
            third.visualize(level);
        }
    }

}
