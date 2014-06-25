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

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

public abstract class SubscriptStoreIndexNode extends SubscriptStoreNode {

    @Override
    public PNode makeReadNode() {
        return SubscriptLoadIndexNodeFactory.create(getPrimary(), getSlice());
    }

    @Specialization(order = 1, guards = "isIntStorage")
    public Object doPListInt(PList primary, int idx, int value) {
        final IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
        store.setIntItemBoundCheck(idx, value);
        return PNone.NONE;
    }

    @Specialization(order = 2, guards = "isDoubleStorage")
    public Object doPListDouble(PList primary, int idx, double value) {
        final DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
        store.setDoubleItemInBound(idx, value);
        return PNone.NONE;
    }

    @Specialization(order = 3)
    public Object doPListObject(PList list, int index, Object value) {
        list.setItem(index, value);
        return PNone.NONE;
    }

    /**
     * PDict key & value store.
     */
    @Specialization(order = 6)
    public Object doPDict(PDict primary, Object key, Object value) {
        primary.setItem(key, value);
        return PNone.NONE;
    }

    /**
     * Unboxed array stores.
     */
    @Specialization(order = 10)
    public Object doPArrayInt(PIntArray primary, int idx, int value) {
        primary.setIntItemBoundCheck(idx, value);
        return PNone.NONE;
    }

    @Specialization(order = 11)
    public double doPArrayDouble(PDoubleArray primary, int idx, double value) {
        primary.setDoubleItemBoundCheck(idx, value);
        return 0;
    }

    @Specialization(order = 12)
    public char doPArrayChar(PCharArray primary, int idx, char value) {
        primary.setCharItemBoundCheck(idx, value);
        return 0;
    }

    @Specialization(order = 17)
    public Object doPArrayChar(PArray primary, int index, char value) {
        primary.setItem(index, value);
        return PNone.NONE;
    }

}
