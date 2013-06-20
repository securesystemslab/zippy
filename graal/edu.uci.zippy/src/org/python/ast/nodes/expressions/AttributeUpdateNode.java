package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;
import org.python.core.PyObject;

import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class AttributeUpdateNode extends BinaryOpNode {

    private final String name;

    public AttributeUpdateNode(TypedNode primary, String name, TypedNode value) {
        super(primary, value);
        this.name = name;
    }

    protected AttributeUpdateNode(AttributeUpdateNode node) {
        super(node);
        this.name = node.name;
    }

    @Override
    public void patchValue(TypedNode right) {
        rightNode = adoptChild(right);
    }

    @Override
    public void doLeftHandSide(VirtualFrame frame, Object value) {
        Object primary = this.leftNode.executeGeneric(frame);
        doGeneric(primary, value);
    }

    @Specialization
    public Object doGeneric(Object primary, Object value) {
        PyObject prim = (PyObject) primary;
        prim.__setattr__(name, (PyObject) value);
        return null;
    }

}
