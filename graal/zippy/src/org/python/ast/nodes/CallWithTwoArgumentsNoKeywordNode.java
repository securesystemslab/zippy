package org.python.ast.nodes;

import static org.python.core.truffle.PythonTypesUtil.*;

import org.python.ast.datatypes.PCallable;
import org.python.core.Py;
import org.python.core.PyObject;

import com.oracle.truffle.api.codegen.Generic;
import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class CallWithTwoArgumentsNoKeywordNode extends TypedNode {

    @Child
    protected TypedNode callee;

    private TypedNode arg0;

    private TypedNode arg1;

    public CallWithTwoArgumentsNoKeywordNode(TypedNode callee, TypedNode arg0, TypedNode arg1) {
        this.callee = adoptChild(callee);
        this.arg0 = adoptChild(arg0);
        this.arg1 = adoptChild(arg1);
    }

    protected CallWithTwoArgumentsNoKeywordNode(CallWithTwoArgumentsNoKeywordNode node) {
        this(node.callee, node.arg0, node.arg1);
        copyNext(node);
    }

    @Specialization
    public Object doPCallable(VirtualFrame frame, PCallable callee) {
        Object a0 = arg0.executeGeneric(frame);
        Object a1 = arg1.executeGeneric(frame);
        return callee.call(frame.pack(), a0, a1);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object callee) {
        Object a0 = arg0.executeGeneric(frame);
        Object a1 = arg1.executeGeneric(frame);

        if (callee instanceof PyObject) {
            PyObject[] pyargs = adaptToPyObjects(new Object[] { a0, a1 });
            PyObject pyCallable = (PyObject) callee;
            return unboxPyObject(pyCallable.__call__(pyargs));
        } else {
            throw Py.SystemError("Unexpected callable type");
        }
    }

}
