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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;

public class PProfilingGenerator extends PGenerator {

    private final PythonContext context;

    private long innerTime;
    private long outerTime;

    private long iterationStart;
    private long iterationEnd;

    public PProfilingGenerator(String name, CallTarget callTarget, FrameDescriptor frameDescriptor, PArguments arguments, PythonContext context) {
        super(name, callTarget, frameDescriptor, arguments);
        this.context = context;
    }

    @Override
    public Object __next__() throws StopIterationException {
        iterationStart = System.nanoTime();
        outerTime += iterationEnd == 0 ? 0 : iterationStart - iterationEnd;

        try {
            Object result = callTarget.call(arguments.packAsObjectArray());

            iterationEnd = System.nanoTime();
            innerTime += iterationEnd - iterationStart;

            return result;
        } catch (StopIterationException e) {
            iterationEnd = System.nanoTime();
            innerTime += iterationEnd - iterationStart;
            reportProfilingInfo();
            throw e;
        }
    }

    private void reportProfilingInfo() {
        context.registerGeneratorProfilingInfo(name, innerTime, outerTime);
    }

}
