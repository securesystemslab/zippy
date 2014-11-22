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
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.standardtype.*;

public final class PGenerator extends PythonBuiltinObject implements PIterator {

    public static final PythonBuiltinClass __class__ = PythonContext.getBuiltinTypeFor(PGenerator.class);

    protected final String name;
    protected final RootCallTarget callTarget;
    protected final FrameDescriptor frameDescriptor;
    protected final Object[] arguments;

    public static PGenerator create(String name, RootCallTarget callTarget, FrameDescriptor frameDescriptor, MaterializedFrame declarationFrame, Object[] arguments, int numOfActiveFlags,
                    int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
        /**
         * Setting up the persistent frame in {@link #arguments}.
         */
        GeneratorControlData generatorArgs = new GeneratorControlData(numOfActiveFlags, numOfGeneratorBlockNode, numOfGeneratorForNode);
        MaterializedFrame generatorFrame = Truffle.getRuntime().createMaterializedFrame(PArguments.create(), frameDescriptor);
        PArguments.setDeclarationFrame(arguments, declarationFrame);
        PArguments.setGeneratorFrame(arguments, generatorFrame);
        PArguments.setControlData(arguments, generatorArgs);
        return new PGenerator(name, callTarget, frameDescriptor, arguments);
    }

    public PGenerator(String name, RootCallTarget callTarget, FrameDescriptor frameDescriptor, Object[] arguments) {
        this.name = name;
        this.callTarget = callTarget;
        this.frameDescriptor = frameDescriptor;
        this.arguments = arguments;
    }

    @Override
    public PythonBuiltinClass __class__() {
        return __class__;
    }

    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    public RootCallTarget getCallTarget() {
        return callTarget;
    }

    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object __next__() throws StopIterationException {
        return callTarget.call(arguments);
    }

    public Object send(Object value) throws StopIterationException {
        PArguments.setSpecialArgument(arguments, value);
        return callTarget.call(arguments);
    }

    @Override
    public String toString() {
        return "<generator object '" + name + "' at " + hashCode() + ">";
    }

}
