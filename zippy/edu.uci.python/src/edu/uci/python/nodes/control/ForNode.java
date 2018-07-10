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

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

import edu.uci.python.ast.VisitorIF;
import edu.uci.python.nodes.PNode;
import edu.uci.python.nodes.frame.WriteNode;
import edu.uci.python.nodes.generator.ComprehensionNode.ListComprehensionNode;
import edu.uci.python.nodes.generator.ListAppendNode;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.datatype.PGenerator;
import edu.uci.python.runtime.datatype.PNone;
import edu.uci.python.runtime.exception.StopIterationException;
import edu.uci.python.runtime.iterator.PDoubleIterator;
import edu.uci.python.runtime.iterator.PIntegerIterator;
import edu.uci.python.runtime.iterator.PIntegerSequenceIterator;
import edu.uci.python.runtime.iterator.PIterator;
import edu.uci.python.runtime.iterator.PLongIterator;
import edu.uci.python.runtime.iterator.PLongSequenceIterator;
import edu.uci.python.runtime.iterator.PRangeIterator;
import edu.uci.python.runtime.iterator.PSequenceIterator;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.sequence.PSequence;
import edu.uci.python.runtime.sequence.storage.BasicSequenceStorage;
import edu.uci.python.runtime.sequence.storage.IntSequenceStorage;
import edu.uci.python.runtime.sequence.storage.LongSequenceStorage;
import edu.uci.python.runtime.sequence.storage.ObjectSequenceStorage;

@NodeInfo(shortName = "for")
@NodeChild(value = "iterator", type = GetIteratorNode.class)
@GenerateNodeFactory
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

    @Specialization
    public Object doPRange(VirtualFrame frame, PRangeIterator range) {
        final int start = range.getStart();
        final int stop = range.getStop();
        final int step = range.getStep();
        int i = start;
        if (i < stop) {
            // execute once to specialize the storage
            ((WriteNode) target).executeWrite(frame, i);
            body.executeVoid(frame);
            i += step;

            if (i < stop) {
                PList list = comprehensionHelper(frame);
                if (list != null)
                    ((BasicSequenceStorage) list.getStorage()).increaseCapacity((stop - i) / step + list.len());

                for (; i < stop; i += step) {
                    ((WriteNode) target).executeWrite(frame, i);
                    body.executeVoid(frame);
                }
            }
        }
        return PNone.NONE;
    }

    private PList comprehensionHelper(VirtualFrame frame) {
        if ((this.getParent() instanceof ListComprehensionNode)) {
            if (body instanceof ListAppendNode) {
                Object o = ((ListAppendNode) body).getLeftNode().execute(frame);
                if (o instanceof PList) {
                    PList list = (PList) o;
                    if (list.getStorage() instanceof BasicSequenceStorage)
                        return list;
                }
            }
        }
        return null;
    }

    @Specialization
    public Object doIntegerSequenceIterator(VirtualFrame frame, PIntegerSequenceIterator iterator) {
        @SuppressWarnings("unused")
        int count = 0;
        IntSequenceStorage store = iterator.getSequenceStorage();

        for (int index = 0; index < store.length(); index++) {
            ((WriteNode) target).executeWrite(frame, store.getIntItemNormalized(index));
            body.executeVoid(frame);

            if (CompilerDirectives.inInterpreter()) {
                count++;
            }
        }

        return PNone.NONE;
    }

    @Specialization
    public Object doIntegerIterator(VirtualFrame frame, PIntegerIterator iterator) {
        @SuppressWarnings("unused")
        int count = 0;

        try {
            while (true) {
                ((WriteNode) target).executeWrite(frame, iterator.__nextInt__());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } catch (StopIterationException e) {

        }

        return PNone.NONE;
    }

    @Specialization
    public Object doLongSequenceIterator(VirtualFrame frame, PLongSequenceIterator iterator) {
        @SuppressWarnings("unused")
        int count = 0;
        LongSequenceStorage store = iterator.getSequenceStorage();

        for (int index = 0; index < store.length(); index++) {
            ((WriteNode) target).executeWrite(frame, store.getLongItemNormalized(index));
            body.executeVoid(frame);

            if (CompilerDirectives.inInterpreter()) {
                count++;
            }
        }

        return PNone.NONE;
    }

    @Specialization
    public Object doLongIterator(VirtualFrame frame, PLongIterator iterator) {
        @SuppressWarnings("unused")
        int count = 0;

        try {
            while (true) {
                ((WriteNode) target).executeWrite(frame, iterator.__nextLong__());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } catch (StopIterationException e) {

        }

        return PNone.NONE;
    }

    @Specialization
    public Object doDoubleIterator(VirtualFrame frame, PDoubleIterator iterator) {
        @SuppressWarnings("unused")
        int count = 0;

        try {
            while (true) {
                ((WriteNode) target).executeWrite(frame, iterator.__nextDouble__());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } catch (StopIterationException e) {

        }

        return PNone.NONE;
    }

    @Specialization(guards = "isObjectStorageIterator(iterator)")
    public Object doObjectStorageIterator(VirtualFrame frame, PSequenceIterator iterator) {
        @SuppressWarnings("unused")
        int count = 0;
        PList list = (PList) iterator.getSeqence();
        ObjectSequenceStorage store = (ObjectSequenceStorage) list.getStorage();

        for (int index = 0; index < store.length(); index++) {
            ((WriteNode) target).executeWrite(frame, store.getItemNormalized(index));
            body.executeVoid(frame);

            if (CompilerDirectives.inInterpreter()) {
                count++;
            }
        }
        return PNone.NONE;
    }

    @Specialization
    public Object doIterator(VirtualFrame frame, PSequenceIterator iterator) {
        @SuppressWarnings("unused")
        int count = 0;
        PSequence sequence = iterator.getSeqence();

        for (int index = 0; index < sequence.len(); index++) {
            ((WriteNode) target).executeWrite(frame, sequence.getItem(index));
            body.executeVoid(frame);

            if (CompilerDirectives.inInterpreter()) {
                count++;
            }
        }
        return PNone.NONE;
    }

    @Specialization
    public Object doGenerator(VirtualFrame frame, PGenerator generator) {
        int count = 0;

        try {
            while (true) {
                ((WriteNode) target).executeWrite(frame, generator.__next__());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } catch (StopIterationException e) {

        }

        if (CompilerDirectives.inInterpreter()) {
            if (count > 0 && PythonOptions.InlineGeneratorCalls) {
                CompilerAsserts.neverPartOfCompilation();
                /**
                 * TODO zwei: This is probably a better place to peel generators than the
                 * complicated logic we currently have in FunctionRootNode now.
                 */
            }
        }

        return PNone.NONE;
    }

    @Specialization
    public Object doIterator(VirtualFrame frame, PIterator iterator) {
        @SuppressWarnings("unused")
        int count = 0;

        try {
            while (true) {
                ((WriteNode) target).executeWrite(frame, iterator.__next__());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } catch (StopIterationException e) {

        }

        return PNone.NONE;
    }

    @Override
    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitForNode(this);
    }

}
