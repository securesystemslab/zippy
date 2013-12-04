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
package edu.uci.python.runtime.function;

import java.util.*;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.impl.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.standardtypes.*;

public final class PFunction extends PythonBuiltinObject implements PythonCallable {

    private final String name;
    private final List<String> parameters;
    private final CallTarget callTarget;
    private final FrameDescriptor frameDescriptor;
    private final MaterializedFrame declarationFrame;

    public PFunction(String name, List<String> parameters, CallTarget callTarget, FrameDescriptor frameDescriptor, MaterializedFrame declarationFrame) {
        this.name = name;
        this.parameters = parameters;
        this.callTarget = callTarget;
        this.frameDescriptor = frameDescriptor;
        this.declarationFrame = declarationFrame;
    }

    public static PFunction duplicate(PFunction function, CallTarget newCallTarget) {
        return new PFunction(function.name, function.parameters, newCallTarget, function.frameDescriptor, function.declarationFrame);
    }

    public CallTarget getCallTarget() {
        return callTarget;
    }

    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public MaterializedFrame getDeclarationFrame() {
        return declarationFrame;
    }

    public RootNode getFunctionRootNode() {
        DefaultCallTarget defaultTarget = (DefaultCallTarget) callTarget;
        return defaultTarget.getRootNode();
    }

    public String getName() {
        return name;
    }

    @Override
    public Object call(PackedFrame caller, Object[] args) {
        return callTarget.call(caller, new PArguments(PNone.NONE, declarationFrame, args));
    }

    @Override
    public Object call(PackedFrame caller, Object[] arguments, PKeyword[] keywords) {
        Object[] combined = applyKeywordArgs(parameters, arguments, keywords);
        return callTarget.call(caller, new PArguments(PNone.NONE, declarationFrame, combined));
    }

    protected static Object[] applyKeywordArgs(List<String> parameters, Object[] arguments, Object[] keywords) {
        Object[] combined = new Object[parameters.size()];
        assert combined.length >= arguments.length : "Parameters size does not match";
        System.arraycopy(arguments, 0, combined, 0, arguments.length);

        for (int i = 0; i < keywords.length; i++) {
            PKeyword keyarg = (PKeyword) keywords[i];
            int keywordIdx = parameters.indexOf(keyarg.getName());

            if (keywordIdx < -1) {
                /**
                 * TODO can throw a type error for wrong keyword name // TypeError: foo() got an
                 * unexpected keyword argument 'c'
                 */
            }
            combined[keywordIdx] = keyarg.getValue();
        }

        return combined;
    }

    @Override
    public String toString() {
        return "<fucntion " + name + " at " + hashCode() + ">";
    }
}
