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

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.Specialization;

import edu.uci.python.nodes.expression.*;
import edu.uci.python.runtime.datatype.*;

public abstract class SliceNode extends TernaryOpNode {

    @Specialization
    public PSlice doPSlice(int start, int stop, int step) {
        return new PSlice(start, stop, step);
    }

    @Specialization
    public PSlice doPSlice(BigInteger start, BigInteger stop, BigInteger step) {
        return new PSlice(start.intValue(), stop.intValue(), step.intValue());
    }

    @Generic
    public Object doGeneric(Object startObj, Object stopObj, Object stepObj) {
        int start = 0;

        if (startObj instanceof Integer) {
            start = (Integer) startObj;
        } else if (startObj instanceof BigInteger) {
            start = ((BigInteger) startObj).intValue();
        }

        int stop = 0;
        if (stopObj instanceof Integer) {
            stop = (Integer) stopObj;
        } else if (stopObj instanceof BigInteger) {
            stop = ((BigInteger) stopObj).intValue();
        }

        int step = 1;
        if (stepObj instanceof Integer) {
            step = (Integer) stepObj;
        } else if (stepObj instanceof BigInteger) {
            step = ((BigInteger) stepObj).intValue();
        }

        return new PSlice(start, stop, step);
    }

}
