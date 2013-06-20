package org.python.ast.nodes;

import org.python.ast.datatypes.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

@ExecuteChildren({ "right" })
public abstract class MultiAssignmentNode extends TypedNode {

    @Children
    protected LeftHandSideNode[] targets;

    @Child
    protected TypedNode right;

    public MultiAssignmentNode(LeftHandSideNode[] targets, TypedNode right) {
        this.targets = adoptChildren(targets);
        this.right = adoptChild(right);
    }

    protected MultiAssignmentNode(MultiAssignmentNode node) {
        this(node.targets, node.right);
        copyNext(node);
    }

    public void patchValue(TypedNode right) {
        this.right = adoptChild(right);
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame, Object right) {
        PSequence val = (PSequence) right;
        int index = 0;

        Object[] values = val.getSequence();
        for (int i = 0; i < values.length; i++) {
            targets[index].doLeftHandSide(frame, values[i]);
            index++;
        }

        return right;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " = " + right;
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;

//        for (LeftHandSideNode target : targets) {
//            PythonTree p = (PythonTree) target;
//            p.visualize(level);
//        }
        right.visualize(level);
    }

}
