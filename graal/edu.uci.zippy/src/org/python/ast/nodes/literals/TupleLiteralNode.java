package org.python.ast.nodes.literals;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

import static org.python.core.truffle.PythonTypesUtil.*;

@ExecuteChildren({ "values" })
public abstract class TupleLiteralNode extends TypedNode {

    @Children
    protected TypedNode[] values;

    public TupleLiteralNode(TypedNode[] values) {
        this.values = adoptChildren(values);
    }

    protected TupleLiteralNode(TupleLiteralNode node) {
        this(node.values);
    }

    @Specialization
    public PTuple doTruffleTuple(VirtualFrame frame) {
        return (PTuple) doGeneric(frame); // TODO
    }

    @Generic
    public Object doGeneric(VirtualFrame frame) {
        Object[] elements = new Object[values.length];

        int i = 0;
        for (TypedNode v : this.values) {
            elements[i++] = v.executeGeneric(frame);
        }

        return createTuple(elements);
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
        for (TypedNode v : values) {
            v.visualize(level);
        }
    }

}
