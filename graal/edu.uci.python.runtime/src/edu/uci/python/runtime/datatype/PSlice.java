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

import static edu.uci.python.runtime.sequence.SequenceUtil.*;

public class PSlice {

    private int start;
    private int stop;
    private final int step;

    public PSlice(int start, int stop, int step) {
        this.start = start;
        this.stop = stop;
        this.step = step;
    }

    public final int getStart() {
        return start;
    }

    public final int getStop() {
        return stop;
    }

    public final int getStep() {
        return step;
    }

    public final int computeActualIndices(int len) {
        int length;

        if (step == 0) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw Py.ValueError("slice step cannot be zero");
        }

        if (start == MISSING_INDEX) {
            start = step < 0 ? len - 1 : 0;
        } else {
            if (start < 0) {
                start += len;
            }
            if (start < 0) {
                start = step < 0 ? -1 : 0;
            }
            if (start >= len) {
                start = step < 0 ? len - 1 : len;
            }
        }

        if (stop == MISSING_INDEX) {
            stop = step < 0 ? -1 : len;
        } else {
            if (stop < 0) {
                stop += len;
            }
            if (stop < 0) {
                stop = -1;
            }
            if (stop > len) {
                stop = len;
            }
        }

        if (step > 0 && stop < start) {
            stop = start;
        }

        if (step > 0) {
            length = (stop - start + step - 1) / step;
        } else {
            length = (stop - start + step + 1) / step;
        }

        if (length < 0) {
            length = 0;
        }

        return length;
    }

    /**
     * Make step a long in case adding the start, stop and step together overflows an int.
     */
    public static final int sliceLength(int start, int stop, long step) {
        int ret;
        if (step > 0) {
            ret = (int) ((stop - start + step - 1) / step);
        } else {
            ret = (int) ((stop - start + step + 1) / step);
        }

        if (ret < 0) {
            return 0;
        }

        return ret;
    }

    /**
     * Stop is missing.
     */
    public static final class PStartSlice extends PSlice {

        public PStartSlice(int start) {
            super(start, MISSING_INDEX, 1);
        }
    }

    /**
     * Start is missing.
     */
    public static final class PStopSlice extends PSlice {

        public PStopSlice(int stop) {
            super(MISSING_INDEX, stop, 1);
        }
    }

}
