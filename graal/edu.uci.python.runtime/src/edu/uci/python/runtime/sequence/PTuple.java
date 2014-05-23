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

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.storage.*;

public abstract class PTuple extends PImmutableSequence implements Comparable<Object> {

    public static PTuple create() {
        return new PObjectTuple(new Object[0]);
    }

    public static PTuple create(Object[] elements) {
        if (PythonOptions.UnboxSequenceStorage) {
            if (SequenceStorageFactory.canSpecializeToInt(elements)) {
                return new PIntTuple(SequenceStorageFactory.specializeToInt(elements));
            } else if (SequenceStorageFactory.canSpecializeToDouble(elements)) {
                return new PDoubleTuple(SequenceStorageFactory.specializeToDouble(elements));
            } else {
                return new PObjectTuple(elements);
            }
        } else {
            return new PObjectTuple(elements);
        }
    }

    public static PTuple create(PIterator iter) {
        /**
         * TODO Can be improved Currently creates a list, and then creates an array
         */
        List<Object> list = new ArrayList<>();

        try {
            while (true) {
                list.add(iter.__next__());
            }
        } catch (StopIterationException e) {
            // fall through
        }

        Object[] values = list.toArray();
        if (PythonOptions.UnboxSequenceStorage) {
            if (SequenceStorageFactory.canSpecializeToInt(values)) {
                return new PIntTuple(SequenceStorageFactory.specializeToInt(values));
            } else if (SequenceStorageFactory.canSpecializeToDouble(values)) {
                return new PDoubleTuple(SequenceStorageFactory.specializeToDouble(values));
            } else {
                return new PObjectTuple(values);
            }
        } else {
            return new PObjectTuple(values);
        }
    }

    public static PTuple create(PRangeIterator iter) {
        return new PIntTuple(iter);
    }

    /**
     * Note: This constructor assumes that <code>elements</code> is not null.
     *
     * @param elements the tuple elements
     * @param copy whether to copy the elements into a new array or not
     */
    public static PTuple create(Object[] elements, boolean copy) {
        if (PythonOptions.UnboxSequenceStorage) {
            if (SequenceStorageFactory.canSpecializeToInt(elements)) {
                return new PIntTuple(SequenceStorageFactory.specializeToInt(elements), copy);
            } else if (SequenceStorageFactory.canSpecializeToDouble(elements)) {
                return new PDoubleTuple(SequenceStorageFactory.specializeToDouble(elements), copy);
            } else {
                return new PObjectTuple(elements, copy);
            }
        } else {
            return new PObjectTuple(elements, copy);
        }
    }

    public abstract Object[] getArray();

    @Override
    public boolean lessThan(PSequence sequence) {
        int i = SequenceUtil.cmp(this, sequence);
        if (i < 0) {
            return i == -1 ? true : false;
        }

        Object element1 = this.getItem(i);
        Object element2 = sequence.getItem(i);

        /**
         * TODO: Can use a better approach instead of instanceof checks
         */
        if (element1 instanceof Integer && element1 instanceof Integer) {
            int int1 = (int) element1;
            int int2 = (int) element2;
            if (int1 < int2) {
                return true;
            } else {
                return false;
            }
        }

        throw new UnsupportedOperationException();
    }

    public PTuple __add__(PTuple tuple) {
        Object[] newArray = new Object[len() + tuple.len()];
        Object[] rightArray = tuple.getArray();

        System.arraycopy(getArray(), 0, newArray, 0, len());
        System.arraycopy(rightArray, 0, newArray, len(), rightArray.length);
        return PTuple.create(newArray);
    }

    @SuppressWarnings({"unused"})
    public PTuple __mul__(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SequenceStorage getStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int index(Object value) {
        throw new UnsupportedOperationException();
    }

    public int compareTo(Object o) {
        return SequenceUtil.cmp(this, (PSequence) o);
    }

}
