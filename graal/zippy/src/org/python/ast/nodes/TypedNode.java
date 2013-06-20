package org.python.ast.nodes;

import java.math.*;

import org.python.antlr.ast.VisitorIF;
import org.python.ast.datatypes.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

public abstract class TypedNode extends ConditionNode {

    protected void copyNext(TypedNode node) {
        setNext(node.next());
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        executeGeneric(frame);
    }

    @Override
    public void doLeftHandSide(VirtualFrame frame, Object value) {
        throw new RuntimeException("Not a real LeftHandSideNode!");
    }

    @Override
    public void patchValue(TypedNode value) {
        throw new RuntimeException("Not a real LeftHandSideNode!");
    }

    @Override
    public final boolean executeCondition(VirtualFrame frame) {
        try {
            return executeBoolean(frame);
        } catch (UnexpectedResultException ex) {
            throw new RuntimeException("Illegal type for condition: " + ex.getResult().getClass().getSimpleName());
        }
    }

    public abstract boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException;

    public abstract int executeInteger(VirtualFrame frame) throws UnexpectedResultException;

    public abstract BigInteger executeBigInteger(VirtualFrame frame) throws UnexpectedResultException;

    public abstract double executeDouble(VirtualFrame frame) throws UnexpectedResultException;

    public abstract char executeCharacter(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PComplex executeComplex(VirtualFrame frame) throws UnexpectedResultException;

    public abstract String executeString(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PDictionary executePDictionary(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PSequence executePSequence(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PList executePList(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PTuple executePTuple(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PIntegerArray executePIntegerArray(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PDoubleArray executePDoubleArray(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PCharArray executePCharArray(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PBaseSet executePBaseSet(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PSet executePSet(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PFrozenSet executePFrozenSet(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PSlice executePSlice(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PCallable executePCallable(VirtualFrame frame) throws UnexpectedResultException;

    public abstract PObject executePObject(VirtualFrame frame) throws UnexpectedResultException;

    public abstract Object executeGeneric(VirtualFrame frame);

    // Simple return it self if visited. Usually this is for LiteralNodes
    @SuppressWarnings("unchecked")
    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return (R) this;
    }

    public static TypedNode DUMMY = TypedNodeFactory.DummyTypedNodeFactory.create();

    static abstract class DummyTypedNode extends TypedNode {

        public DummyTypedNode() {}

        @Specialization
        Object doGeneric() {
            return null;
        }
    }

}
