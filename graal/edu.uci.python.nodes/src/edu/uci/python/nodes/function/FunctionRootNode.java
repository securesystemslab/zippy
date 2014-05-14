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

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.profiler.*;
import edu.uci.python.runtime.*;

/**
 * RootNode of a Python Function body. It is invoked by a CallTarget.
 *
 * @author zwei
 */
public final class FunctionRootNode extends RootNode {

    private final PythonContext context;
    private final String functionName;

    @Child protected PNode body;
    private PNode uninitializedBody;

    @Child private PNode profiler;

    public FunctionRootNode(PythonContext context, String functionName, FrameDescriptor frameDescriptor, PNode body) {
        super(null, frameDescriptor); // SourceSection is not supported yet.
        this.context = context;
        this.functionName = functionName;
        this.body = body;
        this.uninitializedBody = NodeUtil.cloneNode(body);
        if (PythonOptions.ProfileCalls) {
            this.profiler = new ProfilerNode(this);
        } else {
            this.profiler = EmptyNode.INSTANCE;
        }
    }

    public PythonContext getContext() {
        return context;
    }

    public String getFunctionName() {
        return functionName;
    }

    public PNode getBody() {
        return body;
    }

    public InlinedFunctionRootNode getInlinedRootNode() {
        return new InlinedFunctionRootNode(this);
    }

    public PNode getUninitializedBody() {
        return uninitializedBody;
    }

    public PNode getClonedUninitializedBody() {
        return NodeUtil.cloneNode(uninitializedBody);
    }

    @Override
    public FunctionRootNode copy() {
        return new FunctionRootNode(this.context, this.functionName, this.getFrameDescriptor(), this.uninitializedBody);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        profiler.execute(frame);
        return body.execute(frame);
    }

    @Override
    public String toString() {
        return "<function " + functionName + " at " + Integer.toHexString(hashCode()) + ">";
    }

    public static class InlinedFunctionRootNode extends PNode {

        private final String functionName;
        @Child protected PNode body;

        protected InlinedFunctionRootNode(FunctionRootNode node) {
            this.functionName = node.functionName;
            this.body = NodeUtil.cloneNode(node.uninitializedBody);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return body.execute(frame);
        }

        @Override
        public String toString() {
            return "<inlined function root " + functionName + " at " + Integer.toHexString(hashCode()) + ">";
        }
    }

}
