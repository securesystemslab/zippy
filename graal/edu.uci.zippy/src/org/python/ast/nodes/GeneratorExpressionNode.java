package org.python.ast.nodes;

import org.python.ast.nodes.expressions.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

@ExecuteChildren({})
public abstract class GeneratorExpressionNode extends TypedNode {

    @Child
    GeneratorNode generator;

    private final FrameDescriptor frameDescriptor;

    public GeneratorExpressionNode(GeneratorNode generator, FrameDescriptor descriptor) {
        this.generator = adoptChild(generator);
        this.frameDescriptor = descriptor;
    }

    protected GeneratorExpressionNode(GeneratorExpressionNode node) {
        this(node.generator, node.frameDescriptor);
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        CallTarget ct = Truffle.getRuntime().createCallTarget(generator, frameDescriptor);

        // TODO: This is probably not the best way to determine whether the
        // generator should be evaluated immediately or not.
        if (getParent() instanceof WriteLocalNode || getParent() instanceof WriteGlobalNode) {
            return ct;
        } else {
            return ct.call(frame.pack());
        }
    }

}
