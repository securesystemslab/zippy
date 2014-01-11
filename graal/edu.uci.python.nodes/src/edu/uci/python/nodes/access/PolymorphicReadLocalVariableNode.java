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
package edu.uci.python.nodes.access;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.nodes.NodeInfo.Kind;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;

public abstract class PolymorphicReadLocalVariableNode extends FrameSlotNode implements ReadNode {

    @Child PolymorphicReadLocalVariableNode next;

    public PolymorphicReadLocalVariableNode(FrameSlot frameSlot) {
        super(frameSlot);
    }

    protected PolymorphicReadLocalVariableNode(PolymorphicReadLocalVariableNode prev) {
        this(prev.frameSlot);
    }

    public static PolymorphicReadLocalVariableNode create(FrameSlot frameSlot) {
        return new PolymorphicReadLocalVariableUninitializedNode(frameSlot);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return WriteLocalVariableNodeFactory.create(frameSlot, rhs);
    }

    @Override
    public final Object executeWrite(VirtualFrame frame, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Catches FrameSlotTypeException.
     */
    @Override
    protected final Object getObject(Frame frame) {
        try {
            return frame.getObject(frameSlot);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public final boolean getBoolean(Frame frame) {
        try {
            return frame.getBoolean(frameSlot);
        } catch (FrameSlotTypeException ex) {
            throw new IllegalStateException();
        }
    }

    @Override
    public final int getInteger(Frame frame) {
        try {
            return frame.getInt(frameSlot);
        } catch (FrameSlotTypeException ex) {
            throw new IllegalStateException();
        }
    }

    @Override
    public final double getDouble(Frame frame) {
        try {
            return frame.getDouble(frameSlot);
        } catch (FrameSlotTypeException ex) {
            throw new IllegalStateException();
        }
    }

    protected final Object executeNext(VirtualFrame frame) {
        if (next == null) {
            CompilerDirectives.transferToInterpreter();
            next = adoptChild(new PolymorphicReadLocalVariableUninitializedNode(frameSlot));
        }

        return next.execute(frame);
    }

    @NodeInfo(kind = Kind.UNINITIALIZED)
    private static final class PolymorphicReadLocalVariableUninitializedNode extends PolymorphicReadLocalVariableNode {

        PolymorphicReadLocalVariableUninitializedNode(FrameSlot slot) {
            super(slot);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            CompilerDirectives.transferToInterpreter();
            PolymorphicReadLocalVariableNode readNode;

            if (frame.isObject(frameSlot)) {
                readNode = new PolymorphicReadLocalVariableObjectNode(this);
            } else if (frame.isInt(frameSlot)) {
                readNode = new PolymorphicReadLocalVariableIntNode(this);
            } else if (frame.isDouble(frameSlot)) {
                readNode = new PolymorphicReadLocalVariableDoubleNode(this);
            } else if (frame.isBoolean(frameSlot)) {
                readNode = new PolymorphicReadLocalVariableBooleanNode(this);
            } else {
                throw new UnsupportedOperationException("frame slot kind?");
            }

            return replace(readNode).execute(frame);
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class PolymorphicReadLocalVariableBooleanNode extends PolymorphicReadLocalVariableNode {

        PolymorphicReadLocalVariableBooleanNode(PolymorphicReadLocalVariableNode copy) {
            super(copy);
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            if (frame.isBoolean(frameSlot)) {
                return getBoolean(frame);
            } else {
                return PythonTypesGen.PYTHONTYPES.expectBoolean(executeNext(frame));
            }
        }

        @Override
        public Object execute(VirtualFrame frame) {
            if (frame.isBoolean(frameSlot)) {
                return getBoolean(frame);
            } else {
                return executeNext(frame);
            }
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class PolymorphicReadLocalVariableIntNode extends PolymorphicReadLocalVariableNode {

        PolymorphicReadLocalVariableIntNode(PolymorphicReadLocalVariableNode copy) {
            super(copy);
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            if (frame.isInt(frameSlot)) {
                return super.getInteger(frame);
            } else {
                return PythonTypesGen.PYTHONTYPES.expectInteger(executeNext(frame));
            }
        }

        @Override
        public Object execute(VirtualFrame frame) {
            if (frameSlot.getKind() != FrameSlotKind.Double && frame.isInt(frameSlot)) {
                return super.getInteger(frame);
            } else {
                return executeNext(frame);
            }
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class PolymorphicReadLocalVariableDoubleNode extends PolymorphicReadLocalVariableNode {

        PolymorphicReadLocalVariableDoubleNode(PolymorphicReadLocalVariableNode copy) {
            super(copy);
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            if (frame.isDouble(frameSlot)) {
                return super.getDouble(frame);
            } else {
                return PythonTypesGen.PYTHONTYPES.expectDouble(executeNext(frame));
            }
        }

        @Override
        public Object execute(VirtualFrame frame) {
            if (frame.isDouble(frameSlot)) {
                return super.getDouble(frame);
            } else {
                return executeNext(frame);
            }
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class PolymorphicReadLocalVariableObjectNode extends PolymorphicReadLocalVariableNode {

        PolymorphicReadLocalVariableObjectNode(PolymorphicReadLocalVariableNode copy) {
            super(copy);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            if (frame.isObject(frameSlot)) {
                return super.getObject(frame);
            } else {
                return executeNext(frame);
            }
        }
    }

}
