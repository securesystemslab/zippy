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
package edu.uci.python.nodes.function;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;

public class FunctionDefinitionNode extends PNode {

    protected final String name;
    protected final PythonContext context;
    protected final RootCallTarget callTarget;
    protected final FrameDescriptor frameDescriptor;
    protected final boolean needsDeclarationFrame;
    protected final Arity arity;
    @Child protected StatementNode defaults;

    public FunctionDefinitionNode(String name, PythonContext context, Arity arity, StatementNode defaults, RootCallTarget callTarget, FrameDescriptor frameDescriptor, boolean needsDeclarationFrame) {
        this.name = name;
        this.context = context;
        this.callTarget = callTarget;
        this.frameDescriptor = frameDescriptor;
        this.needsDeclarationFrame = needsDeclarationFrame;
        this.arity = arity;
        this.defaults = defaults;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        defaults.executeVoid(frame);
        MaterializedFrame declarationFrame = needsDeclarationFrame ? frame.materialize() : null;
        return new PFunction(name, context, arity, callTarget, frameDescriptor, declarationFrame);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + name + ")";
    }

}
