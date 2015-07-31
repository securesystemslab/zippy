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

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.storage.*;

public final class PBytes extends PImmutableSequence {

    private final byte[] bytes;

    public PBytes() {
        bytes = new byte[0];
    }

    public PBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int len() {
        return bytes.length;
    }

    public Object getMax() {
        byte[] copy = Arrays.copyOf(bytes, bytes.length);
        Arrays.sort(copy);
        return copy[0];
    }

    public Object getMin() {
        byte[] copy = Arrays.copyOf(bytes, bytes.length);
        Arrays.sort(copy);
        return copy[bytes.length - 1];
    }

    @Override
    public Object getItem(int idx) {
        int checkedIdx = idx;

        if (idx < 0) {
            checkedIdx += bytes.length;
        }

        return bytes[checkedIdx];
    }

    @Override
    public Object getSlice(PSlice slice) {
        int length = slice.computeActualIndices(bytes.length);
        return getSlice(slice.getStart(), slice.getStop(), slice.getStep(), length);
    }

    @Override
    public Object getSlice(int start, int stop, int step, int length) {
        byte[] newBytes = new byte[length];
        if (step == 1) {
            System.arraycopy(bytes, start, newBytes, 0, stop - start);
            return new PBytes(newBytes);
        }
        for (int i = start, j = 0; j < length; i += step, j++) {
            newBytes[j] = bytes[i];
        }
        return new PBytes(newBytes);
    }

    @Override
    public int index(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SequenceStorage getStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean lessThan(PSequence sequence) {
        throw new UnsupportedOperationException();
    }

}
