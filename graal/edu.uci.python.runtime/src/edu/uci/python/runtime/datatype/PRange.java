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
package edu.uci.python.runtime.datatype;

import org.python.core.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

public final class PRange extends PImmutableSequence {

    private final int start;
    private final int stop;
    private final int step;
    private final int length;

    public PRange(int hi) {
        this(0, hi, 1);
    }

    public PRange(int low, int hi) {
        this(low, hi, 1);
    }

    public PRange(int low, int hi, int step) {
        if (step == 0) {
            CompilerDirectives.transferToInterpreter();
            throw Py.ValueError("range() arg 3 must not be zero");
        }

        int n;
        if (step > 0) {
            n = getLenOfRange(low, hi, step);
        } else {
            n = getLenOfRange(hi, low, -step);
        }

        this.start = low;
        this.stop = hi;
        this.step = step;
        this.length = n;
    }

    public static int getLenOfRange(int lo, int hi, int step) {
        int n = 0;
        if (lo < hi) {
            // the base difference may be > Integer.MAX_VALUE
            long diff = (long) hi - (long) lo - 1;
            // any long > Integer.MAX_VALUE or < Integer.MIN_VALUE gets casted
            // to a
            // negative number
            n = (int) ((diff / step) + 1);
            if (n < 0) {
                CompilerDirectives.transferToInterpreter();
                throw Py.OverflowError("range() result has too many items");
            }
        }
        return n;
    }

    public int getStart() {
        return start;
    }

    public int getStep() {
        return step;
    }

    public int getStop() {
        return stop;
    }

    @Override
    public PRangeIterator __iter__() {
        return new PRangeIterator(this);
    }

    @Override
    public Object getItem(int idx) {
        int index = SequenceUtil.normalizeIndex(idx, length);

        if (index > length - 1) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw Py.IndexError("range object index out of range");
        }

        return index * step + start;
    }

    @SuppressWarnings("hiding")
    @Override
    public Object getSlice(int start, int stop, int step, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSlice(PSlice slice) {
        if (step != slice.getStep()) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new RuntimeException();
        }

        final int newStart = Math.max(start, slice.getStart());
        final int newStop = slice.getStop() == SequenceUtil.MISSING_INDEX ? stop : Math.min(stop, slice.getStop());
        return new PRange(newStart, newStop, step);
    }

    @Override
    public boolean lessThan(PSequence sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getMax() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getMin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int len() {
        return length;
    }

    @Override
    public SequenceStorage getStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int index(Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * See {@link PyXRange}.
     */
    @Override
    public String toString() {
        if (step == 1) {
            return String.format("range(%d, %d)", start, stop);
        } else {
            return String.format("range(%d, %d, %d)", start, stop, step);
        }

    }

}
