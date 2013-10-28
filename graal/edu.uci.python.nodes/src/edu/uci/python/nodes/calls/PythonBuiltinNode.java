package edu.uci.python.nodes.calls;

import com.oracle.truffle.api.dsl.*;
import edu.uci.python.nodes.PNode;

/**
 * @author Gulfem
 */

@NodeChild(value = "arguments", type = PNode[].class)
public abstract class PythonBuiltinNode extends PNode {

    private final String name;

    public PythonBuiltinNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
