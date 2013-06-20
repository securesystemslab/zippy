package org.python.ast.nodes;

import org.python.ast.datatypes.PCallable;

import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class CallBuiltInWithTwoArgsNoKeywordNode extends TypedNode {

    protected final PCallable callee;
    
    protected final String name;

    private TypedNode arg0;

    private TypedNode arg1;

    public CallBuiltInWithTwoArgsNoKeywordNode(PCallable callee, String name, TypedNode arg0, TypedNode arg1) {
        this.callee = callee;
        this.name = name;
        this.arg0 = adoptChild(arg0);
        this.arg1 = adoptChild(arg1);
    }

    protected CallBuiltInWithTwoArgsNoKeywordNode(CallBuiltInWithTwoArgsNoKeywordNode node) {
        this(node.callee, node.name, node.arg0, node.arg1);
        copyNext(node);
    }
    
    public TypedNode getArgument0() {
        return arg0;
    }
    
    public TypedNode getArgument1() {
        return arg1;
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        Object a0 = arg0.executeGeneric(frame);
        Object a1 = arg1.executeGeneric(frame);
        return callee.call(frame.pack(), a0, a1);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callee=" + name + ")";
    }

    public Object getName() {
        return name;
    }
}
