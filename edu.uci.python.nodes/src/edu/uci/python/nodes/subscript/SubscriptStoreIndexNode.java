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
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

@NodeInfo(shortName = "subscript_store_index")
@GenerateNodeFactory
public abstract class SubscriptStoreIndexNode extends SubscriptStoreNode {

    @Override
    public PNode makeReadNode() {
        return SubscriptLoadIndexNodeFactory.create(getPrimary(), getSlice());
    }

    @Specialization(order = 1, guards = {"isIntStorage(primary)", "isIndexPositive(primary,idx)"})
    public Object doPListInt(PList primary, int idx, int value) {
        final IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
        store.setIntItemNormalized(idx, value);
        return PNone.NONE;
    }

    @Specialization(order = 2, guards = {"isIntStorage(primary)", "isIndexNegative(primary,idx)"})
    public Object doPListIntNegative(PList primary, int idx, int value) {
        final IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
        store.setIntItemNormalized(idx + store.length(), value);
        return PNone.NONE;
    }

    @Specialization(order = 3, guards = {"isDoubleStorage(primary)", "isIndexPositive(primary,idx)"})
    public Object doPListDouble(PList primary, int idx, double value) {
        final DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
        store.setDoubleItemNormalized(idx, value);
        return PNone.NONE;
    }

    @Specialization(order = 4, guards = {"isDoubleStorage(primary)", "isIndexNegative(primary,idx)"})
    public Object doPListDoubleNegative(PList primary, int idx, double value) {
        final DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
        store.setDoubleItemNormalized(idx + store.length(), value);
        return PNone.NONE;
    }

    @Specialization(order = 5, guards = {"isObjectStorage(primary)", "isIndexPositive(primary,idx)"})
    public Object doPListObject(PList primary, int idx, Object value) {
        final ObjectSequenceStorage store = (ObjectSequenceStorage) primary.getStorage();
        store.setItemNormalized(idx, value);
        return PNone.NONE;
    }

    @Specialization(order = 6, guards = {"isObjectStorage(primary)", "isIndexNegative(primary,idx)"})
    public Object doPListObjectNegative(PList primary, int idx, Object value) {
        final ObjectSequenceStorage store = (ObjectSequenceStorage) primary.getStorage();
        store.setItemNormalized(idx + store.length(), value);
        return PNone.NONE;
    }

    @Specialization(order = 14)
    public Object doPList(PList list, int idx, Object value) {
        list.setItem(idx, value);
        return PNone.NONE;
    }

    /**
     * PDict key & value store.
     */
    @Specialization(order = 15)
    public Object doPDict(PDict primary, Object key, Object value) {
        primary.setItem(key, value);
        return PNone.NONE;
    }

    /**
     * Unboxed array stores.
     */
    @Specialization(order = 20, guards = "isIndexPositive(primary,index)")
    public Object doPArrayInt(PIntArray primary, int index, int value) {
        primary.setIntItemNormalized(index, value);
        return PNone.NONE;
    }

    @Specialization(order = 21, guards = "isIndexNegative(primary,index)")
    public Object doPArrayIntNegative(PIntArray primary, int index, int value) {
        primary.setIntItemNormalized(index + primary.len(), value);
        return PNone.NONE;
    }

    @Specialization(order = 22, guards = "isIndexPositive(primary,index)")
    public double doPArrayDouble(PDoubleArray primary, int index, double value) {
        primary.setDoubleItemNormalized(index, value);
        return 0;
    }

    @Specialization(order = 23, guards = "isIndexNegative(primary,index)")
    public double doPArrayDoubleNegative(PDoubleArray primary, int index, double value) {
        primary.setDoubleItemNormalized(index + primary.len(), value);
        return 0;
    }

    @Specialization(order = 24, guards = "isIndexPositive(primary,index)")
    public char doPArrayChar(PCharArray primary, int index, char value) {
        primary.setCharItemNormalized(index, value);
        return 0;
    }

    @Specialization(order = 25, guards = "isIndexNegative(primary,index)")
    public char doPArrayCharNegative(PCharArray primary, int index, char value) {
        primary.setCharItemNormalized(index + primary.len(), value);
        return 0;
    }

}
