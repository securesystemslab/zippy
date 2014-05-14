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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.impl.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.standardtype.*;

public class PFunction extends PythonBuiltinObject implements PythonCallable {

    private final String name;
    private final Arity arity;
    private final RootCallTarget callTarget;
    private final FrameDescriptor frameDescriptor;
    private final MaterializedFrame declarationFrame;
    protected final PythonContext context;

    public PFunction(String name, PythonContext context, Arity arity, RootCallTarget callTarget, FrameDescriptor frameDescriptor, MaterializedFrame declarationFrame) {
        this.name = name;
        this.arity = arity;
        this.callTarget = callTarget;
        this.frameDescriptor = frameDescriptor;
        this.declarationFrame = declarationFrame;
        this.context = context;
    }

    public static PFunction duplicate(PFunction function, RootCallTarget newCallTarget) {
        return new PFunction(function.name, function.context, function.arity, newCallTarget, function.frameDescriptor, function.declarationFrame);
    }

    @Override
    public RootCallTarget getCallTarget() {
        return callTarget;
    }

    @Override
    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    public MaterializedFrame getDeclarationFrame() {
        return declarationFrame;
    }

    public RootNode getFunctionRootNode() {
        DefaultCallTarget defaultTarget = (DefaultCallTarget) callTarget;
        return defaultTarget.getRootNode();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object call(Object[] args) {
        return callTarget.call(new PArguments(declarationFrame, args).packAsObjectArray());
    }

    @Override
    public Object call(Object[] arguments, PKeyword[] keywords) {
        Object[] combined = applyKeywordArgs(arity, arguments, keywords);
        return callTarget.call(new PArguments(declarationFrame, combined).packAsObjectArray());
    }

    @Override
    public Arity getArity() {
        return arity;
    }

    @Override
    public void arityCheck(int numOfArgs, int numOfKeywords, String[] keywords) {
        arity.arityCheck(numOfArgs, numOfKeywords, keywords);
    }

    public static Object[] applyKeywordArgs(Arity calleeArity, Object[] arguments, PKeyword[] keywords) {
        List<String> parameters = calleeArity.getParameterIds();
        Object[] combined = new Object[parameters.size()];
        assert combined.length >= arguments.length : "Parameters size does not match";
        System.arraycopy(arguments, 0, combined, 0, arguments.length);

        for (int i = 0; i < keywords.length; i++) {
            PKeyword keyarg = keywords[i];
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
        return "<function " + name + " at " + hashCode() + ">";
    }

}
