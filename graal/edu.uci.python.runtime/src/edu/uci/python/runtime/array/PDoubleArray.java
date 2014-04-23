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

public class PDoubleArray extends PArray {

    private final double[] array;

    public PDoubleArray() {
        array = new double[0];
    }

    public PDoubleArray(double[] elements) {
        if (elements == null) {
            array = new double[0];
        } else {
            array = new double[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        }
    }

    /**
     * Note: This constructor assumes that <code>elements</code> is not null.
     *
     * @param elements the tuple elements
     * @param copy whether to copy the elements into a new array or not
     */
    private PDoubleArray(double[] elements, boolean copy) {
        if (copy) {
            array = new double[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        } else {
            array = elements;
        }
    }

    public double[] getSequence() {
        return array;
    }

    @Override
    public Object getItem(int idx) {
        return getDoubleItemInBound(idx);
    }

    public double getDoubleItemInBound(int idx) {
        return ObjectLayoutUtil.readDoubleArrayUnsafeAt(array, idx, this);
    }

    @Override
    public void setItem(int idx, Object value) {
        int index = SequenceUtil.normalizeIndex(idx, array.length);
        setDoubleItemInBound(index, (double) value);
    }

    public void setDoubleItemInBound(int idx, double value) {
        ObjectLayoutUtil.writeDoubleArrayUnsafeAt(array, idx, value, this);
    }

    @Override
    public PDoubleArray getSlice(PSlice slice) {
        int length = slice.computeActualIndices(array.length);
        return getSlice(slice.getStart(), slice.getStop(), slice.getStep(), length);
    }

    @Override
    public PDoubleArray getSlice(int start, int stop, int step, int length) {
        double[] newArray = new double[length];

        if (step == 1) {
            System.arraycopy(array, start, newArray, 0, stop - start);
            return new PDoubleArray(newArray, false);
        }
        for (int i = start, j = 0; j < length; i += step, j++) {
            newArray[j] = array[i];
        }
        return new PDoubleArray(newArray, false);
    }

    @Override
    public Object getMax() {
        double[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }

    @Override
    public Object getMin() {
        double[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[0];
    }

    @Override
    public int len() {
        return array.length;
    }

    @Override
    public PArray __add__(PArray other) {
        PDoubleArray otherArray = (PDoubleArray) other;
        double[] joined = new double[len() + other.len()];
        System.arraycopy(array, 0, joined, 0, len());
        System.arraycopy(otherArray.getSequence(), 0, joined, len(), other.len());
        return new PDoubleArray(joined);
    }

    @Override
    public PArray __mul__(int value) {
        double[] newArray = new double[value * array.length];
        int count = 0;

        for (int i = 0; i < value; i++) {
            for (int j = 0; j < array.length; j++) {
                newArray[count++] = array[j];
            }
        }

        return new PDoubleArray(newArray);
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
