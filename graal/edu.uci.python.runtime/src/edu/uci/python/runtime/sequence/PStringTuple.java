/*
 * Copyright (c) 2014, Regents of the University of California
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
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.iterator.*;

public final class PStringTuple extends PTuple {

    private final String[] array;

    public PStringTuple(String[] elements) {
        if (elements == null) {
            array = new String[0];
        } else {
            array = new String[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        }
    }

    /**
     * Note: This constructor assumes that <code>elements</code> is not null.
     *
     * @param elements the tuple elements
     * @param copy whether to copy the elements into a new array or not
     */
    public PStringTuple(String[] elements, boolean copy) {
        if (copy) {
            array = new String[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        } else {
            array = elements;
        }
    }

    public String getStringItem(int idx) {
        int checkedIdx = idx;

        if (idx < 0) {
            checkedIdx += array.length;
        }

        return array[checkedIdx];
    }

    @Override
    public PIterator __iter__() {
        if (PythonOptions.UnboxSequenceIteration) {
            return new PStringTupleIterator(this);
        } else {
            return new PSequenceIterator(this);
        }
    }

    @Override
    public int len() {
        return array.length;
    }

    @Override
    public Object getMin() {
        String[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[0];
    }

    @Override
    public Object getMax() {
        String[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }

    @Override
    public Object[] getArray() {
        Object[] objectArray = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return objectArray;
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
        String[] newArray = new String[length];
        if (step == 1) {
            System.arraycopy(array, start, newArray, 0, stop - start);
            return new PStringTuple(newArray, false);
        }
        for (int i = start, j = 0; j < length; i += step, j++) {
            newArray[j] = array[i];
        }
        return new PStringTuple(newArray, false);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("(");
        for (int i = 0; i < array.length - 1; i++) {
            buf.append(toString(array[i]));
            buf.append(", ");
        }

        if (array.length > 0) {
            buf.append(toString(array[array.length - 1]));
        }

        if (array.length == 1) {
            buf.append(",");
        }

        buf.append(")");
        return buf.toString();
    }

}
