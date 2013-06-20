package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;
import org.python.ast.utils.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.VirtualFrame;

@ExecuteChildren({})
public abstract class ListComprehensionNode extends TypedNode {

    @Child
    ComprehensionNode comprehension;

    public ListComprehensionNode(ComprehensionNode comprehension) {
        this.comprehension = adoptChild(comprehension);
    }

    protected ListComprehensionNode(ListComprehensionNode node) {
        this(node.comprehension);
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        try {
            comprehension.executeGeneric(frame);
        } catch (ExplicitReturnException ere) {
            return ere.getValue();
        }

        return null;
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        comprehension.visualize(level);
    }

}
