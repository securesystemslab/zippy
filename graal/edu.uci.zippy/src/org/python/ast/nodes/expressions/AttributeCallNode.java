package org.python.ast.nodes.expressions;

import org.python.modules.truffle.Module;
import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;
import org.python.core.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

import static org.python.core.truffle.PythonTypesUtil.*;

@ExecuteChildren({ "primary" })
public abstract class AttributeCallNode extends TypedNode {

    @Child
    protected TypedNode primary;

    @Children
    private TypedNode[] arguments;

    private final String name;

    public AttributeCallNode(TypedNode primary, TypedNode[] arguments, String name) {
        this.primary = adoptChild(primary);
        this.arguments = adoptChildren(arguments);
        this.name = name;
    }

    protected AttributeCallNode(AttributeCallNode node) {
        this(node.primary, node.arguments, node.name);
        copyNext(node);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callee=" + primary + "," + name + ")";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        for (TypedNode e : arguments) {
            e.visualize(level);
        }
    }

    @Specialization
    public Object doPList(VirtualFrame frame, PList prim) {
        Object[] args = doArguments(frame);
        return prim.findAttribute(name).call(null, args);
    }

    @Specialization
    public Object doPDictionary(VirtualFrame frame, PDictionary prim) {
        Object[] args = doArguments(frame);
        return prim.findAttribute(name).call(null, args);
    }

    @Specialization
    public Object doString(VirtualFrame frame, String prim) {
        Object[] args = doArguments(frame);
        PString primString = new PString(prim);
        return primString.findAttribute(name).call(null, args);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object prim) {
        Object[] args = doArguments(frame);

        PyObject primary;
        if (prim instanceof PyObject) {
            primary = (PyObject) prim;
        } else if (prim instanceof Module) {
            return ((Module) prim).lookupMethod(name).call(null, args, null);
        } else {
            primary = adaptToPyObject(prim);
        }

        PyObject callable = primary.__findattr__(name);

        // need to box Object to PyObject
        PyObject[] pyargs = new PyObject[args.length];
        int i = 0;
        for (Object arg : args) {
            pyargs[i] = adaptToPyObject(arg);
            i++;
        }

        return unboxPyObject(((PyObject) callable).__call__(pyargs));
    }

    private Object[] doArguments(VirtualFrame frame) {
        Object[] evaluated = new Object[arguments.length];
        int index = 0;

        for (int i = 0; i < arguments.length; i++) {
            Object arg = arguments[i].executeGeneric(frame);

            if (arg instanceof PyObject) {
                arg = unboxPyObject((PyObject) arg);
            }

            evaluated[index] = arg;
            index++;
        }

        return evaluated;
    }

}
