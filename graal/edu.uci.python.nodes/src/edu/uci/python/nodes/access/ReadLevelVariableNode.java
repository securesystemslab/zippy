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
package edu.uci.python.nodes.access;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.nodes.NodeInfo.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;

public abstract class ReadLevelVariableNode extends FrameSlotNode implements ReadNode {

    @Child protected ReadLevelVariableNode next;
    final int level;

    public ReadLevelVariableNode(FrameSlot slot, int level) {
        super(slot);
        this.level = level;
    }

    public ReadLevelVariableNode(ReadLevelVariableNode specialized) {
        this(specialized.frameSlot, specialized.level);
    }

    public static ReadLevelVariableNode create(FrameSlot frameSlot, int level) {
        return new ReadLevelVariableUninitializedNode(frameSlot, level);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return WriteLocalVariableNodeFactory.create(frameSlot, rhs);
    }

    @Override
    public final Object executeWrite(VirtualFrame frame, Object value) {
        throw new UnsupportedOperationException();
    }

    protected Object executeNext(VirtualFrame frame) {
        if (next == null) {
            CompilerDirectives.transferToInterpreter();
            next = adoptChild(new ReadLevelVariableUninitializedNode(frameSlot, level));
        }

        return next.execute(frame);
    }

    @NodeInfo(kind = Kind.UNINITIALIZED)
    private static final class ReadLevelVariableUninitializedNode extends ReadLevelVariableNode {

        ReadLevelVariableUninitializedNode(FrameSlot slot, int level) {
            super(slot, level);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            CompilerDirectives.transferToInterpreter();
            ReadLevelVariableNode readNode;

            if (!isNotIllegal() && !frameSlot.getIdentifier().equals("<return_val>")) {
                throw Py.UnboundLocalError("local variable '" + frameSlot.getIdentifier() + "' referenced before assignment");
            }

            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            if (parent.isObject(frameSlot)) {
                readNode = new ReadLevelVariableObjectNode(this);
            } else if (parent.isInt(frameSlot)) {
                readNode = new ReadLevelVariableIntNode(this);
            } else if (parent.isDouble(frameSlot)) {
                readNode = new ReadLevelVariableDoubleNode(this);
            } else if (parent.isBoolean(frameSlot)) {
                readNode = new ReadLevelVariableBooleanNode(this);
            } else {
                throw new UnsupportedOperationException("frame slot kind?");
            }

            return replace(readNode).execute(frame);
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class ReadLevelVariableBooleanNode extends ReadLevelVariableNode {

        ReadLevelVariableBooleanNode(ReadLevelVariableNode copy) {
            super(copy);
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            if (frameSlot.getKind() == FrameSlotKind.Boolean) {
                return getBoolean(parent);
            } else {
                return PythonTypesGen.PYTHONTYPES.expectBoolean(executeNext(frame));
            }
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            if (frameSlot.getKind() == FrameSlotKind.Boolean) {
                return getBoolean(parent);
            } else {
                return executeNext(frame);
            }
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class ReadLevelVariableIntNode extends ReadLevelVariableNode {

        ReadLevelVariableIntNode(ReadLevelVariableNode copy) {
            super(copy);
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            if (frameSlot.getKind() == FrameSlotKind.Int) {
                return getInteger(parent);
            } else {
                return PythonTypesGen.PYTHONTYPES.expectInteger(executeNext(frame));
            }
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            if (frameSlot.getKind() == FrameSlotKind.Int) {
                return getInteger(parent);
            } else {
                return executeNext(frame);
            }
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class ReadLevelVariableDoubleNode extends ReadLevelVariableNode {

        ReadLevelVariableDoubleNode(ReadLevelVariableNode copy) {
            super(copy);
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            if (frameSlot.getKind() == FrameSlotKind.Double) {
                return getDouble(parent);
            } else {
                return PythonTypesGen.PYTHONTYPES.expectDouble(executeNext(frame));
            }
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            if (frameSlot.getKind() == FrameSlotKind.Double) {
                return getDouble(parent);
            } else {
                return executeNext(frame);
            }
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class ReadLevelVariableObjectNode extends ReadLevelVariableNode {

        ReadLevelVariableObjectNode(ReadLevelVariableNode copy) {
            super(copy);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            if (parent.isObject(frameSlot)) {
                return getObject(parent);
            } else {
                return executeNext(frame);
            }
        }
    }

}
