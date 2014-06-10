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
package edu.uci.python.runtime.sequence.storage;

import java.io.*;

import org.python.core.*;

import edu.uci.python.runtime.*;

public final class EmptySequenceStorage extends SequenceStorage {

    public static final EmptySequenceStorage INSTANCE = new EmptySequenceStorage();

    private EmptySequenceStorage() {
    }

    @Override
    public SequenceStorage generalizeFor(Object value) {
        final SequenceStorage generalized;

        if (value instanceof Integer) {
            generalized = new IntSequenceStorage();
        } else if (value instanceof Double) {
            generalized = new DoubleSequenceStorage();
        } else {
            generalized = new ObjectSequenceStorage();
        }

        if (PythonOptions.TraceSequenceStorageGeneralization) {
            PrintStream ps = System.out;
            ps.println("[ZipPy]" + this + " generalizing to " + generalized);
        }

        return generalized;
    }

    @Override
    public Object getIndicativeValue() {
        return null;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public int index(Object value) {
        throw Py.ValueError(value + " is not in list");
    }

    @Override
    public SequenceStorage copy() {
        return this;
    }

    @Override
    public Object[] getInternalArray() {
        return new Object[]{};
    }

    @Override
    public Object[] getCopyOfInternalArray() {
        return getInternalArray();
    }

    @Override
    public Object getItemInBound(int idx) {
        throw Py.ValueError("list index out of range");
    }

    @Override
    public void setItemInBound(int idx, Object value) throws SequenceStoreException {
        throw Py.ValueError("list assignment index out of range");
    }

    @Override
    public void insertItem(int idx, Object value) throws SequenceStoreException {
        assert idx == 0;
        throw SequenceStoreException.INSTANCE;
    }

    @Override
    public SequenceStorage getSliceInBound(int start, int stop, int step, int length) {
        assert start == stop && stop == 0;
        return this;
    }

    @Override
    public void setSliceInBound(int start, int stop, int step, SequenceStorage sequence) throws SequenceStoreException {
        throw SequenceStoreException.INSTANCE;
    }

    @Override
    public void delSlice(int start, int stop) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delItemInBound(int idx) {
        throw new UnsupportedOperationException("Cannot delete from empty storage");
    }

    @Override
    public Object popInBound(int idx) {
        return new UnsupportedOperationException();
    }

    @Override
    public void append(Object value) throws SequenceStoreException {
        throw SequenceStoreException.INSTANCE;
    }

    @Override
    public void extend(SequenceStorage other) throws SequenceStoreException {
        throw SequenceStoreException.INSTANCE;
    }

    @Override
    public void reverse() {
    }

    @Override
    public void sort() {
    }

    @Override
    public boolean equals(SequenceStorage other) {
        return other == EmptySequenceStorage.INSTANCE;
    }

}
