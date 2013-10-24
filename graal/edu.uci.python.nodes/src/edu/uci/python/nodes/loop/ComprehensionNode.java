/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.nodes.loop;

import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.expressions.*;
import edu.uci.python.nodes.statements.*;
import edu.uci.python.nodes.utils.*;
import edu.uci.python.runtime.datatypes.*;

@NodeChild(value = "iterator", type = PNode.class)
public abstract class ComprehensionNode extends StatementNode {

    public abstract PNode getIterator();

    @Child protected BooleanCastNode condition;

    @Child protected PNode target;

    public ComprehensionNode(PNode target, BooleanCastNode condition) {
        this.target = adoptChild(target);
        this.condition = adoptChild(condition);
    }

    protected ComprehensionNode(ComprehensionNode node) {
        this(node.target, node.condition);
    }

    @Specialization
    public Object doPSequence(VirtualFrame frame, PSequence sequence) {
        List<Object> results = new ArrayList<>();
        Iterator<?> iter = sequence.iterator();
        RuntimeValueNode rvn = (RuntimeValueNode) ((WriteNode) target).getRhs();

        while (iter.hasNext()) {
            Object value = iter.next();
            rvn.setValue(value);
            evaluateNextItem(frame, value, results);
        }

        throw new ExplicitReturnException(new PList(results));
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame, Object sequence) {
        PList seq;
        if (sequence instanceof CallTarget) {
            CallTarget ct = (CallTarget) sequence;
            Object ret = ct.call(frame.pack());
            seq = (PList) ret;
        } else {
            throw new RuntimeException("Unhandled sequence");
        }

        List<Object> results = new ArrayList<>();
        Iterator<?> iter = seq.iterator();
        RuntimeValueNode rvn = (RuntimeValueNode) ((WriteNode) target).getRhs();

        while (iter.hasNext()) {
            Object value = iter.next();
            rvn.setValue(value);
            evaluateNextItem(frame, value, results);
        }

        throw new ExplicitReturnException(new PList(results));
    }

    protected boolean evaluateCondition(VirtualFrame frame) {
        return condition != null && !condition.executeBoolean(frame);
    }

    @SuppressWarnings("unused")
    protected void evaluateNextItem(VirtualFrame frame, Object value, List<Object> results) {
        throw new RuntimeException("This is not a concrete comprehension node!");
    }

    public abstract static class InnerComprehensionNode extends ComprehensionNode {

        @Child protected PNode loopBody;

        public InnerComprehensionNode(PNode target, BooleanCastNode condition, PNode loopBody) {
            super(target, condition);
            this.loopBody = adoptChild(loopBody);
        }

        protected InnerComprehensionNode(InnerComprehensionNode node) {
            this(node.target, node.condition, node.loopBody);
        }

        @Override
        protected void evaluateNextItem(VirtualFrame frame, Object value, List<Object> results) {
            target.execute(frame);

            if (this.evaluateCondition(frame)) {
                return;
            }

            if (loopBody != null) {
                results.add(loopBody.execute(frame));
            }
        }
    }

    public abstract static class OuterComprehensionNode extends ComprehensionNode {

        @Child protected PNode innerLoop;

        public OuterComprehensionNode(PNode target, BooleanCastNode condition, PNode innerLoop) {
            super(target, condition);
            this.innerLoop = adoptChild(innerLoop);
        }

        protected OuterComprehensionNode(OuterComprehensionNode node) {
            this(node.target, node.condition, node.innerLoop);
        }

        @Override
        protected void evaluateNextItem(VirtualFrame frame, Object value, List<Object> results) {
            target.execute(frame);

            if (this.evaluateCondition(frame)) {
                return;
            }

            if (innerLoop == null) {
                return;
            }

            try {
                innerLoop.execute(frame);
            } catch (ExplicitReturnException ere) {
                PList list = (PList) ere.getValue();
                results.addAll(Arrays.asList(list.getSequence()));
            }
        }

    }

}
