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
package edu.uci.python.nodes.frame;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;

public abstract class ReadLocalVariableNode extends ReadVariableNode {

    public ReadLocalVariableNode(FrameSlot frameSlot) {
        super(frameSlot);
    }

    protected ReadLocalVariableNode(ReadLocalVariableNode prev) {
        this(prev.frameSlot);
    }

    public static ReadLocalVariableNode create(FrameSlot frameSlot) {
        return new ReadLocalVariableUninitializedNode(frameSlot);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return WriteLocalVariableNodeFactory.create(frameSlot, rhs);
    }

    @Override
    protected final ReadLocalVariableNode createUninitialized(FrameSlot slot, int level) {
        return new ReadLocalVariableUninitializedNode(frameSlot);
    }

    @Override
    protected final ReadVariableNode createReadBoolean(ReadVariableNode prev) {
        return new ReadLocalVariableBooleanNode((ReadLocalVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadInt(ReadVariableNode prev) {
        return new ReadLocalVariableIntNode((ReadLocalVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadDouble(ReadVariableNode prev) {
        return new ReadLocalVariableDoubleNode((ReadLocalVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadObject(ReadVariableNode prev) {
        return new ReadLocalVariableObjectNode((ReadLocalVariableNode) prev);
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    private static final class ReadLocalVariableUninitializedNode extends ReadLocalVariableNode {

        ReadLocalVariableUninitializedNode(FrameSlot slot) {
            super(slot);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return specialize(frame, frame);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    private static final class ReadLocalVariableBooleanNode extends ReadLocalVariableNode {

        ReadLocalVariableBooleanNode(ReadLocalVariableNode copy) {
            super(copy);
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            return doBooleanUnboxed(frame, frame);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return doBooleanBoxed(frame, frame);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    private static final class ReadLocalVariableIntNode extends ReadLocalVariableNode {

        ReadLocalVariableIntNode(ReadLocalVariableNode copy) {
            super(copy);
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            if (frameSlot.getKind() == FrameSlotKind.Int) {
                return getInteger(frame);
            } else {
                return PythonTypesGen.PYTHONTYPES.expectInteger(executeNext(frame));
            }
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return doIntBoxed(frame, frame);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    private static final class ReadLocalVariableDoubleNode extends ReadLocalVariableNode {

        ReadLocalVariableDoubleNode(ReadLocalVariableNode copy) {
            super(copy);
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            return doDoubleUnboxed(frame, frame);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return doDoubleBoxed(frame, frame);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    private static final class ReadLocalVariableObjectNode extends ReadLocalVariableNode {

        ReadLocalVariableObjectNode(ReadLocalVariableNode copy) {
            super(copy);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            if (frame.isObject(frameSlot)) {
                return getObject(frame);
            } else {
                return executeNext(frame);
            }
        }
    }

}
