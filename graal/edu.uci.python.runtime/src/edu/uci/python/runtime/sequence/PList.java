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
package edu.uci.python.runtime.sequence;

import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.storage.*;

public class PList extends PSequence {

    private static final PythonBuiltinClass __class__ = PythonContext.getBuiltinTypeFor(PList.class);

    @CompilationFinal private SequenceStorage store;

    public PList() {
        store = SequenceStorageFactory.createStorage(null);
    }

    public PList(SequenceStorage store) {
        this.store = store;
    }

    public PList(PIterator iter) {
        store = SequenceStorageFactory.createStorage(null);

        try {
            while (true) {
                append(iter.__next__());
            }
        } catch (StopIterationException e) {
            // fall through
        }
    }

    @Override
    public PythonBuiltinClass __class__() {
        return __class__;
    }

    @Override
    public PythonCallable __getattribute__(String name) {
        return (PythonCallable) __class__.getAttribute(name);
    }

    @Override
    public PIterator __iter__() {
        if (PythonOptions.UnboxSequenceIteration) {
            if (store instanceof IntSequenceStorage) {
                return new PIntegerSequenceIterator((IntSequenceStorage) store);
            } else if (store instanceof DoubleSequenceStorage) {
                return new PDoubleSequenceIterator((DoubleSequenceStorage) store);
            }
        }

        return new PSequenceIterator(this);
    }

    @Override
    public SequenceStorage getStorage() {
        return store;
    }

    @Override
    public Object getItem(int idx) {
        int index = SequenceUtil.normalizeIndex(idx, store.length());
        return store.getItemInBound(index);
    }

    @Override
    public void setItem(int idx, Object value) {
        int index = SequenceUtil.normalizeIndex(idx, store.length());
        try {
            store.setItemInBound(index, value);
        } catch (SequenceStoreException e) {
            store = store.generalizeFor(value);

            try {
                store.setItemInBound(idx, value);
            } catch (SequenceStoreException ex) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public Object getSlice(PSlice slice) {
        int length = slice.computeActualIndices(store.length());
        return getSlice(slice.getStart(), slice.getStop(), slice.getStep(), length);
    }

    @Override
    public Object getSlice(int start, int stop, int step, int length) {
        return new PList(store.getSliceInBound(start, stop, step, length));
    }

    @Override
    public void setSlice(PSlice slice, PSequence value) {
        setSlice(slice.getStart(), slice.getStop(), slice.getStep(), value);
    }

    @Override
    public void setSlice(int start, int stop, int step, PSequence value) {
        final int normalizedStart = SequenceUtil.normalizeSliceStart(start, step, store.length());
        int normalizedStop = SequenceUtil.normalizeSliceStop(stop, step, store.length());

        if (normalizedStop < normalizedStart) {
            normalizedStop = normalizedStart;
        }

        try {
            store.setSliceInBound(normalizedStart, normalizedStop, step, value.getStorage());
        } catch (SequenceStoreException e) {
            store = store.generalizeFor(value.getStorage().getIndicativeValue());

            try {
                store.setSliceInBound(start, stop, step, value.getStorage());
            } catch (SequenceStoreException ex) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public void delItem(int idx) {
        store.delItemInBound(idx);
    }

    @Override
    public boolean lessThan(PSequence sequence) {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("[");

        for (int i = 0; i < store.length(); i++) {
            Object item = store.getItemInBound(i);

            if (item instanceof String) {
                buf.append("'" + item.toString() + "'");
            } else {
                buf.append(item.toString());
            }

            if (i < store.length() - 1) {
                buf.append(", ");
            }
        }

        buf.append("]");
        return buf.toString();
    }

    @Override
    public Object getMax() {
        Object[] copy = store.getCopyOfInternalArray();
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }

    @Override
    public Object getMin() {
        Object[] copy = store.getCopyOfInternalArray();
        Arrays.sort(copy);
        return copy[0];
    }

    public void sort() {
        store.sort();
    }

    @Override
    public int len() {
        return store.length();
    }

    public PList __mul__(int value) {
        assert value > 0;
        SequenceStorage newStore = store.copy();

        try {
            for (int i = 1; i < value; i++) {
                newStore.extend(store.copy());
            }
        } catch (SequenceStoreException e) {
            throw new RuntimeException();
        }

        return new PList(newStore);
    }

    public void reverse() {
        store.reverse();
    }

    public void append(Object value) {
        if (store instanceof EmptySequenceStorage) {
            store = store.generalizeFor(value);
        }

        try {
            store.append(value);
        } catch (SequenceStoreException e) {
            store = store.generalizeFor(value);

            try {
                store.append(value);
            } catch (SequenceStoreException e1) {
                throw new RuntimeException();
            }
        }
    }

    public void extend(PList appendee) {
        SequenceStorage other = appendee.getStorage();

        try {
            store.extend(other);
        } catch (SequenceStoreException e) {
            store = store.generalizeFor(other.getIndicativeValue());

            try {
                store.extend(other);
            } catch (SequenceStoreException e1) {
                throw new RuntimeException();
            }
        }
    }

    public PList __add__(PList other) {
        SequenceStorage otherStore = other.getStorage();
        SequenceStorage newStore = store.copy();

        try {
            newStore.extend(otherStore);
        } catch (SequenceStoreException e) {
            newStore = newStore.generalizeFor(otherStore.getIndicativeValue());

            try {
                newStore.extend(otherStore);
            } catch (SequenceStoreException e1) {
                throw new RuntimeException();
            }
        }

        return new PList(newStore);
    }

    @Override
    public int index(Object value) {
        int index = store.index(value);

        if (index != -1) {
            return index;
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw Py.ValueError(value + " is not in list");
        }
    }

    public void insert(int index, Object value) {
        try {
            store.insertItem(index, value);
        } catch (SequenceStoreException e) {
            store = store.generalizeFor(value);

            try {
                store.insertItem(index, value);
            } catch (SequenceStoreException e1) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PList)) {
            return false;
        }

        PList otherList = (PList) other;
        SequenceStorage otherStore = otherList.getStorage();

        if (store.length() != otherStore.length()) {
            return false;
        }

        for (int i = 0; i < store.length(); i++) {
            Object l = store.getItemInBound(i);
            Object r = otherStore.getItemInBound(i);
            boolean isTheSame = ArithmeticUtil.is(l, r);

            if (!isTheSame) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
