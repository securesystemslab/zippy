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
package edu.uci.python.nodes.control;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

@NodeChild(value = "iterator", type = GetIteratorNode.class)
public abstract class ForNode extends LoopNode {

    @Child protected PNode target;

    public ForNode(PNode body, PNode target) {
        super(body);
        this.target = target;
        assert target instanceof WriteNode;
    }

    protected ForNode(ForNode prev) {
        this(prev.body, prev.target);
    }

    public PNode getTarget() {
        return target;
    }

    public abstract PNode getIterator();

    @Override
    protected final void reportLoopCount(int count) {
        if (CompilerDirectives.inInterpreter()) {
            super.reportLoopCount(count);
        }
    }

    @Specialization(order = 1)
    public Object doPRange(VirtualFrame frame, PRangeIterator range) {
        final int start = range.getStart();
        final int stop = range.getStop();
        final int step = range.getStep();
        int count = 0;

        for (int i = start; i < stop; i += step) {
            ((WriteNode) target).executeWrite(frame, i);
            body.executeVoid(frame);

            if (CompilerDirectives.inInterpreter()) {
                count++;
            }
        }

        reportLoopCount(count);
        return PNone.NONE;
    }

    @Specialization(order = 2)
    public Object doIntegerSequenceIterator(VirtualFrame frame, PIntegerSequenceIterator iterator) {
        int count = 0;
        int index = 0;
        IntSequenceStorage store = iterator.getSequenceStorage();

        while (index < store.length()) {
            loopBodyBranch.enter();
            ((WriteNode) target).executeWrite(frame, store.getIntItemNormalized(index++));
            body.executeVoid(frame);

            if (CompilerDirectives.inInterpreter()) {
                count++;
            }
        }

        reportLoopCount(count);
        return PNone.NONE;
    }

    @Specialization(order = 3)
    public Object doIntegerIterator(VirtualFrame frame, PIntegerIterator iterator) {
        int count = 0;

        try {
            while (true) {
                loopBodyBranch.enter();
                ((WriteNode) target).executeWrite(frame, iterator.__nextInt__());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } catch (StopIterationException e) {

        } finally {
            reportLoopCount(count);
        }

        return PNone.NONE;
    }

    @Specialization(order = 5)
    public Object doDoubleIterator(VirtualFrame frame, PDoubleIterator iterator) {
        int count = 0;

        try {
            while (true) {
                loopBodyBranch.enter();
                ((WriteNode) target).executeWrite(frame, iterator.__nextDouble__());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } catch (StopIterationException e) {

        } finally {
            reportLoopCount(count);
        }

        return PNone.NONE;
    }

    @Specialization(order = 6, guards = "isObjectStorageIterator")
    public Object doObjectStorageIterator(VirtualFrame frame, PSequenceIterator iterator) {
        int count = 0;
        int index = 0;
        PList list = (PList) iterator.getSeqence();
        ObjectSequenceStorage store = (ObjectSequenceStorage) list.getStorage();

        while (index < store.length()) {
            loopBodyBranch.enter();
            ((WriteNode) target).executeWrite(frame, store.getItemNormalized(index++));
            body.executeVoid(frame);

            if (CompilerDirectives.inInterpreter()) {
                count++;
            }
        }

        reportLoopCount(count);
        return PNone.NONE;
    }

    @Specialization(order = 7)
    public Object doIterator(VirtualFrame frame, PSequenceIterator iterator) {
        int count = 0;
        int index = 0;
        PSequence sequence = iterator.getSeqence();

        while (index < sequence.len()) {
            loopBodyBranch.enter();
            ((WriteNode) target).executeWrite(frame, sequence.getItem(index++));
            body.executeVoid(frame);

            if (CompilerDirectives.inInterpreter()) {
                count++;
            }
        }

        reportLoopCount(count);
        return PNone.NONE;
    }

    @Specialization(order = 8)
    public Object doIterator(VirtualFrame frame, PIterator iterator) {
        int count = 0;

        try {
            while (true) {
                loopBodyBranch.enter();
                ((WriteNode) target).executeWrite(frame, iterator.__next__());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } catch (StopIterationException e) {

        } finally {
            reportLoopCount(count);
        }

        return PNone.NONE;
    }

}
