package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import com.oracle.truffle.api.codegen.*;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;
import org.python.ast.nodes.literals.IntegerLiteralNodeFactory;
import org.python.ast.nodes.literals.NoneLiteralNode;

public abstract class SliceNode extends TernaryOpNode {

    public SliceNode(TypedNode lower, TypedNode upper, TypedNode step) {
        super(lower, upper, step);
        if (first == null || first instanceof NoneLiteralNode) {
            this.first = IntegerLiteralNodeFactory.create(Integer.MIN_VALUE);
        }
        if (second == null || second instanceof NoneLiteralNode) {
            this.second = IntegerLiteralNodeFactory.create(Integer.MIN_VALUE);  //with the assumption Integer.MIN_VALUE would rarely be used by programmers
        }
        if (third == null || third instanceof NoneLiteralNode) {
            this.third = IntegerLiteralNodeFactory.create(1);
        }
    }

    protected SliceNode(SliceNode node) {
        this(node.first, node.second, node.third);
    }

    @Specialization
    public PSlice doPSlice(int start, int stop, int step) {
        return new PSlice(start, stop, step);
    }

    @Specialization
    public PSlice doPSlice(BigInteger start, BigInteger stop, BigInteger step) {
        return new PSlice(start.intValue(), stop.intValue(), step.intValue());
    }
    
    @Generic
    public Object doGeneric(Object startObj, Object stopObj, Object stepObj) {
        int start = 0;
        if (startObj instanceof Integer) {
            start = (Integer) startObj;
        } else if (startObj instanceof BigInteger) {
            start = ((BigInteger) startObj).intValue();
        }
        
       int stop = 0;
        if (stopObj instanceof Integer) {
            stop = (Integer) stopObj;
        } else if (stopObj instanceof BigInteger) {
            stop = ((BigInteger) stopObj).intValue();
        }
        
        int step = 1;
        if (stepObj instanceof Integer) {
            step = (Integer) stepObj;
        } else if (stepObj instanceof BigInteger) {
            step = ((BigInteger) stepObj).intValue();
        }
        
        return new PSlice(start, stop, step);
    }

}
