package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;
import org.python.core.truffle.GlobalScope;

import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.*;

public abstract class WriteGlobalNode extends TypedNode {

    @Child
    TypedNode right;

    private final String name;

    public WriteGlobalNode(String name, TypedNode right) {
        this.name = name;
        this.right = adoptChild(right);
    }

    protected WriteGlobalNode(WriteGlobalNode node) {
        this(node.name, node.right);
        copyNext(node);
    }

    public void patchValue(TypedNode right) {
        this.right = adoptChild(right);
    }

    @Override
    public void doLeftHandSide(VirtualFrame frame, Object value) {
        doGeneric(frame, value);
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame, Object value) {
        GlobalScope.getInstance().set(name, value);
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + name + ")";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }

        System.out.println(this);
    }

}
