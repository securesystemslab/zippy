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

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;

@NodeInfo(shortName = "subscript_load_slice")
public abstract class SubscriptLoadSliceNode extends SubscriptLoadNode {

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return SubscriptStoreSliceNodeFactory.create(getPrimary(), getSlice(), rhs);
    }

    @ExplodeLoop
    @Specialization(order = 0)
    public String doString(String primary, PSlice slice) {
        final int length = slice.computeActualIndices(primary.length());
        final int start = slice.getStart();
        int stop = slice.getStop();
        int step = slice.getStep();

        if (step > 0 && stop < start) {
            stop = start;
        }
        if (step == 1) {
            return getSubString(primary, start, stop);
        } else {
            char[] newChars = new char[length];
            int j = 0;
            for (int i = start; j < length; i += step) {
                newChars[j++] = primary.charAt(i);
            }

            return new String(newChars);
        }
    }

    @Specialization(order = 4)
    public Object doPList(PList list, PSlice slice) {
        return list.getSlice(slice);
    }

    @Specialization(order = 5)
    public Object doPTuple(PTuple tuple, PSlice slice) {
        return tuple.getSlice(slice);
    }

    @Specialization(order = 6)
    public Object doPRange(PRange range, PSlice slice) {
        return range.getSlice(slice);
    }

    /**
     * Unboxed array reads.
     */
    @Specialization(order = 15)
    public Object doPArray(PArray primary, PSlice slice) {
        return primary.getSlice(slice);
    }

    @SuppressWarnings("unused")
    @Generic
    public Object doGeneric(Object primary, Object slice) {
        throw new RuntimeException("Unsupported primary Type " + primary.getClass().getSimpleName());
    }

    private static String getSubString(String origin, int start, int stop) {
        char[] chars = new char[stop - start];
        origin.getChars(start, stop, chars, 0);
        return new String(chars);
    }

}
