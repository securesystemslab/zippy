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

public class ObjectSequenceStorage extends BasicSequenceStorage {

    private Object[] array;

    public ObjectSequenceStorage(Object[] elements) {
        this.array = elements;
    }

    @Override
    public int length() {
        return array.length;
    }

    @Override
    public Object getItemInBound(int idx) {
        return array[idx];
    }

    @Override
    public void setItemInBound(int idx, Object value) {
        array[idx] = value;
    }

    @Override
    public void insertItemInBound(int idx, Object value) {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("hiding")
    @Override
    public Object getSliceInBound(int start, int stop, int step, int length) {
        Object[] newArray = new Object[length];

        if (step == 1) {
            System.arraycopy(array, start, newArray, 0, length);
            return new ObjectSequenceStorage(newArray);
        }

        for (int i = start, j = 0; j < length; i += step, j++) {
            newArray[j] = array[i];
        }

        return new ObjectSequenceStorage(newArray);
    }

    @Override
    public void setSliceInBound(int start, int stop, int step, SequenceStorage sequence) {
        if (stop > array.length) {
            increaseCapacity(stop);
        }

        for (int i = start, j = 0; i < stop; i += step, j++) {
            array[i] = sequence.getInternalArray()[j];
        }
    }

    @Override
    public void delItemInBound(int idx) {
        Object[] newArray = new Object[array.length - 1];
        System.arraycopy(array, 0, newArray, 0, idx);
        System.arraycopy(array, idx + 1, newArray, idx, array.length - idx - 1);
        array = newArray;
    }

    @Override
    public Object[] getInternalArray() {
        return array;
    }

    @Override
    public void increaseCapacity(int newSize) {
        assert newSize > array.length;
        Object[] newArray = new Object[newSize];
        System.arraycopy(array, 0, newArray, 0, array.length);
        array = newArray;
    }
}
