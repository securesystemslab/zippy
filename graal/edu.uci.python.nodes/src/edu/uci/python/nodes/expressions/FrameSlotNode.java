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

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.FrameUtil;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;

/**
 * Followed the same logic from com.oracle.truffle.sl.nodes.FrameSlotNode.
 * 
 * @author zwei
 * 
 */
public abstract class FrameSlotNode extends PNode {

    protected final FrameSlot slot;

    public FrameSlotNode(FrameSlot slot) {
        this.slot = slot;
    }

    public final FrameSlot getSlot() {
        return slot;
    }

    protected final void setInteger(Frame frame, int value) throws FrameSlotTypeException {
        frame.setInt(slot, value);
    }

    protected final void setBoolean(Frame frame, boolean value) throws FrameSlotTypeException {
        frame.setBoolean(slot, value);
    }

    /**
     * Promoting int slot to double slot. Promotion from BigInteger(Object) to double is not
     * included yet.
     */
    protected final void setDouble(Frame frame, double value) throws FrameSlotTypeException {
        try {
            frame.setDouble(slot, value);
        } catch (FrameSlotTypeException e) {
            if (slot.getKind() == FrameSlotKind.Int) {
                FrameUtil.setDoubleSafe(frame, slot, value);
            } else {
                throw e;
            }
        }
    }

    /**
     * Promoting int slot to BigInteger(Object) slot.
     */
    protected final void setBigInteger(Frame frame, BigInteger value) throws FrameSlotTypeException {
        try {
            frame.setObject(slot, value);
        } catch (FrameSlotTypeException e) {
            if (slot.getKind() == FrameSlotKind.Int) {
                FrameUtil.setObjectSafe(frame, slot, value);
            } else {
                throw e;
            }
        }
    }

    protected final void setObject(Frame frame, Object value) {
        try {
            frame.setObject(slot, value);
        } catch (FrameSlotTypeException e) {
            FrameUtil.setObjectSafe(frame, slot, value);
        }
    }

    protected final int getInteger(Frame frame) throws FrameSlotTypeException {
        return frame.getInt(slot);
    }

    protected final boolean getBoolean(Frame frame) throws FrameSlotTypeException {
        return frame.getBoolean(slot);
    }

    protected final double getDouble(Frame frame) throws FrameSlotTypeException {
        return frame.getDouble(slot);
    }

    /**
     * Specialization for reference types. This is only needed for read access since write accesses
     * of FrameSlot cannot be made different from {@link #setObject(Frame, Object) };
     */
    protected final BigInteger getBigInteger(Frame frame) throws FrameSlotTypeException {
        Object object = frame.getObject(slot);

        if (object instanceof BigInteger) {
            return (BigInteger) object;
        } else {
            throw new FrameSlotTypeException();
        }
    }

    protected final PComplex getPComplex(Frame frame) throws FrameSlotTypeException {
        Object object = frame.getObject(slot);

        if (object instanceof PComplex) {
            return (PComplex) object;
        } else {
            throw new FrameSlotTypeException();
        }
    }

    protected final String getString(Frame frame) throws FrameSlotTypeException {
        Object object = frame.getObject(slot);

        if (object instanceof String) {
            return (String) object;
        } else {
            throw new FrameSlotTypeException();
        }
    }

    protected final PSequence getPSequence(Frame frame) throws FrameSlotTypeException {
        Object object = frame.getObject(slot);

        if (object instanceof PSequence) {
            return (PSequence) object;
        } else {
            throw new FrameSlotTypeException();
        }
    }

    protected final Object getObject(Frame frame) {
        try {
            return frame.getObject(slot);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + slot + ")";
    }

}
