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
package edu.uci.python.nodes.frame;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.truffle.*;

public abstract class ReadVariableNode extends FrameSlotNode implements ReadNode {

    @Child protected ReadVariableNode next;

    public ReadVariableNode(FrameSlot slot) {
        super(slot);
    }

    @Override
    public final Object executeWrite(VirtualFrame frame, Object value) {
        throw new UnsupportedOperationException();
    }

    protected abstract ReadVariableNode createUninitialized(FrameSlot slot, int level);

    protected abstract ReadVariableNode createReadBoolean(ReadVariableNode prev);

    protected abstract ReadVariableNode createReadInt(ReadVariableNode prev);

    protected abstract ReadVariableNode createReadDouble(ReadVariableNode prev);

    protected abstract ReadVariableNode createReadObject(ReadVariableNode prev);

    protected final Object executeNext(VirtualFrame frame) {
        if (next == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            next = insert(createUninitialized(frameSlot, 0));
        }

        return next.execute(frame);
    }

    protected final Object specialize(VirtualFrame frame, Frame accessingFrame) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        ReadVariableNode readNode;

        if (!isNotIllegal() && !frameSlot.getIdentifier().equals("<return_val>")) {
            throw Py.UnboundLocalError("local variable '" + frameSlot.getIdentifier() + "' referenced before assignment");
        }

        if (accessingFrame.isObject(frameSlot)) {
            readNode = createReadObject(this);
        } else if (accessingFrame.isInt(frameSlot)) {
            readNode = createReadInt(this);
        } else if (accessingFrame.isDouble(frameSlot)) {
            readNode = createReadDouble(this);
        } else if (accessingFrame.isBoolean(frameSlot)) {
            readNode = createReadBoolean(this);
        } else {
            throw new UnsupportedOperationException("frame slot kind?");
        }

        return replace(readNode).execute(frame);
    }

    protected final boolean doBooleanUnboxed(VirtualFrame frame, Frame accessingFrame) throws UnexpectedResultException {
        if (frameSlot.getKind() == FrameSlotKind.Boolean) {
            return getBoolean(accessingFrame);
        } else {
            return PythonTypesGen.PYTHONTYPES.expectBoolean(executeNext(frame));
        }
    }

    protected final Object doBooleanBoxed(VirtualFrame frame, Frame accessingFrame) {
        if (frameSlot.getKind() == FrameSlotKind.Boolean) {
            return getBoolean(accessingFrame);
        } else {
            return executeNext(frame);
        }
    }

    protected final int doIntUnboxed(VirtualFrame frame, Frame accessingFrame) throws UnexpectedResultException {
        if (frameSlot.getKind() == FrameSlotKind.Int) {
            return getInteger(accessingFrame);
        } else {
            return PythonTypesGen.PYTHONTYPES.expectInteger(executeNext(frame));
        }
    }

    protected final Object doIntBoxed(VirtualFrame frame, Frame accessingFrame) {
        if (frameSlot.getKind() == FrameSlotKind.Int) {
            return getInteger(accessingFrame);
        } else {
            return executeNext(frame);
        }
    }

    protected final double doDoubleUnboxed(VirtualFrame frame, Frame accessingFrame) throws UnexpectedResultException {
        if (frameSlot.getKind() == FrameSlotKind.Double) {
            return getDouble(accessingFrame);
        } else {
            return PythonTypesGen.PYTHONTYPES.expectDouble(executeNext(frame));
        }
    }

    protected final Object doDoubleBoxed(VirtualFrame frame, Frame accessingFrame) {
        if (frameSlot.getKind() == FrameSlotKind.Double) {
            return getDouble(accessingFrame);
        } else {
            return executeNext(frame);
        }
    }

    protected final Object doObject(VirtualFrame frame, Frame accessingFrame) {
        if (accessingFrame.isObject(frameSlot)) {
            return getObject(accessingFrame);
        } else {
            return executeNext(frame);
        }
    }

}
