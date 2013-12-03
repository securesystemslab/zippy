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
import edu.uci.python.nodes.statements.*;
import edu.uci.python.runtime.function.*;

public class FunctionDefinitionNode extends PNode {

    private final String name;
    private final CallTarget callTarget;
    private final FrameDescriptor frameDescriptor;
    private final boolean needsDeclarationFrame;

    // It's parked here, but not adopted.
    @Child protected ParametersNode parameters;

    public FunctionDefinitionNode(String name, ParametersNode parameters, CallTarget callTarget, FrameDescriptor frameDescriptor, boolean needsDeclarationFrame) {
        this.name = name;
        this.parameters = parameters;
        this.callTarget = callTarget;
        this.frameDescriptor = frameDescriptor;
        this.needsDeclarationFrame = needsDeclarationFrame;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        parameters.evaluateDefaults(frame);
        MaterializedFrame declarationFrame = needsDeclarationFrame ? frame.materialize() : null;
        return new PFunction(name, parameters.getParameterNames(), callTarget, frameDescriptor, declarationFrame);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + name + ")";
    }
}
