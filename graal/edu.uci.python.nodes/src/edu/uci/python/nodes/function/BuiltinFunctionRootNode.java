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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.profiler.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem
 * @author zwei
 */
public class BuiltinFunctionRootNode extends RootNode {

    private final String functionName;

    @Child protected PythonBuiltinNode builtinNode;
    private final PythonBuiltinNode uninitialized;

    @Child private PNode profiler;

    public BuiltinFunctionRootNode(String functionName, PythonBuiltinNode builtinNode) {
        this.functionName = functionName;
        this.builtinNode = builtinNode;
        this.uninitialized = NodeUtil.cloneNode(builtinNode);
        if (PythonOptions.ProfileCalls) {
            this.profiler = new ProfilerNode(this);
        } else {
            this.profiler = EmptyNode.INSTANCE;
        }
    }

    @Override
    public RootNode copy() {
        return new BuiltinFunctionRootNode(functionName, NodeUtil.cloneNode(uninitialized));
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (PythonOptions.ProfileCalls) {
            profiler.execute(frame);
        }
        return builtinNode.execute(frame);

    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public String toString() {
        return "<builtin function " + functionName + " at " + Integer.toHexString(hashCode()) + ">";
    }

}
