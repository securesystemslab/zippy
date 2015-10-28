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
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

@NodeInfo(shortName = "subscript_load_index")
@GenerateNodeFactory
public abstract class SubscriptLoadIndexNode extends SubscriptLoadNode {

    public PNode makeWriteNode(PNode rhs) {
        return SubscriptStoreIndexNodeFactory.create(getPrimary(), getSlice(), rhs);
    }

    @Specialization
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

    @Specialization(guards = {"isIntStorage(primary)", "isIndexPositive(primary,idx)"})
    public int doPListInt(PList primary, int idx) {
        final IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
        return store.getIntItemNormalized(idx);
    }

    @Specialization(guards = {"isIntStorage(primary)", "isIndexNegative(primary,idx)"})
    public int doPListIntNegative(PList primary, int idx) {
        final IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
        return store.getIntItemNormalized(idx + store.length());
    }

    @Specialization(guards = {"isDoubleStorage(primary)", "isIndexPositive(primary,idx)"})
    public double doPListDouble(PList primary, int idx) {
        final DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
        return store.getDoubleItemNormalized(idx);
    }

    @Specialization(guards = {"isDoubleStorage(primary)", "isIndexNegative(primary,idx)"})
    public double doPListDoubleNegative(PList primary, int idx) {
        final DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
        return store.getDoubleItemNormalized(idx + store.length());
    }

    @Specialization(guards = {"isObjectStorage(primary)", "isIndexPositive(primary,idx)"})
    public Object doPListObject(PList primary, int idx) {
        final ObjectSequenceStorage store = (ObjectSequenceStorage) primary.getStorage();
        return store.getItemNormalized(idx);
    }

    @Specialization(guards = {"isObjectStorage(primary)", "isIndexNegative(primary,idx)"})
    public Object doPListObjectNegative(PList primary, int idx) {
        final ObjectSequenceStorage store = (ObjectSequenceStorage) primary.getStorage();
        return store.getItemNormalized(idx + store.length());
    }

    @Specialization
    public Object doPList(PList list, int idx) {
        return list.getItem(idx);
    }

    @Specialization(guards = "isIndexPositive(tuple,idx)")
    public Object doPTuplePositive(PTuple tuple, int idx) {
        return tuple.getItemNormalized(idx);
    }

    @Specialization(guards = "isIndexNegative(tuple,idx)")
    public Object doPTupleNegative(PTuple tuple, int idx) {
        return tuple.getItemNormalized(idx + tuple.len());
    }

    @Specialization
    public Object doPTuple(PTuple tuple, int idx) {
        return tuple.getItem(idx);
    }

    @Specialization(guards = "isIndexPositive(primary,idx)")
    public Object doPRangePositive(PRange primary, int idx) {
        return primary.getItemNormalized(idx);
    }

    @Specialization(guards = "isIndexNegative(primary,idx)")
    public Object doPRangeNegative(PRange primary, int idx) {
        return primary.getItemNormalized(idx + primary.len());
    }

    @Specialization
    public Object doPRange(PRange primary, int idx) {
        return primary.getItem(idx);
    }

    /**
     * PDict lookup using key.
     */
    @Specialization
    public Object doPDict(PDict primary, Object key) {
        final Object result = primary.getItem(key);
        assert result != null;
        return result;
    }

    /**
     * Unboxed array reads.
     */
    @Specialization(guards = "isIndexPositive(primary,idx)")
    public int doPIntArray(PIntArray primary, int idx) {
        return primary.getIntItemNormalized(idx);
    }

    @Specialization(guards = "isIndexNegative(primary,idx)")
    public int doPIntArrayNegative(PIntArray primary, int idx) {
        return primary.getIntItemNormalized(idx + primary.len());
    }

    @Specialization(guards = "isIndexPositive(primary,idx)")
    public double doPDoubleArray(PDoubleArray primary, int idx) {
        return primary.getDoubleItemNormalized(idx);
    }

    @Specialization(guards = "isIndexNegative(primary,idx)")
    public double doPDoubleArrayNegative(PDoubleArray primary, int idx) {
        return primary.getDoubleItemNormalized(idx + primary.len());
    }

    @Specialization(guards = "isIndexPositive(primary,idx)")
    public char doPCharArray(PCharArray primary, int idx) {
        return primary.getCharItemNormalized(idx);
    }

    @Specialization(guards = "isIndexNegative(primary,idx)")
    public char doPCharArrayNegative(PCharArray primary, int idx) {
        return primary.getCharItemNormalized(idx + primary.len());
    }

    @Specialization
    public Object doPArray(PArray primary, int idx) {
        return primary.getItem(idx);
    }

    /**
     * zwei: PythonTypesUtil does not unbox PyList. Instead we perform inplace update on PyList.
     * This avoid unwated data strcture duplication and actually updates a PyList imported from
     * Jython. As soon as we never have to actually read an imported PyList, this should be gone.
     */
    @Specialization
    public Object doPyList(PyObject primary, int index) {
        CompilerAsserts.neverPartOfCompilation();

        PyList list = (PyList) primary;
        Object value = list.get(index);

        if (value instanceof PyObject) {
            return PythonTypesUtil.unboxPyObject((PyObject) value);
        }

        return value;
    }

    @Specialization
    public Object doSpecialInt(VirtualFrame frame, PythonObject primary, int index) {
        return doSpecialMethodCall(frame, "__getitem__", primary, index);
    }

    @Specialization
    public Object doSpecialObject(VirtualFrame frame, PythonObject primary, Object index) {
        return doSpecialMethodCall(frame, "__getitem__", primary, index);
    }

}
