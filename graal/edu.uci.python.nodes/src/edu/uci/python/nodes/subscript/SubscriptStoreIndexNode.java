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
package edu.uci.python.nodes.subscript;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.statements.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.sequence.*;

@NodeChildren({@NodeChild(value = "primary", type = PNode.class), @NodeChild(value = "index", type = PNode.class), @NodeChild(value = "right", type = PNode.class)})
public abstract class SubscriptStoreIndexNode extends StatementNode implements WriteNode {

    public abstract PNode getPrimary();

    public abstract PNode getIndex();

    public abstract PNode getRight();

    @Override
    public PNode makeReadNode() {
        return SubscriptLoadIndexNodeFactory.create(getPrimary(), getIndex());
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

    @Specialization(order = 1)
    public Object doPSequence(PSequence primary, int index, Object value) {
        primary.setItem(index, value);
        return PNone.NONE;
    }

    /**
     * PDict key & value store.
     */
    @Specialization(order = 2)
    public Object doPDict(PDict primary, Object key, Object value) {
        primary.setItem(key, value);
        return PNone.NONE;
    }

    /**
     * Unboxed array stores.
     */
    @Specialization(order = 10)
    public Object doPArrayInt(PIntArray primary, int index, int value) {
        primary.setIntItem(index, value);
        return PNone.NONE;
    }

    @Specialization(order = 15)
    public Object doPArrayDouble(PArray primary, int index, double value) {
        primary.setItem(index, value);
        return PNone.NONE;
    }

    @Specialization(order = 17)
    public Object doPArrayChar(PArray primary, int index, char value) {
        primary.setItem(index, value);
        return PNone.NONE;
    }
}
