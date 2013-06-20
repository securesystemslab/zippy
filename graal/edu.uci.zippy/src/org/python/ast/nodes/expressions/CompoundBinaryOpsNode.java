package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.ConditionNode;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

@ExecuteChildren({ "operations" })
public abstract class CompoundBinaryOpsNode extends TypedNode {

    @Children
    protected ConditionNode[] operations;

    public CompoundBinaryOpsNode(ConditionNode[] operations) {
        this.operations = adoptChildren(operations);
    }

    protected CompoundBinaryOpsNode(CompoundBinaryOpsNode node) {
        this(node.operations);
        copyNext(node);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;

        for (ConditionNode node : operations) {
            node.visualize(level);
        }
    }

    /**
     * The problem with not able to use annotation to generate this class is
     * because the generated code directly calls executeCondition() on
     * operations array.
     * 
     * @author zwei
     * 
     */
    public static class CompoundComparisonsNode extends CompoundBinaryOpsNode {

        public CompoundComparisonsNode(ConditionNode[] operations) {
            super(operations);
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            boolean result = true;

            for (int i = 0; i < operations.length; i++) {
                result = result && operations[i].executeCondition(frame);
            }

            return result;
        }

        @Override
        public int executeInteger(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public BigInteger executeBigInteger(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PComplex executeComplex(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public Object executeGeneric(VirtualFrame frame) {
            try {
                return executeBoolean(frame);
            } catch (UnexpectedResultException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public String executeString(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PDictionary executePDictionary(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PSequence executePSequence(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PSlice executePSlice(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PList executePList(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PTuple executePTuple(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PBaseSet executePBaseSet(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PSet executePSet(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PFrozenSet executePFrozenSet(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PIntegerArray executePIntegerArray(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PDoubleArray executePDoubleArray(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PCharArray executePCharArray(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public char executeCharacter(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PCallable executePCallable(VirtualFrame frame) throws UnexpectedResultException {
            throw new RuntimeException("unexpected");
        }

        @Override
        public PObject executePObject(VirtualFrame frame) throws UnexpectedResultException {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
