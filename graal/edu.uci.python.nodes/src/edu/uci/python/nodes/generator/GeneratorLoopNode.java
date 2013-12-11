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
import edu.uci.python.nodes.loop.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.*;

@NodeChild(value = "iteratorNode", type = PNode.class)
public abstract class GeneratorLoopNode extends LoopNode {

    @Child protected CastToBooleanNode condition;
    @Child protected PNode target;
    // protected Iterator<?> iterator;

    protected PIterator iterator;

    public abstract PNode getIteratorNode();

    public GeneratorLoopNode(PNode target, CastToBooleanNode condition, PNode loopBody) {
        super(loopBody);
        this.target = adoptChild(target);
        this.condition = adoptChild(condition);
    }

    protected GeneratorLoopNode(GeneratorLoopNode node) {
        this(node.target, node.condition, node.body);
    }

    protected boolean evaluateCondition(VirtualFrame frame) {
        return condition == null || condition.executeBoolean(frame);
    }

    public abstract static class InnerGeneratorLoopNode extends GeneratorLoopNode {

        public InnerGeneratorLoopNode(PNode target, CastToBooleanNode condition, PNode loopBody) {
            super(target, condition, loopBody);
            assert loopBody != null;
        }

        protected InnerGeneratorLoopNode(InnerGeneratorLoopNode node) {
            this(node.target, node.condition, node.body);
        }

        @Specialization
        public Object doPSequence(VirtualFrame frame, PSequence sequence) {
            if (iterator == null) {
                // iterator = sequence.iterator();
                iterator = sequence.__iter__();
                // iterator = sequence.__iter__().iterator();
            }

// while (iterator.hasNext()) {
// generateNextValue(frame);
// }

            try {
                while (true) {
                    generateNextValue(frame);
                }
            } catch (StopIterationException e) {
                // fall through
            }

            iterator = null;
            throw StopIterationException.INSTANCE;
        }

        protected final void generateNextValue(VirtualFrame frame) {
            // Object value = iterator.next();
            Object value = iterator.__next__();
            ((WriteNode) target).executeWrite(frame, value);

            if (!evaluateCondition(frame)) {
                return;
            }

            Object result = body.execute(frame);
            throw new ExplicitYieldException(result);
        }
    }

    public abstract static class OuterGeneratorLoopNode extends GeneratorLoopNode {

        protected Object currentValue;

        public OuterGeneratorLoopNode(PNode target, CastToBooleanNode condition, PNode innerLoop) {
            super(target, condition, innerLoop);
            assert innerLoop != null;
        }

        protected OuterGeneratorLoopNode(OuterGeneratorLoopNode node) {
            this(node.target, node.condition, node.body);
        }

        @Specialization
        public Object doPSequence(VirtualFrame frame, PSequence sequence) {
            if (iterator == null) {
                // iterator = sequence.iterator();
                iterator = sequence.__iter__();
                // iterator = sequence.__iter__().iterator();

            }

// do {
// generateNextValue(frame);
// } while (iterator.hasNext());

            try {
                while (true) {
                    // System.out.println("WHILEDA");
                    generateNextValue(frame);
                }
            } catch (StopIterationException e) {
                // fall through
            }

            iterator = null;
            throw StopIterationException.INSTANCE;
        }

        protected final void generateNextValue(VirtualFrame frame) {
            try {
                // currentValue = currentValue == null ? iterator.next() : currentValue;
                currentValue = currentValue == null ? iterator.__next__() : currentValue;
                ((WriteNode) target).executeWrite(frame, currentValue);

                if (!evaluateCondition(frame)) {
                    return;
                }

                body.execute(frame);
            } catch (StopIterationException sie) {
                // return to the loop header
                currentValue = null;
            }
        }
    }
}
