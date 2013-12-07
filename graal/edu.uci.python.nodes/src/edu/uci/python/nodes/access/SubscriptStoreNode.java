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

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.statements.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.sequence.*;

@NodeChildren({@NodeChild(value = "primary", type = PNode.class), @NodeChild(value = "slice", type = PNode.class), @NodeChild(value = "right", type = PNode.class)})
public abstract class SubscriptStoreNode extends StatementNode implements WriteNode {

    public abstract PNode getPrimary();

    public abstract PNode getSlice();

    public abstract PNode getRight();

    @Override
    public PNode makeReadNode() {
        return SubscriptLoadNodeFactory.create(getPrimary(), getSlice());
    }

    @Override
    public PNode getRhs() {
        return getRight();
    }

    @Override
    public Object executeWrite(VirtualFrame frame, Object value) {
        return executeWith(frame, value);
    }

    public abstract Object executeWith(VirtualFrame frame, Object value);

    /*
     * As a right hand side expression
     */
    @Specialization(order = 0)
    public Object doPDictionary(PDict primary, Object slice, Object value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 1)
    public Object doPSequence(PSequence primary, int slice, Object value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 2)
    public Object doPSequence(PSequence primary, PSlice slice, PSequence value) {
        primary.setSlice(slice, value);
        return null;
    }

    /**
     * Unboxed array stores.
     */
    @Specialization(order = 10)
    public Object doPArrayInt(PIntArray primary, int slice, int value) {
        primary.setIntItem(slice, value);
        return PNone.NONE;
    }

    @Specialization(order = 14)
    public Object doPArray(PArray primary, PSlice slice, PArray value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Specialization(order = 15)
    public Object doPArrayDouble(PArray primary, int slice, double value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 17)
    public Object doPArrayChar(PArray primary, int slice, char value) {
        primary.setItem(slice, value);
        return null;
    }

    @Generic
    public Object doGeneric(Object primary, Object slice, Object value) {
        if (primary instanceof PSequence) {
            PSequence prim = (PSequence) primary;
            if (slice instanceof Integer) {
                prim.setItem((int) slice, value);
            } else if (slice instanceof PSlice) {
                prim.setSlice((PSlice) slice, (PSequence) value);
            }
        } else if (primary instanceof PDict) {
            PDict prim = (PDict) primary;
            prim.setItem(slice, value);
        } else if (primary instanceof PArray) {
            PArray prim = (PArray) primary;
            prim.setItem((int) slice, value);
        } else {
            throw new RuntimeException("Unsupported Type!");
        }

        return PNone.NONE;
    }

}
