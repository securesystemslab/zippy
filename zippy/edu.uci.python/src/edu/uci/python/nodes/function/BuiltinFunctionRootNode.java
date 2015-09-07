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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.*;
import edu.uci.python.nodes.*;

/**
 * @author Gulfem
 * @author zwei
 */
public class BuiltinFunctionRootNode extends RootNode {

    private final String functionName;

    @Child protected PythonBuiltinNode body;
    private final PythonBuiltinNode uninitialized;

    public BuiltinFunctionRootNode(String functionName, PythonBuiltinNode builtinNode) {
        super(PythonLanguage.class, null, null);
        this.functionName = functionName;
        this.body = builtinNode;
        this.uninitialized = NodeUtil.cloneNode(builtinNode);
    }

    @Override
    public RootNode copy() {
        return new BuiltinFunctionRootNode(functionName, NodeUtil.cloneNode(uninitialized));
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return body.execute(frame);
    }

    public String getFunctionName() {
        return functionName;
    }

    public PNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "<builtin function " + functionName + " at " + Integer.toHexString(hashCode()) + ">";
    }

}
