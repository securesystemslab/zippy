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

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.nodes.NodeInfo.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.function.*;

public abstract class ReadGeneratorFrameVariableNode extends ReadVariableNode {

    public ReadGeneratorFrameVariableNode(FrameSlot slot) {
        super(slot);
    }

    protected ReadGeneratorFrameVariableNode(ReadGeneratorFrameVariableNode specialized) {
        this(specialized.frameSlot);
    }

    public static ReadGeneratorFrameVariableNode create(FrameSlot frameSlot) {
        return new ReadGeneratorFrameVariableUninitializedNode(frameSlot);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return WriteGeneratorFrameVariableNodeFactory.create(frameSlot, rhs);
    }

    @Override
    protected final ReadGeneratorFrameVariableNode createUninitialized(FrameSlot slot, int parentFrameLevel) {
        return new ReadGeneratorFrameVariableUninitializedNode(frameSlot);
    }

    @Override
    protected final ReadVariableNode createReadBoolean(ReadVariableNode prev) {
        return new ReadGeneratorFrameVariableBooleanNode((ReadGeneratorFrameVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadInt(ReadVariableNode prev) {
        return new ReadGeneratorFrameVariableIntNode((ReadGeneratorFrameVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadDouble(ReadVariableNode prev) {
        return new ReadGeneratorFrameVariableDoubleNode((ReadGeneratorFrameVariableNode) prev);
    }

    @Override
    protected final ReadVariableNode createReadObject(ReadVariableNode prev) {
        return new ReadGeneratorFrameVariableObjectNode((ReadGeneratorFrameVariableNode) prev);
    }

    @NodeInfo(kind = Kind.UNINITIALIZED)
    private static final class ReadGeneratorFrameVariableUninitializedNode extends ReadGeneratorFrameVariableNode {

        ReadGeneratorFrameVariableUninitializedNode(FrameSlot slot) {
            super(slot);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
            return specialize(frame, mframe);
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class ReadGeneratorFrameVariableBooleanNode extends ReadGeneratorFrameVariableNode {

        ReadGeneratorFrameVariableBooleanNode(ReadGeneratorFrameVariableNode copy) {
            super(copy);
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
            return doBooleanUnboxed(frame, mframe);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
            return doBooleanBoxed(frame, mframe);
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class ReadGeneratorFrameVariableIntNode extends ReadGeneratorFrameVariableNode {

        ReadGeneratorFrameVariableIntNode(ReadGeneratorFrameVariableNode copy) {
            super(copy);
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
            if (frameSlot.getKind() == FrameSlotKind.Int) {
                return getInteger(mframe);
            } else {
                return PythonTypesGen.PYTHONTYPES.expectInteger(executeNext(frame));
            }
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
            return doIntBoxed(frame, mframe);
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class ReadGeneratorFrameVariableDoubleNode extends ReadGeneratorFrameVariableNode {

        ReadGeneratorFrameVariableDoubleNode(ReadGeneratorFrameVariableNode copy) {
            super(copy);
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
            return doDoubleUnboxed(frame, mframe);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
            return doDoubleBoxed(frame, mframe);
        }
    }

    @NodeInfo(kind = Kind.SPECIALIZED)
    private static final class ReadGeneratorFrameVariableObjectNode extends ReadGeneratorFrameVariableNode {

        ReadGeneratorFrameVariableObjectNode(ReadGeneratorFrameVariableNode copy) {
            super(copy);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
            if (mframe.isObject(frameSlot)) {
                return getObject(mframe);
            } else {
                return executeNext(frame);
            }
        }
    }

}
