package edu.uci.python.nodes.calls;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.*;

/**
 * @author Gulfem
 */

public class PythonBuiltinRootNode extends RootNode {

    @Child private PythonBuiltinNode builtinNode;

    public PythonBuiltinRootNode(PythonBuiltinNode builtinNode) {
        this.builtinNode = adoptChild(builtinNode);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return builtinNode.execute(frame);
    }

    @Override
    public String toString() {
        return "<Builtin function " + builtinNode.toString() + " at " + Integer.toHexString(hashCode()) + ">";
    }
}
