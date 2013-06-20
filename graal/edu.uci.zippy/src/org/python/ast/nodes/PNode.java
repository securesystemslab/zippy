package org.python.ast.nodes;

import org.python.ast.PNodeVisitor;
import org.python.core.truffle.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.nodes.*;

/**
 * This should be the base of all PythonNodes
 */
@TypeSystemReference(PythonTypes.class)
public abstract class PNode extends Node implements Visualizable {

//    @Override
//    public String toString() {
//        return NodeUtil.printTreeToString(this);
//    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }

        System.out.println(this);
    }
    
    public void accept(PNodeVisitor visitor) {
        throw new RuntimeException("Unexpected node: " + this);
    }
}
