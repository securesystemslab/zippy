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

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.storage.*;

public final class PTuple extends PImmutableSequence {

    private final Object[] array;

    public PTuple() {
        array = new Object[0];
    }

    public PTuple(Object[] elements) {
        if (elements == null) {
            array = new Object[0];
        } else {
            array = new Object[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        }
    }

    public PTuple(PRangeIterator iter) {
        /**
         * TODO Can be improved Currently creates a list, and then creates an array
         */
        List<Object> list = new ArrayList<>();

        final int start = iter.getStart();
        final int stop = iter.getStop();
        final int step = iter.getStep();

        for (int i = start; i < stop; i += step) {
            list.add(i);
        }

        array = list.toArray();
    }

    public PTuple(PIterator iter) {
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

        array = list.toArray();
    }

    /**
     * Note: This constructor assumes that <code>elements</code> is not null.
     * 
     * @param elements the tuple elements
     * @param copy whether to copy the elements into a new array or not
     */
    public PTuple(Object[] elements, boolean copy) {
        if (copy) {
            array = new Object[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        } else {
            array = elements;
        }
    }

    public Object[] getArray() {
        return array;
    }

    @Override
    public int len() {
        return array.length;
    }

    @Override
    public Object getItem(int idx) {
        int checkedIdx = idx;

        if (idx < 0) {
            checkedIdx += array.length;
        }

        return array[checkedIdx];
    }

    @Override
    public Object getSlice(PSlice slice) {
        int length = slice.computeActualIndices(array.length);
        return getSlice(slice.getStart(), slice.getStop(), slice.getStep(), length);
    }

    @Override
    public Object getSlice(int start, int stop, int step, int length) {
        Object[] newArray = new Object[length];
        if (step == 1) {
            System.arraycopy(array, start, newArray, 0, stop - start);
            return new PTuple(newArray, false);
        }
        for (int i = start, j = 0; j < length; i += step, j++) {
            newArray[j] = array[i];
        }
        return new PTuple(newArray, false);
    }

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

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("(");
        for (int i = 0; i < array.length - 1; i++) {
            buf.append(getStringForElementAt(i));
            buf.append(", ");
        }

        if (array.length > 0) {
            buf.append(getStringForElementAt(array.length - 1));
        }

        if (array.length == 1) {
            buf.append(",");
        }

        buf.append(")");
        return buf.toString();
    }

    private String getStringForElementAt(int index) {
        Object element = array[index];
        if (element instanceof String) {
            return "'" + element + "'";
        } else {
            return element.toString();
        }
    }

    @Override
    public Object getMin() {
        Object[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[0];
    }

    @Override
    public Object getMax() {
        Object[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }

    public PTuple __add__(PTuple tuple) {
        Object[] newArray = new Object[len() + tuple.len()];
        Object[] rightArray = tuple.getArray();

        System.arraycopy(getArray(), 0, newArray, 0, len());
        System.arraycopy(rightArray, 0, newArray, len(), rightArray.length);
        return new PTuple(newArray);
    }

    @SuppressWarnings({"unused", "static-method"})
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
}
