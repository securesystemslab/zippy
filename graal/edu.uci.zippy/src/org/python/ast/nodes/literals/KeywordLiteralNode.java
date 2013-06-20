package org.python.ast.nodes.literals;

import org.python.ast.datatypes.PKeyword;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class KeywordLiteralNode extends TypedNode {
    
    private final TypedNode value;
    
    private final String name;
    
    
    public KeywordLiteralNode(TypedNode value, String name) {
        this.name = name;
        this.value = value;
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        return new PKeyword(value.executeGeneric(frame), name);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "( value: " + value + ", name: " + name + ")";
    }
}
