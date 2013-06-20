package org.python.ast.nodes.literals;

import java.util.*;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;
import org.python.core.truffle.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

@ExecuteChildren({ "values" })
public abstract class ListLiteralNode extends TypedNode {

    @Children
    protected TypedNode[] values;

    public ListLiteralNode(TypedNode[] values) {
        this.values = adoptChildren(values);
    }

    protected ListLiteralNode(ListLiteralNode node) {
        this(node.values);
    }

    @Specialization
    public PList doTruffleList(VirtualFrame frame) {
        return (PList) doGeneric(frame); // TODO
    }

    @Generic
    public Object doGeneric(VirtualFrame frame) {
        List<Object> elements = new ArrayList<Object>();

        for (TypedNode v : this.values) {
            elements.add(v.executeGeneric(frame));
        }

        return PythonTypesUtil.createList(elements);
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
