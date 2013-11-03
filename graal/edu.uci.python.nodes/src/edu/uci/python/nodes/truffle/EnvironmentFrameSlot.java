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
package edu.uci.python.nodes.truffle;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;

public class EnvironmentFrameSlot extends FrameSlot {

    private final FrameSlot slot;

    private final int level;

    public EnvironmentFrameSlot(FrameSlot slot, int level) {
        super(slot.getFrameDescriptor(), slot.getIdentifier(), slot.getIndex(), slot.getKind());
        this.slot = slot;
        this.level = level;
    }

    public static EnvironmentFrameSlot pack(FrameSlot slot, int level) {
        return new EnvironmentFrameSlot(slot, level);
    }

    public FrameSlot unpack() {
        return slot;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public Object getIdentifier() {
        return slot.getIdentifier();
    }

    @Override
    public int getIndex() {
        return slot.getIndex();
    }

    @Override
    public FrameSlotKind getKind() {
        return slot.getKind();
    }

    @Override
    public void setKind(FrameSlotKind kind) {
        slot.setKind(kind);
    }

    @Override
    public String toString() {
        return slot.toString();
    }

    @Override
    public FrameDescriptor getFrameDescriptor() {
        return slot.getFrameDescriptor();
    }
}
