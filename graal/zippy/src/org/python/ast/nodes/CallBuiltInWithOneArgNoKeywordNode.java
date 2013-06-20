package org.python.ast.nodes;

import org.python.ast.datatypes.PCallable;

import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class CallBuiltInWithOneArgNoKeywordNode extends TypedNode {

    protected final PCallable callee;
    
    protected final String name;

    private TypedNode argument;

    public CallBuiltInWithOneArgNoKeywordNode(PCallable callee, String name, TypedNode argument) {
        this.callee = callee;
        this.name = name;
        this.argument = adoptChild(argument);
    }

    protected CallBuiltInWithOneArgNoKeywordNode(CallBuiltInWithOneArgNoKeywordNode node) {
        this(node.callee, node.name, node.argument);
        copyNext(node);
    }
    
    public TypedNode getArgument() {
        return argument;
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        Object arg = argument.executeGeneric(frame);
        return callee.call(frame.pack(), arg);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callee=" + name + ")";
    }

    public Object getName() {
        return name;
    }
}
