package org.python.ast.nodes;

import org.python.ast.datatypes.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

@ExecuteChildren({ "right" })
public abstract class TwoAssignmentNode extends TypedNode {

    @Children
    protected LeftHandSideNode target0;
    
    @Children
    protected LeftHandSideNode target1;

    @Child
    protected TypedNode right;

    public TwoAssignmentNode(LeftHandSideNode target0, LeftHandSideNode target1, TypedNode right) {
        this.target0 = adoptChild(target0);
        this.target1 = adoptChild(target1);
        this.right = adoptChild(right);
    }

    protected TwoAssignmentNode(TwoAssignmentNode node) {
        this(node.target0, node.target1, node.right);
        copyNext(node);
    }

    public void patchValue(TypedNode right) {
        this.right = adoptChild(right);
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame, Object right) {
        Object[] values = ((PSequence) right).getSequence();
        
        target0.doLeftHandSide(frame, values[0]);
        target1.doLeftHandSide(frame, values[1]);

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
