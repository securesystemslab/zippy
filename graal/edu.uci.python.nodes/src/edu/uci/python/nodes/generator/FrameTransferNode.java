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

import java.math.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.runtime.function.*;

/**
 * Transfer a local variable value from the current frame to a cargo frame.
 */
@NodeChild(value = "right", type = PNode.class)
public abstract class FrameTransferNode extends FrameSlotNode {

    public FrameTransferNode(FrameSlot slot) {
        super(slot);
    }

    protected FrameTransferNode(FrameTransferNode prev) {
        super(prev.frameSlot);
    }

    @Specialization(order = 0, guards = "isBooleanKind")
    public boolean write(VirtualFrame frame, boolean right) {
        VirtualFrame cargoFrame = PArguments.getVirtualFrameCargoArguments(frame);
        assert frameSlot.getFrameDescriptor() == cargoFrame.getFrameDescriptor();
        cargoFrame.setBoolean(frameSlot, right);
        return right;
    }

    @Specialization(guards = "isIntegerKind")
    public int doInteger(VirtualFrame frame, int value) {
        VirtualFrame cargoFrame = PArguments.getVirtualFrameCargoArguments(frame);
        assert frameSlot.getFrameDescriptor() == cargoFrame.getFrameDescriptor();
        cargoFrame.setInt(frameSlot, value);
        return value;
    }

    @Specialization(guards = "isIntOrObjectKind")
    public BigInteger write(VirtualFrame frame, BigInteger value) {
        VirtualFrame cargoFrame = PArguments.getVirtualFrameCargoArguments(frame);
        assert frameSlot.getFrameDescriptor() == cargoFrame.getFrameDescriptor();
        setObject(cargoFrame, value);
        return value;
    }

    @Specialization(guards = "isDoubleKind")
    public double doDouble(VirtualFrame frame, double right) {
        VirtualFrame cargoFrame = PArguments.getVirtualFrameCargoArguments(frame);
        assert frameSlot.getFrameDescriptor() == cargoFrame.getFrameDescriptor();
        cargoFrame.setDouble(frameSlot, right);
        return right;
    }

    @Specialization(guards = "isObjectKind")
    public Object write(VirtualFrame frame, Object right) {
        VirtualFrame cargoFrame = PArguments.getVirtualFrameCargoArguments(frame);
        assert frameSlot.getFrameDescriptor() == cargoFrame.getFrameDescriptor();
        setObject(cargoFrame, right);
        return right;
    }

}
