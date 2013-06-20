package org.python.ast.nodes.expressions;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * This is also a LeftHandSideNode
 * 
 * @author zwei
 * 
 */
public abstract class SubscriptStoreNode extends TernaryOpNode {

    public SubscriptStoreNode(TypedNode primary, TypedNode slice, TypedNode value) {
        super(primary, slice, value);
    }

    protected SubscriptStoreNode(SubscriptStoreNode node) {
        super(node);
    }

    @Override
    public void patchValue(TypedNode right) {
        this.third = adoptChild(right);
    }

    /*
     * As a LeftHandSideNode
     */
    @Override
    public void doLeftHandSide(VirtualFrame frame, Object value) {
        Object primary = this.first.executeGeneric(frame);
        Object slice = this.second.executeGeneric(frame);
        doGeneric(primary, slice, value);
    }

    /*
     * As a right hand side expression
     */
    @Specialization(order = 0)
    public Object doPDictionary(PDictionary primary, Object slice, Object value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 1)
    public Object doPSequence(PSequence primary, int slice, Object value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 2)
    public Object doPSequence(PSequence primary, PSlice slice, PSequence value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Specialization(order = 3)
    public Object doPIntegerArray(PIntegerArray primary, int slice, int value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 4)
    public Object doPIntegerArray(PIntegerArray primary, PSlice slice, PIntegerArray value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Specialization(order = 5)
    public Object doPDoubleArray(PDoubleArray primary, int slice, double value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 6)
    public Object doPDoubleArray(PDoubleArray primary, PSlice slice, PDoubleArray value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Specialization(order = 7)
    public Object doPCharArray(PCharArray primary, int slice, char value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 8)
    public Object doPCharArray(PCharArray primary, PSlice slice, PCharArray value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Generic
    public Object doGeneric(Object primary, Object slice, Object value) {
        if (primary instanceof PSequence) {
            PSequence prim = (PSequence) primary;
            if (slice instanceof Integer) {
                prim.setItem((int) slice, value);
            } else if (slice instanceof PSlice) {
                prim.setSlice((PSlice) slice, (PSequence) value);
            }
        } else if (primary instanceof PDictionary) {
            PDictionary prim = (PDictionary) primary;
            prim.setItem(slice, value);
        } else if (primary instanceof PArray) {
            PArray prim = (PArray) primary;
            prim.setItem((int) slice, value);
        } else {
            throw new RuntimeException("Unsupported Type!");
        }

        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " = " + first + "[" + second + "]";
    }

}
