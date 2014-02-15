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
package edu.uci.python.runtime.sequence.storage;

public final class ImmutableIntSequenceStorage extends ImmutableSequenceStorage {

    private final int[] values;

    public ImmutableIntSequenceStorage(int[] values) {
        this.values = values;
    }

    @Override
    public int length() {
        return values.length;
    }

    @Override
    public SequenceStorage copy() {
        return this;
    }

    @Override
    public Object[] getInternalArray() {
        /**
         * Have to box and copy.
         */
        Object[] boxed = new Object[values.length];

        for (int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }

        return boxed;
    }

    @Override
    public Object[] getCopyOfInternalArray() {
        return getInternalArray();
    }

    @Override
    public Object getItemInBound(int idx) {
        return values[idx];
    }

    // See IntSequenceStorage#getSliceInBound
    @Override
    public SequenceStorage getSliceInBound(int start, int stop, int step, int sliceLength) {
        int[] newArray = new int[sliceLength];

        if (step == 1) {
            System.arraycopy(values, start, newArray, 0, sliceLength);
            return new IntSequenceStorage(newArray);
        }

        for (int i = start, j = 0; j < sliceLength; i += step, j++) {
            newArray[j] = values[i];
        }

        return new IntSequenceStorage(newArray);
    }

    @Override
    public int index(Object value) {
        if (value instanceof Integer) {
            return indexOfInt((int) value);
        }

        throw new IllegalStateException();
    }

    public int indexOfInt(int value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public SequenceStorage generalizeFor(Object value) {
        return new ImmutableObjectSequenceStorage(getInternalArray());
    }

    @Override
    public Object getIndicativeValue() {
        return 0;
    }

}
