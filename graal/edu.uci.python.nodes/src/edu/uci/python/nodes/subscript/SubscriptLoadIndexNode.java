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

@NodeInfo(shortName = "subscript_load_index")
public abstract class SubscriptLoadIndexNode extends SubscriptLoadNode {

    public PNode makeWriteNode(PNode rhs) {
        return SubscriptStoreIndexNodeFactory.create(getPrimary(), getSlice(), rhs);
    }

    @Specialization(order = 0)
    public String doString(String primary, int idx) {
        int index = idx;

        if (idx < 0) {
            index += primary.length();
        }

        return charAtToString(primary, index);
    }

    private static String charAtToString(String primary, int index) {
        char charactor = primary.charAt(index);
        return new String(new char[]{charactor});
    }

    @Specialization(order = 1, guards = "isIntStore")
    public int doPListInt(PList primary, int idx) {
        final IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
        int index = SequenceUtil.normalizeIndex(idx, store.length());
        return store.getIntItemInBound(index);
    }

    @Specialization(order = 2, guards = "isDoubleStore")
    public double doPListDouble(PList primary, int idx) {
        final DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
        int index = SequenceUtil.normalizeIndex(idx, store.length());
        return store.getDoubleItemInBound(index);
    }

    @Specialization(order = 3)
    public Object doPListObject(PList list, int idx) {
        return list.getItem(idx);
    }

    @Specialization(order = 4)
    public int doPIntTuple(PIntTuple tuple, int idx) {
        return tuple.getIntItem(idx);
    }

    @Specialization(order = 5)
    public double doPDoubleTuple(PDoubleTuple tuple, int idx) {
        return tuple.getDoubleItem(idx);
    }

    @Specialization(order = 6)
    public String doPStringTuple(PStringTuple tuple, int idx) {
        return tuple.getStringItem(idx);
    }

    @Specialization(order = 7)
    public Object doPObjectTuple(PObjectTuple tuple, int idx) {
        return tuple.getObjectItem(idx);
    }

    @Specialization(order = 8)
    public Object doPRange(PRange primary, int idx) {
        return primary.getItem(idx);
    }

    /**
     * PDict lookup using key.
     */
    @Specialization(order = 9)
    public Object doPDict(PDict primary, Object key) {
        return primary.getItem(key);
    }

    /**
     * Unboxed array reads.
     */
    @Specialization(order = 10)
    public int doPIntArray(PIntArray primary, int index) {
        return primary.getIntItemInBound(index);
    }

    @Specialization(order = 11)
    public double doPDoubleArray(PDoubleArray primary, int index) {
        return primary.getDoubleItemInBound(index);
    }

    @Specialization(order = 12)
    public char doPCharArray(PCharArray primary, int index) {
        return primary.getCharItemInBound(index);
    }

    @Specialization(order = 14)
    public Object doPArray(PArray primary, int slice) {
        return primary.getItem(slice);
    }

}
