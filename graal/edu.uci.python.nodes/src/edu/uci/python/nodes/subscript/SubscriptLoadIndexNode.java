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

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

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

    @Specialization(order = 1, guards = "isIntStorage")
    public int doPListInt(PList primary, int idx) {
        final IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
        int index = SequenceUtil.normalizeIndex(idx, store.length());
        return store.getIntItemNormalized(index);
    }

    @Specialization(order = 2, guards = "isDoubleStorage")
    public double doPListDouble(PList primary, int idx) {
        final DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
        int index = SequenceUtil.normalizeIndex(idx, store.length());
        return store.getDoubleItemNormalized(index);
    }

    @Specialization(order = 3, guards = "isObjectStorage")
    public Object doPListObject(PList primary, int idx) {
        final ObjectSequenceStorage store = (ObjectSequenceStorage) primary.getStorage();
        int index = SequenceUtil.normalizeIndex(idx, store.length());
        return store.getItemNormalized(index);
    }

    @Specialization(order = 5)
    public Object doPList(PList list, int idx) {
        return list.getItem(idx);
    }

    @Specialization(order = 7)
    public Object doPTuple(PTuple tuple, int idx) {
        return tuple.getItem(idx);
    }

    @Specialization(order = 10)
    public Object doPRange(PRange primary, int idx) {
        return primary.getItem(idx);
    }

    /**
     * PDict lookup using key.
     */
    @Specialization(order = 11)
    public Object doPDict(PDict primary, Object key) {
        final Object result = primary.getItem(key);
        assert result != null;
        return result;
    }

    /**
     * Unboxed array reads.
     */
    @Specialization(order = 12)
    public int doPIntArray(PIntArray primary, int idx) {
        final int index = SequenceUtil.normalizeIndex(idx, primary.len());
        return primary.getIntItemNormalized(index);
    }

    @Specialization(order = 13)
    public double doPDoubleArray(PDoubleArray primary, int idx) {
        final int index = SequenceUtil.normalizeIndex(idx, primary.len());
        return primary.getDoubleItemNormalized(index);
    }

    @Specialization(order = 14)
    public char doPCharArray(PCharArray primary, int idx) {
        final int index = SequenceUtil.normalizeIndex(idx, primary.len());
        return primary.getCharItemNormalized(index);
    }

    @Specialization(order = 15)
    public Object doPArray(PArray primary, int idx) {
        final int index = SequenceUtil.normalizeIndex(idx, primary.len());
        return primary.getItem(index);
    }

    /**
     * zwei: PythonTypesUtil does not unbox PyList. Instead we perform inplace update on PyList.
     * This avoid unwated data strcture duplication and actually updates a PyList imported from
     * Jython. As soon as we never have to actually read an imported PyList, this should be gone.
     */
    @Specialization(order = 19)
    public Object doPyList(PyObject primary, int index) {
        CompilerAsserts.neverPartOfCompilation();

        PyList list = (PyList) primary;
        Object value = list.get(index);

        if (value instanceof PyObject) {
            return PythonTypesUtil.unboxPyObject((PyObject) value);
        }

        return value;
    }

    @Specialization(order = 20)
    public Object doSpecialInt(VirtualFrame frame, PythonObject primary, int index) {
        return doSpecialMethodCall(frame, "__getitem__", primary, index);
    }

    @Specialization(order = 21)
    public Object doSpecialObject(VirtualFrame frame, PythonObject primary, Object index) {
        return doSpecialMethodCall(frame, "__getitem__", primary, index);
    }

}
