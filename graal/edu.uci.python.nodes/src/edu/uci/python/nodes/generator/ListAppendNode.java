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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

/**
 * Implements LIST_APPEND bytecode in CPython.
 */
@NodeChild(value = "rightNode", type = PNode.class)
public abstract class ListAppendNode extends FrameSlotNode {

    public abstract PNode getRightNode();

    public ListAppendNode(FrameSlot frameSlot) {
        super(frameSlot);
    }

    protected ListAppendNode(ListAppendNode node) {
        this(node.frameSlot);
    }

    @Specialization
    public boolean doBoolean(VirtualFrame frame, boolean right) {
        getPList(frame).append(right);
        return right;
    }

    @Specialization
    public int doInteger(VirtualFrame frame, int right) {
        PList list = getPList(frame);
        SequenceStorage store = list.getStorage();

        if (store instanceof IntSequenceStorage) {
            ((IntSequenceStorage) store).appendInt(right);
        } else {
            list.append(right);
        }

        return right;
    }

    @Specialization
    public double doDouble(VirtualFrame frame, double right) {
        PList list = getPList(frame);
        SequenceStorage store = list.getStorage();

        if (store instanceof IntSequenceStorage) {
            ((DoubleSequenceStorage) store).appendDouble(right);
        } else {
            list.append(right);
        }

        return right;
    }

    @Specialization
    public Object doObject(VirtualFrame frame, Object right) {
        getPList(frame).append(right);
        return right;
    }

    protected final PList getPList(Frame frame) {
        return CompilerDirectives.unsafeCast(getObject(frame), PList.class, true);
    }

}
