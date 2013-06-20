package org.python.ast.nodes;

import org.python.ast.datatypes.*;
import org.python.core.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

import static org.python.core.truffle.PythonTypesUtil.*;

public abstract class CallNode extends TypedNode {

    @Child
    protected TypedNode callee;

    private TypedNode[] arguments;

    private TypedNode[] keywords;

    public CallNode(TypedNode callee, TypedNode[] arguments, TypedNode[] keywords) {
        this.callee = adoptChild(callee);
        this.arguments = adoptChildren(arguments);
        this.keywords = adoptChildren(keywords);
    }

    protected CallNode(CallNode node) {
        this(node.callee, node.arguments, node.keywords);
        copyNext(node);
    }
    
    public TypedNode getCallee() {
        return callee;
    }
    
    public TypedNode[] getArguments() {
        return arguments;
    }

    @Specialization
    public Object doPCallable(VirtualFrame frame, PCallable callee) {
        Object[] args = executeArguments(frame, arguments);
        Object[] kwords = executeArguments(frame, keywords);
        return callee.call(frame.pack(), args, kwords);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object callee) {
        Object[] args = executeArguments(frame, arguments);

        if (callee instanceof PyObject) {
            PyObject[] pyargs = adaptToPyObjects(args);
            PyObject pyCallable = (PyObject) callee;
            return unboxPyObject(pyCallable.__call__(pyargs));
        } else {
            throw Py.SystemError("Unexpected callable type");
        }
    }

    private Object[] executeArguments(VirtualFrame frame, TypedNode[] arguments) {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callee=" + callee + ")";
    }
    
    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);
        
        level++;
        for (TypedNode a : arguments) {
            a.visualize(level);
        }
    }

}
