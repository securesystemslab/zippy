package org.python.ast.nodes.expressions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.python.ast.datatypes.PList;
import org.python.ast.datatypes.PSequence;
import org.python.ast.nodes.ConditionNode;
import org.python.ast.nodes.LeftHandSideNode;
import org.python.ast.nodes.TypedNode;
import org.python.ast.utils.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.VirtualFrame;

@ExecuteChildren({ "iterator" })
public abstract class ComprehensionNode extends TypedNode {

    @Child
    protected TypedNode iterator;

    protected ConditionNode condition;

    protected LeftHandSideNode target;

    public ComprehensionNode(LeftHandSideNode target, TypedNode iterator, ConditionNode condition) {
        this.target = adoptChild(target);
        this.iterator = adoptChild(iterator);
        this.condition = adoptChild(condition);
    }

    protected ComprehensionNode(ComprehensionNode node) {
        this(node.target, node.iterator, node.condition);
        copyNext(node);
    }

    @Specialization
    public Object doPSequence(VirtualFrame frame, PSequence sequence) {
        List<Object> results = new ArrayList<Object>();
        Iterator<?> iter = sequence.iterator();

        while (iter.hasNext()) {
            Object value = iter.next();
            evaluateNextItem(frame, value, results);
        }

        throw new ExplicitReturnException(new PList(results));
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object sequence) {
        PList seq;
        if (sequence instanceof CallTarget) {
            CallTarget ct = (CallTarget) sequence;
            Object ret = ct.call(frame.pack());
            seq = (PList) ret;
        } else {
            throw new RuntimeException("Unhandled sequence");
        }

        List<Object> results = new ArrayList<Object>();
        Iterator<?> iter = seq.iterator();

        while (iter.hasNext()) {
            Object value = iter.next();
            evaluateNextItem(frame, value, results);
        }

        throw new ExplicitReturnException(new PList(results));
    }

    protected boolean evaluateCondition(VirtualFrame frame) {
        return condition != null && !condition.executeCondition(frame);
    }

    protected void evaluateNextItem(VirtualFrame frame, Object value, List<Object> results) {
        throw new RuntimeException("This is not a concrete comprehension node!");
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        iterator.visualize(level);
        target.visualize(level);
        if (condition != null) {
            condition.visualize(level);
        }
    }

    public abstract static class InnerComprehensionNode extends ComprehensionNode {

        @Child
        protected TypedNode loopBody;

        public InnerComprehensionNode(LeftHandSideNode target, TypedNode iterator, ConditionNode condition, TypedNode loopBody) {
            super(target, iterator, condition);
            this.loopBody = adoptChild(loopBody);
        }

        protected InnerComprehensionNode(InnerComprehensionNode node) {
            this(node.target, node.iterator, node.condition, node.loopBody);
        }

        @Override
        protected void evaluateNextItem(VirtualFrame frame, Object value, List<Object> results) {
            target.doLeftHandSide(frame, value);

            if (this.evaluateCondition(frame)) {
                return;
            }

            if (loopBody != null) {
                results.add(loopBody.executeGeneric(frame));
            }
        }

        @Override
        public void visualize(int level) {
            super.visualize(level);
            if (loopBody != null) {
                loopBody.visualize(level++);
            }
        }

    }

    public abstract static class OuterComprehensionNode extends ComprehensionNode {

        @Child
        protected TypedNode innerLoop;

        public OuterComprehensionNode(LeftHandSideNode target, TypedNode iterator, ConditionNode condition, TypedNode innerLoop) {
            super(target, iterator, condition);
            this.innerLoop = adoptChild(innerLoop);
        }

        protected OuterComprehensionNode(OuterComprehensionNode node) {
            this(node.target, node.iterator, node.condition, node.innerLoop);
        }

        @Override
        protected void evaluateNextItem(VirtualFrame frame, Object value, List<Object> results) {
            target.doLeftHandSide(frame, value);

            if (this.evaluateCondition(frame)) {
                return;
            }

            if (innerLoop == null) {
                return;
            }

            try {
                innerLoop.executeGeneric(frame);
            } catch (ExplicitReturnException ere) {
                PList list = (PList) ere.getValue();
                results.addAll(list.getList());
            }
        }

        @Override
        public void visualize(int level) {
            super.visualize(level);
            if (innerLoop != null) {
                innerLoop.visualize(level++);
            }
        }

    }

}
