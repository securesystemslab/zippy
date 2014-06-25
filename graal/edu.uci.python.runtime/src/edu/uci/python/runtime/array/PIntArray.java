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
package edu.uci.python.runtime.array;

import java.util.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.*;

public final class PIntArray extends PArray {

    private final int[] array;

    public PIntArray() {
        array = new int[0];
    }

    public PIntArray(int[] elements) {
        if (elements == null) {
            array = new int[0];
        } else {
            array = new int[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        }
    }

    /**
     * Note: This constructor assumes that <code>elements</code> is not null.
     *
     * @param elements the tuple elements
     * @param copy whether to copy the elements into a new array or not
     */
    private PIntArray(int[] elements, boolean copy) {
        if (copy) {
            array = new int[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        } else {
            array = elements;
        }
    }

    public int[] getSequence() {
        return array;
    }

    @Override
    public PIterator __iter__() {
        if (PythonOptions.UnboxSequenceIteration) {
            return new PIntArrayIterator(this);
        } else {
            return new PSequenceIterator(this);
        }
    }

    @Override
    public Object getItem(int idx) {
        return getIntItemBoundCheck(idx);
    }

    public int getIntItemBoundCheck(int idx) {
        int index = SequenceUtil.normalizeIndex(idx, array.length);
        if (index < array.length) {
            return getIntItemInBound(index);
        } else {
            return SequenceUtil.throwIndexError("array index out of range");
        }
    }

    public int getIntItemInBound(int idx) {
        return array[idx];
    }

    @Override
    public void setItem(int idx, Object value) {
        setIntItemBoundCheck(idx, (int) value);
    }

    public void setIntItemBoundCheck(int idx, int value) {
        final int index = SequenceUtil.normalizeIndex(idx, array.length);
        if (index < array.length) {
            setIntItemInBound(index, value);
        } else {
            SequenceUtil.throwIndexError("array assignment index out of range");
        }
    }

    public void setIntItemInBound(int idx, int value) {
        array[idx] = value;
    }

    @Override
    public PIntArray getSlice(PSlice slice) {
        int length = slice.computeActualIndices(array.length);
        return getSlice(slice.getStart(), slice.getStop(), slice.getStep(), length);
    }

    @Override
    public PIntArray getSlice(int start, int stop, int step, int length) {
        int[] newArray = new int[length];

        if (step == 1) {
            System.arraycopy(array, start, newArray, 0, stop - start);
            return new PIntArray(newArray, false);
        }
        for (int i = start, j = 0; j < length; i += step, j++) {
            newArray[j] = array[i];
        }
        return new PIntArray(newArray, false);
    }

    @Override
    public Object getMax() {
        int[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }

    @Override
    public Object getMin() {
        int[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[0];
    }

    @Override
    public int len() {
        return array.length;
    }

    @Override
    public PArray __add__(PArray other) {
        PIntArray otherArray = (PIntArray) other;
        int[] joined = new int[len() + other.len()];
        System.arraycopy(array, 0, joined, 0, len());
        System.arraycopy(otherArray.getSequence(), 0, joined, len(), other.len());
        return new PIntArray(joined);
    }

    @Override
    public PArray __mul__(int value) {
        int[] newArray = new int[value * array.length];
        int count = 0;
        for (int i = 0; i < value; i++) {
            for (int j = 0; j < array.length; j++) {
                newArray[count++] = array[j];
            }
        }

        return new PIntArray(newArray);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("array('i', [");
        for (int i = 0; i < array.length - 1; i++) {
            buf.append(array[i] + ", ");
        }
        buf.append(array[array.length - 1]);
        buf.append("])");
        return buf.toString();
    }
}
