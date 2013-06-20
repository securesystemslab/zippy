package org.python.ast.nodes.expressions;

import org.python.ast.datatypes.PObject;
import org.python.ast.datatypes.PString;
import org.python.ast.nodes.TypedNode;
import org.python.core.*;
import org.python.modules.truffle.Module;

import com.oracle.truffle.api.codegen.*;
import static org.python.core.truffle.PythonTypesUtil.*;

public abstract class AttributeRefNode extends UnaryOpNode {

    private final String name;

    public AttributeRefNode(TypedNode operand, String name) {
        super(operand);
        this.name = name;
    }

    protected AttributeRefNode(AttributeRefNode node) {
        super(node);
        this.name = node.name;
    }

    public String getName() {
        return name;
    }

    @Specialization
    public Object doPObject(PObject operand) {
        return operand.findAttribute(name);
    }

    @Specialization
    public Object doString(String operand) {
        PString primString = new PString(operand);
        return primString.findAttribute(name);
    }

    @Specialization
    public Object doGeneric(Object operand) {
        PyObject primary;
        if (operand instanceof Module) {
            return ((Module) operand).lookupMethod(name);
        } else if (operand instanceof PyObject) {
            primary = (PyObject) operand;
        } else {
            primary = adaptToPyObject(operand);
        }
        // need to change PyList to TruffleList
        return unboxPyObject(primary.__findattr__(name));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " ( " + operand + ", " + name + ")";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        operand.visualize(level);
    }

}
