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
package edu.uci.python.nodes.generator;

import java.util.*;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.expressions.*;
import edu.uci.python.nodes.statements.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.exception.*;

@NodeChild(value = "iteratorNode", type = PNode.class)
public abstract class ComprehensionNode extends StatementNode {

    public abstract PNode getIteratorNode();

    @Child protected BooleanCastNode condition;

    @Child protected PNode target;

    protected Iterator<?> iterator;

    public ComprehensionNode(PNode target, BooleanCastNode condition) {
        this.target = adoptChild(target);
        this.condition = adoptChild(condition);
    }

    protected ComprehensionNode(ComprehensionNode node) {
        this(node.target, node.condition);
    }

    protected boolean evaluateCondition(VirtualFrame frame) {
        return condition == null || condition.executeBoolean(frame);
    }

    @SuppressWarnings("unused")
    protected void generateNextValue(VirtualFrame frame, Object value) {
        throw new UnsupportedOperationException();
    }

    public abstract static class InnerComprehensionNode extends ComprehensionNode {

        @Child protected PNode loopBody;

        public InnerComprehensionNode(PNode target, BooleanCastNode condition, PNode loopBody) {
            super(target, condition);
            assert loopBody != null;
            this.loopBody = adoptChild(loopBody);
        }

        protected InnerComprehensionNode(InnerComprehensionNode node) {
            this(node.target, node.condition, node.loopBody);
        }

        @Specialization
        public Object doPSequence(VirtualFrame frame, PSequence sequence) {
            if (iterator == null) {
                iterator = sequence.iterator();
            }

            while (iterator.hasNext()) {
                Object value = iterator.next();
                generateNextValue(frame, value);
            }

            iterator = null;
            throw new StopIterationException();
        }

        @Override
        protected void generateNextValue(VirtualFrame frame, Object value) {
            ((WriteNode) target).executeWrite(frame, value);

            if (!evaluateCondition(frame)) {
                return;
            }

            Object result = loopBody.execute(frame);
            throw new ExplicitYieldException(result);
        }
    }

    public abstract static class OuterComprehensionNode extends ComprehensionNode {

        @Child protected PNode innerLoop;
        protected Object currentValue;

        public OuterComprehensionNode(PNode target, BooleanCastNode condition, PNode innerLoop) {
            super(target, condition);
            assert condition == null;
            assert innerLoop != null;
            this.innerLoop = adoptChild(innerLoop);
        }

        protected OuterComprehensionNode(OuterComprehensionNode node) {
            this(node.target, node.condition, node.innerLoop);
        }

        @Specialization
        public Object doPSequence(VirtualFrame frame, PSequence sequence) {
            if (iterator == null) {
                iterator = sequence.iterator();
            }

            do {
                currentValue = currentValue == null ? iterator.next() : currentValue;

                try {
                    generateNextValue(frame, currentValue);
                } catch (StopIterationException sie) {
                    // return to the loop header
                    currentValue = null;
                }
            } while (iterator.hasNext());

            iterator = null;
            throw new StopIterationException();
        }

        @Specialization
        public Object doGeneric(VirtualFrame frame, Object sequence) {
            Iterator<?> iter;

            if (sequence instanceof PGenerator) {
                PGenerator generator = (PGenerator) sequence;
                iter = generator.evaluateToJavaIteratore(frame);
            } else {
                throw new RuntimeException("Unhandled sequence");
            }

            while (iter.hasNext()) {
                Object value = iter.next();
                generateNextValue(frame, value);
            }

            throw new StopIterationException();
        }

        @Override
        protected void generateNextValue(VirtualFrame frame, Object value) {
            ((WriteNode) target).executeWrite(frame, value);

            if (!evaluateCondition(frame)) {
                return;
            }

            innerLoop.execute(frame);
        }
    }
}
