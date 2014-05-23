/*
 * Copyright (c) 2014, Regents of the University of California
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
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

public abstract class TupleLiteralNode extends LiteralNode {

    @Children protected final PNode[] values;

    public TupleLiteralNode(PNode[] values) {
        this.values = values;
    }

    @ExplodeLoop
    protected PTuple doGeneric(VirtualFrame frame, Object[] evaluated) {
        transferToInterpreterAndInvalidate();
        Object[] elements = new Object[values.length];

        for (int i = 0; i < values.length; i++) {
            if (i < evaluated.length) {
                elements[i] = evaluated[i];
            } else {
                elements[i] = values[i].execute(frame);
            }
        }

        replace(new ObjectTupleLiteralNode(values));
        return new PObjectTuple(elements);
    }

    @Override
    public String toString() {
        return "tuple";
    }

    public static class UninitializedTupleLiteralNode extends TupleLiteralNode {

        public UninitializedTupleLiteralNode(PNode[] values) {
            super(values);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            transferToInterpreterAndInvalidate();
            if (values.length == 0) {
                replace(new ObjectTupleLiteralNode(values));
                return new PObjectTuple(new Object[0]);
            }

            final Object[] elements = new Object[values.length];

            for (int i = 0; i < values.length; i++) {
                elements[i] = values[i].execute(frame);
            }

            if (PythonOptions.UnboxSequenceStorage) {
                if (SequenceStorageFactory.canSpecializeToInt(elements)) {
                    replace(new IntTupleLiteralNode(values));
                    return new PIntTuple(SequenceStorageFactory.specializeToInt(elements));
                } else if (SequenceStorageFactory.canSpecializeToDouble(elements)) {
                    replace(new DoubleTupleLiteralNode(values));
                    return new PDoubleTuple(SequenceStorageFactory.specializeToDouble(elements));
                } else {
                    replace(new ObjectTupleLiteralNode(values));
                    return new PObjectTuple(elements);
                }
            } else {
                replace(new ObjectTupleLiteralNode(values));
                return new PObjectTuple(elements);
            }
        }
    }

    public static final class IntTupleLiteralNode extends TupleLiteralNode {

        public IntTupleLiteralNode(PNode[] values) {
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

            return new PIntTuple(elements);
        }
    }

    public static final class DoubleTupleLiteralNode extends TupleLiteralNode {

        public DoubleTupleLiteralNode(PNode[] values) {
            super(values);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            final double[] elements = new double[values.length];

            for (int i = 0; i < values.length; i++) {
                try {
                    elements[i] = values[i].executeDouble(frame);
                } catch (UnexpectedResultException e) {
                    final Object[] evaluated = new Object[i];

                    for (int j = 0; j < i; j++) {
                        evaluated[j] = elements[j];
                    }

                    doGeneric(frame, evaluated);
                }
            }

            return new PDoubleTuple(elements);
        }
    }

    public static final class ObjectTupleLiteralNode extends TupleLiteralNode {

        public ObjectTupleLiteralNode(PNode[] values) {
            super(values);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            final Object[] elements = new Object[values.length];

            for (int i = 0; i < values.length; i++) {
                elements[i] = values[i].execute(frame);
            }

            return new PObjectTuple(elements);
        }
    }

}
