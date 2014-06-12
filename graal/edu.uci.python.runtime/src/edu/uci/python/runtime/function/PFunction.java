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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.standardtype.*;

public class PFunction extends PythonBuiltinObject implements PythonCallable {

    private final String name;
    private final String enclosingClassName;
    private final Arity arity;
    private final RootCallTarget callTarget;
    private final FrameDescriptor frameDescriptor;
    private final MaterializedFrame declarationFrame;

    public PFunction(String name, String enclosingClassName, Arity arity, RootCallTarget callTarget, FrameDescriptor frameDescriptor, MaterializedFrame declarationFrame) {
        this.name = name;
        this.enclosingClassName = enclosingClassName;
        this.arity = arity;
        this.callTarget = callTarget;
        this.frameDescriptor = frameDescriptor;
        this.declarationFrame = declarationFrame;
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
        return callTarget.getRootNode();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isClassMethod() {
        return arity.isClassMethod();
    }

    @Override
    public Object call(Object[] arguments) {
        PArguments.setDeclarationFrame(arguments, declarationFrame);
        return callTarget.call(arguments);
    }

    @Override
    public Object call(Object[] arguments, PKeyword[] keywords) {
        PArguments.setDeclarationFrame(arguments, declarationFrame);
        return callTarget.call(PArguments.applyKeywordArgs(arity, arguments, keywords));
    }

    @Override
    public Arity getArity() {
        return arity;
    }

    @Override
    public void arityCheck(int numOfArgs, int numOfKeywords, String[] keywords) {
        arity.arityCheck(numOfArgs, numOfKeywords, keywords);
    }

    @Override
    public String toString() {
        String fullName = enclosingClassName == null ? name : enclosingClassName + '.' + name;
        return "<function " + fullName + " at " + hashCode() + ">";
    }

}
