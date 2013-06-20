package org.python.ast.nodes;

import org.python.ast.datatypes.*;
import org.python.core.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

import static org.python.core.truffle.PythonTypesUtil.*;

public abstract class CallBuiltInNode extends TypedNode {

    protected final PCallable callee;
    
    protected final String name;

    private TypedNode[] arguments;

    private TypedNode[] keywords;

    public CallBuiltInNode(PCallable callee, String name, TypedNode[] arguments, TypedNode[] keywords) {
        this.callee = callee;
        this.name = name;
        this.arguments = adoptChildren(arguments);
        this.keywords = adoptChildren(keywords);
    }
    
    protected CallBuiltInNode(CallBuiltInNode node) {
        this(node.callee, node.name, node.arguments, node.keywords);
        copyNext(node);
    }
    
    public String getName() {
        return name;
    }
    
    public TypedNode[] getArguments() {
        return arguments;
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        Object[] args = executeArguments(frame, arguments);
        Object[] kwords = executeArguments(frame, keywords);
        
        return callee.call(frame.pack(), args, kwords);
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
        return getClass().getSimpleName() + "(callee=" + name + ")";
    }
}
