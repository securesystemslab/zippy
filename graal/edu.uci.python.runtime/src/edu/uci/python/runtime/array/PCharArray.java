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

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;

public class PCharArray extends PArray {

    private final char[] array;

    public PCharArray() {
        array = new char[0];
    }

    public PCharArray(char[] elements) {
        if (elements == null) {
            array = new char[0];
        } else {
            array = new char[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        }
    }

    /**
     * Note: This constructor assumes that <code>elements</code> is not null.
     *
     * @param elements the tuple elements
     * @param copy whether to copy the elements into a new array or not
     */
    private PCharArray(char[] elements, boolean copy) {
        if (copy) {
            array = new char[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        } else {
            array = elements;
        }
    }

    public char[] getSequence() {
        return array;
    }

    @Override
    public Object getItem(int idx) {
        return getCharItemInBound(idx);
    }

    public char getCharItemInBound(int idx) {
        return ObjectLayoutUtil.readCharArrayUnsafeAt(array, idx, null);
    }

    @Override
    public void setItem(int idx, Object value) {
        int index = SequenceUtil.normalizeIndex(idx, array.length);
        setCharItemInBound(index, (char) value);
    }

    public void setCharItemInBound(int idx, char value) {
        ObjectLayoutUtil.writeCharArrayUnsafeAt(array, idx, value, null);
    }

    @Override
    public PCharArray getSlice(PSlice slice) {
        int length = slice.computeActualIndices(array.length);
        return getSlice(slice.getStart(), slice.getStop(), slice.getStep(), length);
    }

    @Override
    public PCharArray getSlice(int start, int stop, int step, int length) {
        char[] newArray = new char[length];

        if (step == 1) {
            System.arraycopy(array, start, newArray, 0, stop - start);
            return new PCharArray(newArray, false);
        }
        for (int i = start, j = 0; j < length; i += step, j++) {
            newArray[j] = array[i];
        }
        return new PCharArray(newArray, false);
    }

    public void setItem(int idx, char value) {
        int index = SequenceUtil.normalizeIndex(idx, array.length);
        array[index] = value;
    }

    @Override
    public Object getMax() {
        char[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }

    @Override
    public Object getMin() {
        char[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[0];
    }

    @Override
    public int len() {
        return array.length;
    }

    @Override
    public PArray __add__(PArray other) {
        PCharArray otherArray = (PCharArray) other;
        char[] joined = new char[len() + other.len()];
        System.arraycopy(array, 0, joined, 0, len());
        System.arraycopy(otherArray.getSequence(), 0, joined, len(), other.len());
        return new PCharArray(joined);
    }

    @Override
    public PArray __mul__(int value) {
        char[] newArray = new char[value * array.length];
        int count = 0;
        for (int i = 0; i < value; i++) {
            for (int j = 0; j < array.length; j++) {
                newArray[count++] = array[j];
            }
        }

        return new PCharArray(newArray);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("(");
        for (int i = 0; i < array.length - 1; i++) {
            buf.append(array[i] + " ");
        }
        buf.append(array[array.length - 1]);
        buf.append(")");
        return buf.toString();
    }
}
