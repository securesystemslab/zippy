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
package edu.uci.python.nodes.subscript;

import com.oracle.truffle.api.dsl.Specialization;

import edu.uci.python.nodes.expression.*;
import edu.uci.python.runtime.datatype.*;
import static edu.uci.python.runtime.sequence.SequenceUtil.*;

public abstract class SliceNode extends TernaryOpNode {

    @Specialization(order = 0)
    public PSlice doPSlice(int start, int stop, int step) {
        return new PSlice(start, stop, step);
    }

    @SuppressWarnings("unused")
    @Specialization(order = 1)
    public PSlice doSlice(PNone start, int stop, int step) {
        return new PSlice(MISSING_INDEX, stop, step);
    }

    @SuppressWarnings("unused")
    @Specialization(order = 2)
    public PSlice doPSlice(int start, int stop, PNone step) {
        return new PSlice(start, stop, 1);
    }

    @SuppressWarnings("unused")
    @Specialization(order = 3)
    public PSlice doSlice(int start, PNone stop, PNone step) {
        return new PSlice.PStartSlice(start);
    }

    @SuppressWarnings("unused")
    @Specialization(order = 4)
    public PSlice doSlice(int start, PNone stop, int step) {
        return new PSlice(start, MISSING_INDEX, step);
    }

    @SuppressWarnings("unused")
    @Specialization(order = 5)
    public PSlice doSlice(PNone start, int stop, PNone step) {
        return new PSlice.PStopSlice(stop);
    }

    @SuppressWarnings("unused")
    @Specialization(order = 6)
    public PSlice doSlice(PNone start, PNone stop, int step) {
        return new PSlice(MISSING_INDEX, MISSING_INDEX, step);
    }

    @SuppressWarnings("unused")
    @Specialization(order = 7)
    public PSlice doSlice(PNone start, PNone stop, PNone step) {
        return new PSlice(MISSING_INDEX, MISSING_INDEX, 1);
    }

}
