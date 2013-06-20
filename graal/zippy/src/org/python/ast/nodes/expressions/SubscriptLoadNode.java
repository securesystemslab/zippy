package org.python.ast.nodes.expressions;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;

public abstract class SubscriptLoadNode extends BinaryOpNode {

    public SubscriptLoadNode(TypedNode primary, TypedNode slice) {
        super(primary, slice);
    }

    protected SubscriptLoadNode(SubscriptLoadNode node) {
        super(node);
    }

    public TypedNode getPrimary() {
        return this.leftNode;
    }

    public TypedNode getSlice() {
        return this.rightNode;
    }

    @Specialization(order = 0)
    public String doString(String primary, PSlice slice) {
        int length = slice.computeActualIndices(primary.length());
        int start = slice.getStart();
        int stop = slice.getStop();
        int step = slice.getStep();

        if (step > 0 && stop < start) {
            stop = start;
        }
        if (step == 1) {
            return primary.substring(start, stop);
        } else {
            char new_chars[] = new char[length];
            int j = 0;
            for (int i = start; j < length; i += step) {
                new_chars[j++] = primary.charAt(i);
            }

            return new String(new_chars);
        }
    }

    @Specialization(order = 1)
    public String doString(String primary, int slice) {
        if (slice < 0) {
            slice += primary.length();
        }
        return String.valueOf(primary.charAt(slice));
    }

    @Specialization(order = 2)
    public Object doPDictionary(PDictionary primary, Object slice) {
        return primary.getItem(slice);
    }

    @Specialization(order = 3)
    public Object doPSequence(PSequence primary, int slice) {
        return primary.getItem(slice);
    }

    @Specialization(order = 4)
    public Object doPSequence(PSequence primary, PSlice slice) {
        return primary.getSlice(slice);
    }

    @Specialization(order = 5)
    public Object doPIntegerArray(PIntegerArray primary, int slice) {
        return primary.getItem(slice);
    }

    @Specialization(order = 6)
    public Object doPIntegerArray(PIntegerArray primary, PSlice slice) {
        return primary.getSlice(slice);
    }

    @Specialization(order = 7)
    public Object doPDoubleArray(PDoubleArray primary, int slice) {
        return primary.getItem(slice);
    }

    @Specialization(order = 8)
    public Object doPDoubleArray(PDoubleArray primary, PSlice slice) {
        return primary.getSlice(slice);
    }

    @Specialization(order = 9)
    public Object doPCharArray(PCharArray primary, int slice) {
        return primary.getItem(slice);
    }

    @Specialization(order = 10)
    public Object doPCharArray(PCharArray primary, PSlice slice) {
        return primary.getSlice(slice);
    }

    @Generic
    public Object doGeneric(Object primary, Object slice) {
        throw new RuntimeException("Unsupported Type!");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " = " + leftNode + "[" + rightNode + "]";
    }

}
