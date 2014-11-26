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

@NodeInfo(shortName = "read_level")
public abstract class ReadLevelVariableNode extends ReadVariableNode {

    protected final int level;

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
    protected final ReadLevelVariableNode createUninitialized(FrameSlot slot, int parentFrameLevel) {
        return new ReadLevelVariableUninitializedNode(frameSlot, parentFrameLevel);
    }

    @Override
    protected final ReadVariableNode createReadBoolean(ReadVariableNode prev) {
        return new ReadLevelVariableBooleanNode((ReadLevelVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadInt(ReadVariableNode prev) {
        return new ReadLevelVariableIntNode((ReadLevelVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadDouble(ReadVariableNode prev) {
        return new ReadLevelVariableDoubleNode((ReadLevelVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadObject(ReadVariableNode prev) {
        return new ReadLevelVariableObjectNode((ReadLevelVariableNode) prev);
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    private static final class ReadLevelVariableUninitializedNode extends ReadLevelVariableNode {

        ReadLevelVariableUninitializedNode(FrameSlot slot, int level) {
            super(slot, level);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            return specialize(frame, parent);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    private static final class ReadLevelVariableBooleanNode extends ReadLevelVariableNode {

        ReadLevelVariableBooleanNode(ReadLevelVariableNode copy) {
            super(copy);
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            return doBooleanUnboxed(frame, parent);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            return doBooleanBoxed(frame, parent);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    private static final class ReadLevelVariableIntNode extends ReadLevelVariableNode {

        ReadLevelVariableIntNode(ReadLevelVariableNode copy) {
            super(copy);
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            return doIntUnboxed(frame, parent);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            return doIntBoxed(frame, parent);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    private static final class ReadLevelVariableDoubleNode extends ReadLevelVariableNode {

        ReadLevelVariableDoubleNode(ReadLevelVariableNode copy) {
            super(copy);
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            return doDoubleUnboxed(frame, parent);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            return doDoubleBoxed(frame, parent);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    private static final class ReadLevelVariableObjectNode extends ReadLevelVariableNode {

        ReadLevelVariableObjectNode(ReadLevelVariableNode copy) {
            super(copy);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame parent = FrameUtil.getParentFrame(frame, level);
            return doObject(frame, parent);
        }
    }

}
