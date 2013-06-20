package org.python.ast.nodes.statements;

import java.math.BigInteger;

import org.python.ast.*;
import org.python.ast.datatypes.PDictionary;
import org.python.ast.datatypes.PList;
import org.python.ast.datatypes.PTuple;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.Generic;
import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class IfNode extends StatementNode {

    @Child
    protected TypedNode conditionNode;

    protected BlockNode thenPartNode;

    protected BlockNode elsePartNode;

    public IfNode(TypedNode condition, BlockNode thenPart, BlockNode elsePart) {
        conditionNode = adoptChild(condition);
        thenPartNode = adoptChild(thenPart);
        elsePartNode = adoptChild(elsePart);
    }
    
    protected IfNode(IfNode node) {
        conditionNode = adoptChild(node.conditionNode);
        thenPartNode = adoptChild(node.thenPartNode);
        elsePartNode = adoptChild(node.elsePartNode);
    }

    @Specialization
    Object doInteger(VirtualFrame frame, int conditionNode) {
        boolean test = conditionNode != 0;
        return runIf(test, frame);
    }
    
    @Specialization
    Object doBoolean(VirtualFrame frame, boolean conditionNode) {
        return runIf(conditionNode, frame);
    }

    @Specialization
    Object doBigInteger(VirtualFrame frame, BigInteger conditionNode) {
        boolean test = conditionNode.compareTo(BigInteger.ZERO) != 0;
        return runIf(test, frame);
    }

    @Specialization
    Object doDouble(VirtualFrame frame, double conditionNode) {
        boolean test = conditionNode != 0;
        return runIf(test, frame);
    }
    
    @Specialization
    Object doString(VirtualFrame frame, String conditionNode) {
        boolean test = conditionNode.length() != 0;
        return runIf(test, frame);

    }
    
    @Specialization
    Object doPTuple(VirtualFrame frame, PTuple conditionNode) {
        boolean test = conditionNode.len() != 0;
        return runIf(test, frame);
    }
    
    @Specialization
    Object doPList(VirtualFrame frame, PList conditionNode) {
        boolean test = conditionNode.len() != 0;
        return runIf(test, frame);
    }

    @Specialization
    Object doPDictionary(VirtualFrame frame, PDictionary conditionNode) {
        boolean test = conditionNode.len() != 0;
        return runIf(test, frame);
    }

    @Generic
    Object doGeneric(VirtualFrame frame, Object conditionNode) {
        boolean test = conditionNode != null;
        return runIf(test, frame);
    }
 
    Object runIf(boolean test, VirtualFrame frame) {
        if (test) {
            thenPartNode.executeVoid(frame);
        } else {
            elsePartNode.executeVoid(frame);
        }
        
        return null;
    }
    
    @Override
    public void executeVoid(VirtualFrame frame) {
        executeGeneric(frame);
    }
    
    public abstract Object executeGeneric(VirtualFrame frame);
    
/*    @Override
    public void executeVoid(VirtualFrame frame) {
        if (!conditionNode.executeCondition(frame)) {
            thenPartNode.executeVoid(frame);
        } else {
            elsePartNode.executeVoid(frame);
        }
    }
*/
    @Override
    public String toString() {
        return super.toString() + "(" + conditionNode + ")";
    }

    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitIfNode(this);
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        conditionNode.visualize(level);
        thenPartNode.visualize(level);
        elsePartNode.visualize(level);
    }

}
