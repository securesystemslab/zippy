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
package edu.uci.python.nodes.statements;

import java.util.*;

import org.python.core.PyObject;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.translation.*;
import edu.uci.python.nodes.utils.*;

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

public class ForNode extends StatementNode {

    @Child protected PNode target;

    @Child protected PNode iterator;

    @Child protected BlockNode body;

    @Child protected BlockNode orelse;

    public ForNode(PNode target, PNode iterator, BlockNode body, BlockNode orelse) {
        this.target = adoptChild(target);
        this.iterator = adoptChild(iterator);
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }

    public void setInternal(BlockNode body, BlockNode orelse) {
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object evaluatedIterator = iterator.execute(frame);

        if (evaluatedIterator instanceof Iterable) {
            loopOnIterable(frame, (Iterable<?>) evaluatedIterator);
        } else if (evaluatedIterator instanceof PyObject) {
            loopOnPyObject(frame, (PyObject) evaluatedIterator);
        } else {
            throw new RuntimeException("Unexpected iterator type ");
        }

        return null;
    }

    private void loopOnIterable(VirtualFrame frame, Iterable<?> iterable) {
        Iterator<?> iter = iterable.iterator();
        RuntimeValueNode rvn = (RuntimeValueNode) ((WriteNode) target).getRhs();

        try {
            while (iter.hasNext()) {
                rvn.setValue(iter.next());
                target.execute(frame);

                try {
                    body.executeVoid(frame);
                } catch (ContinueException ex) {
                    // Fall through to next loop iteration.
                }
            }
        } catch (BreakException ex) {
            // Done executing this loop.
            // If there is a break, orelse should not be executed
            return;
        }

        orelse.executeVoid(frame);
    }

    private void loopOnPyObject(VirtualFrame frame, PyObject sequence) {
        PyObject pyIterator = sequence.__iter__();
        PyObject itValue;
        RuntimeValueNode rvn = (RuntimeValueNode) ((WriteNode) target).getRhs();

        try {
            while ((itValue = pyIterator.__iternext__()) != null) {
                rvn.setValue(unboxPyObject(itValue));
                target.execute(frame);

                try {
                    body.executeVoid(frame);
                } catch (ContinueException ex) {
                    // Fall through to next loop iteration.
                }
            }
        } catch (BreakException ex) {
            // Done executing this loop
            // If there is a break, orelse should not be executed
            return;
        }

        orelse.executeVoid(frame);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + target + ", " + iterator + ")";
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitForNode(this);
    }

    public static class GeneratorForNode extends ForNode {

        private Iterator<?> iter;

        public GeneratorForNode(ForNode node) {
            super(node.target, node.iterator, node.body, node.orelse);
        }

        @Override
        public void executeVoid(VirtualFrame frame) {
            if (iter == null) {
                Iterable<?> it = (Iterable<?>) this.iterator.execute(frame);
                iter = it.iterator();
            }

            RuntimeValueNode rvn = (RuntimeValueNode) ((WriteNode) target).getRhs();

            try {
                while (iter.hasNext()) {
                    rvn.setValue(iter.next());
                    target.execute(frame);

                    try {
                        body.executeVoid(frame);
                    } catch (ContinueException ex) {

                    }
                }
            } catch (BreakException ex) {
                return;
            }

            orelse.executeVoid(frame);
        }

    }

}
