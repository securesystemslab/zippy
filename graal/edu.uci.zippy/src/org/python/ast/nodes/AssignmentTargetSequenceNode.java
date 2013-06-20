package org.python.ast.nodes;

import java.util.Iterator;

import org.python.ast.datatypes.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class AssignmentTargetSequenceNode extends TypedNode {

    protected LeftHandSideNode[] targets;

    public AssignmentTargetSequenceNode(LeftHandSideNode[] targets) {
        this.targets = adoptChildren(targets);
    }

    protected AssignmentTargetSequenceNode(AssignmentTargetSequenceNode node) {
        this(node.targets);
        copyNext(node);
    }

    @Override
    public void doLeftHandSide(VirtualFrame frame, Object value) {
        PSequence sequence = (PSequence) value;
        Iterator<?> iter = sequence.iterator();
        int i = 0;

        while (iter.hasNext()) {
            targets[i].doLeftHandSide(frame, iter.next());
            i++;
        }
    }

    @Specialization
    Object doGeneric() {
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
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
    }

}
