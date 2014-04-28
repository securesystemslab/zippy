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
package edu.uci.python.nodes.generator;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.runtime.function.*;

/**
 * Reads from a generator frame that stores a generator node flag, like FirstEntry.
 */
public abstract class ReadGeneratorFrameFlagNode extends FrameSlotNode implements ReadNode {

    public ReadGeneratorFrameFlagNode(FrameSlot slot) {
        super(slot);
    }

    public ReadGeneratorFrameFlagNode(ReadGeneratorFrameFlagNode specialized) {
        this(specialized.frameSlot);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return WriteGeneratorFrameVariableNodeFactory.create(frameSlot, rhs);
    }

    /**
     * A generator node boolean flag by default initialize to true.
     */
    @SuppressWarnings("unused")
    @Specialization(order = 1, rewriteOn = {FrameSlotTypeException.class})
    public boolean doBoolean(VirtualFrame frame) throws FrameSlotTypeException {
        MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
        return getBoolean(mframe);
    }

    @SuppressWarnings("unused")
    @Specialization(order = 2, rewriteOn = {FrameSlotTypeException.class})
    public int doInteger(VirtualFrame frame) throws FrameSlotTypeException {
        MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
        return getInteger(mframe);
    }

    @Specialization
    public Object doObject(VirtualFrame frame) {
        MaterializedFrame mframe = PArguments.getGeneratorArguments(frame).getGeneratorFrame();
        return getObject(mframe);
    }

}
