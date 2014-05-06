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
package edu.uci.python.nodes.literal;

import static com.oracle.truffle.api.CompilerDirectives.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

public abstract class ListLiteralNode extends LiteralNode {

    @Children protected final PNode[] values;

    public ListLiteralNode(PNode[] values) {
        this.values = values;
    }

    @ExplodeLoop
    protected PList doGeneric(VirtualFrame frame, Object[] evaluated) {
        transferToInterpreterAndInvalidate();
        Object[] elements = new Object[values.length];

        for (int i = 0; i < values.length; i++) {
            if (i < evaluated.length) {
                elements[i] = evaluated[i];
            } else {
                elements[i] = values[i].execute(frame);
            }
        }

        replace(new ObjectListLiteralNode(values));
        return new PList(SequenceStorageFactory.createStorage(evaluated));
    }

    @Override
    public String toString() {
        return "list";
    }

    public static class UninitializedListLiteralNode extends ListLiteralNode {

        public UninitializedListLiteralNode(PNode[] values) {
            super(values);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            transferToInterpreterAndInvalidate();
            final Object[] elements = new Object[values.length];

            for (int i = 0; i < values.length; i++) {
                elements[i] = values[i].execute(frame);
            }

            PList list = new PList(SequenceStorageFactory.createStorage(elements));
            SequenceStorage store = list.getStorage();

            if (store instanceof EmptySequenceStorage) {
                replace(new EmptyListLiteralNode(values));
            } else if (store instanceof IntSequenceStorage) {
                replace(new IntListLiteralNode(values));
            } else if (store instanceof DoubleSequenceStorage) {
                replace(new DoubleListLiteralNode(values));
            } else {
                replace(new ObjectListLiteralNode(values));
            }

            return list;
        }
    }

    public static final class EmptyListLiteralNode extends ListLiteralNode {

        public EmptyListLiteralNode(PNode[] values) {
            super(values);
            assert values.length == 0;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return new PList(SequenceStorageFactory.createStorage(null));
        }
    }

    public static final class IntListLiteralNode extends ListLiteralNode {

        public IntListLiteralNode(PNode[] values) {
            super(values);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            final int[] elements = new int[values.length];

            for (int i = 0; i < values.length; i++) {
                try {
                    elements[i] = values[i].executeInt(frame);
                } catch (UnexpectedResultException e) {
                    final Object[] evaluated = new Object[i];

                    for (int j = 0; j < i; j++) {
                        evaluated[j] = elements[j];
                    }

                    doGeneric(frame, evaluated);
                }
            }

            return new PList(new IntSequenceStorage(elements));
        }
    }

    public static final class DoubleListLiteralNode extends ListLiteralNode {

        public DoubleListLiteralNode(PNode[] values) {
            super(values);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            final double[] elements = new double[values.length];

            for (int i = 0; i < values.length; i++) {
                try {
                    elements[i] = values[i].executeInt(frame);
                } catch (UnexpectedResultException e) {
                    final Object[] evaluated = new Object[i];

                    for (int j = 0; j < i; j++) {
                        evaluated[j] = elements[j];
                    }

                    doGeneric(frame, evaluated);
                }
            }

            return new PList(new DoubleSequenceStorage(elements));
        }
    }

    public static final class ObjectListLiteralNode extends ListLiteralNode {

        public ObjectListLiteralNode(PNode[] values) {
            super(values);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            final Object[] elements = new Object[values.length];

            for (int i = 0; i < values.length; i++) {
                elements[i] = values[i].execute(frame);
            }

            return new PList(new ObjectSequenceStorage(elements));
        }
    }

}
