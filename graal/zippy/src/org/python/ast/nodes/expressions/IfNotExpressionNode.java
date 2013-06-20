package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import org.python.ast.datatypes.PDictionary;
import org.python.ast.datatypes.PList;
import org.python.ast.datatypes.PTuple;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.codegen.ExecuteChildren;
import com.oracle.truffle.api.codegen.Generic;
import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@ExecuteChildren({ "test", "body", "orelse" })
public abstract class IfNotExpressionNode extends TypedNode {

    @Child
    protected TypedNode test;

    @Child
    protected TypedNode body;

    @Child
    protected TypedNode orelse;

    public IfNotExpressionNode(TypedNode test, TypedNode body, TypedNode orelse) {
        this.test = adoptChild(test);
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }
    
    protected IfNotExpressionNode(IfNotExpressionNode node) {
        this.test = adoptChild(node.test);
        this.body = adoptChild(node.body);
        this.orelse = adoptChild(node.orelse);
        copyNext(node);
    }
    
    @Specialization
    Object doInteger(int test, Object body, Object orelse) {
        boolean condition = test == 0;
        return runIfNotExp(condition, body, orelse);
    }

    @Specialization
    Object doBigInteger(BigInteger test, Object body, Object orelse) {
        boolean condition = test.compareTo(BigInteger.ZERO) == 0;
        return runIfNotExp(condition, body, orelse);
    }

    @Specialization
    Object doDouble(VirtualFrame frame, double test, Object body, Object orelse) {
        boolean condition = test == 0;
        return runIfNotExp(condition, body, orelse);
    }
    
    @Specialization
    Object doString(VirtualFrame frame, String test, Object body, Object orelse) {
        boolean condition = test.length() == 0;
        return runIfNotExp(condition, body, orelse);

    }
    
    @Specialization
    Object doPTuple(VirtualFrame frame, PTuple test, Object body, Object orelse) {
        boolean condition = test.len() == 0;
        return runIfNotExp(condition, body, orelse);
    }
    
    @Specialization
    Object doPList(VirtualFrame frame, PList test, Object body, Object orelse) {
        boolean condition = test.len() == 0;
        return runIfNotExp(condition, body, orelse);
    }

    @Specialization
    Object doPDictionary(PDictionary test, Object body, Object orelse) {
        boolean condition = test.len() == 0;
        return runIfNotExp(condition, body, orelse);
    }

    @Generic
    Object doGeneric(Object test, Object body, Object orelse) {
        boolean condition = test == null;
        return runIfNotExp(condition, body, orelse);
    }
    
    private Object runIfNotExp(boolean test, Object body, Object orelse) {
        if (test) {
            return body;
        } else {
            return orelse;
        }
    }
}