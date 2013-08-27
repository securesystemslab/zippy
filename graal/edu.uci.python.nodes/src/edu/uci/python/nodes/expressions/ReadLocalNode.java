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
package edu.uci.python.nodes.expressions;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;

public abstract class ReadLocalNode extends FrameSlotNode implements ReadNode {

    public ReadLocalNode(FrameSlot slot) {
        super(slot);
    }

    public ReadLocalNode(ReadLocalNode specialized) {
        this(specialized.slot);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return WriteLocalNodeFactory.create(slot, rhs);
    }

    @Specialization(order = 1, rewriteOn = {FrameSlotTypeException.class})
    public int doInteger(VirtualFrame frame) throws FrameSlotTypeException {
        return getInteger(frame);
    }

    @Specialization(order = 2, rewriteOn = {FrameSlotTypeException.class})
    public boolean doBoolean(VirtualFrame frame) throws FrameSlotTypeException {
        return getBoolean(frame);
    }

    @Specialization(order = 3, rewriteOn = {FrameSlotTypeException.class})
    public double doDouble(VirtualFrame frame) throws FrameSlotTypeException {
        return getDouble(frame);
    }

    @Specialization(order = 4, rewriteOn = {FrameSlotTypeException.class})
    public BigInteger doBigInteger(VirtualFrame frame) throws FrameSlotTypeException {
        return getBigInteger(frame);
    }

    @Specialization(order = 5, rewriteOn = {FrameSlotTypeException.class})
    public PComplex doComplex(VirtualFrame frame) throws FrameSlotTypeException {
        return getPComplex(frame);
    }

    @Specialization(order = 6, rewriteOn = {FrameSlotTypeException.class})
    public String doString(VirtualFrame frame) throws FrameSlotTypeException {
        return getString(frame);
    }

    @Specialization(order = 7, rewriteOn = {FrameSlotTypeException.class})
    public PSequence doPSequence(VirtualFrame frame) throws FrameSlotTypeException {
        return getPSequence(frame);
    }

    @Specialization
    public Object doObject(VirtualFrame frame) {
        return getObject(frame);
    }

}
