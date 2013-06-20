package org.python.ast.nodes;

import org.python.ast.datatypes.PCallable;
import org.python.core.Py;
import org.python.core.PyObject;

import com.oracle.truffle.api.codegen.Generic;
import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import static org.python.core.truffle.PythonTypesUtil.*;

public abstract class CallWithOneArgumentNoKeywordNode extends TypedNode {

    @Child
    protected TypedNode callee;

    private TypedNode argument;

    public CallWithOneArgumentNoKeywordNode(TypedNode callee, TypedNode argument) {
        this.callee = adoptChild(callee);
        this.argument = adoptChild(argument);
    }

    protected CallWithOneArgumentNoKeywordNode(CallWithOneArgumentNoKeywordNode node) {
        this(node.callee, node.argument);
        copyNext(node);
    }

    @Specialization
    public Object doPCallable(VirtualFrame frame, PCallable callee) {
        Object arg = argument.executeGeneric(frame);
        return callee.call(frame.pack(), arg);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object callee) {
        Object arg = argument.executeGeneric(frame);

        if (callee instanceof PyObject) {
            PyObject pyarg = adaptToPyObject(arg);
            PyObject pyCallable = (PyObject) callee;
            return unboxPyObject(pyCallable.__call__(new PyObject[] { pyarg }));
        } else {
            throw Py.SystemError("Unexpected callable type");
        }
    }

}
