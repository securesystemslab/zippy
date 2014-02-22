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

import java.io.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.profiler.*;

public class PGenerator implements PIterator {

    protected final String name;
    protected final CallTarget callTarget;
    protected final FrameDescriptor frameDescriptor;
    protected final PArguments arguments;

    // Profiling
    private static long profiledTime;

    public static PGenerator create(PythonContext context, String name, CallTarget callTarget, FrameDescriptor frameDescriptor, MaterializedFrame declarationFrame, Object[] arguments,
                    int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
        if (PythonOptions.ProfileGeneratorCalls) {
            resetProfiledTime();
        }
        /**
         * Setting up the persistent frame in {@link #arguments}.
         */
        MaterializedFrame generatorFrame = Truffle.getRuntime().createMaterializedFrame(PArguments.EMPTY_ARGUMENT, frameDescriptor);
        PArguments generatorArgs = new PArguments.GeneratorArguments(declarationFrame, generatorFrame, arguments, numOfGeneratorBlockNode, numOfGeneratorForNode);

        if (PythonOptions.ProfileGeneratorCalls) {
            return new PProfilingGenerator(name, callTarget, frameDescriptor, generatorArgs, context);
        } else {
            return new PGenerator(name, callTarget, frameDescriptor, generatorArgs);
        }
    }

    public PGenerator(String name, CallTarget callTarget, FrameDescriptor frameDescriptor, PArguments arguments) {
        this.name = name;
        this.callTarget = callTarget;
        this.frameDescriptor = frameDescriptor;
        this.arguments = arguments;
    }

    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    @Override
    public Object __next__() throws StopIterationException {
        if (PythonOptions.ProfileGeneratorCalls) {
            long start = System.nanoTime();
            Object result = callTarget.call(null, arguments);
            profiledTime += System.nanoTime() - start;
            Profiler.getInstance().increment(name);
            return result;
        } else {
            return callTarget.call(null, arguments);
        }
    }

    public static void resetProfiledTime() {
        profiledTime = 0;
    }

    public static void printProfiledTime() {
        PrintStream out = System.out;
        out.printf("generator time: %.3f\n", profiledTime / 1000000000.0);
    }

    @Override
    public String toString() {
        return "<generator object '" + name + "' at " + hashCode() + ">";
    }

}
