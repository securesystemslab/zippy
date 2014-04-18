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
package edu.uci.python.nodes.call.legacy;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.function.*;

public class CallBuiltinInlinedNode extends InlinedCallNode {

    private final PBuiltinFunction function;
    private final BuiltinFunctionRootNode functionRoot;
    private final Assumption globalScopeUnchanged;
    private final Assumption builtinModuleUnchanged;

    // Dummy, does not need {@link PythonFrameTypeConversion}
    private static final FrameDescriptor FrameDescriptor = new FrameDescriptor();

    public CallBuiltinInlinedNode(PNode callee, PNode[] arguments, PBuiltinFunction function, BuiltinFunctionRootNode functionRoot, Assumption globalScopeUnchanged, Assumption builtinModuleUnchanged,
                    FrameFactory frameFactory) {
        super(callee, arguments, FrameDescriptor, frameFactory);
        this.function = function;
        this.functionRoot = functionRoot;
        this.globalScopeUnchanged = globalScopeUnchanged;
        this.builtinModuleUnchanged = builtinModuleUnchanged;
    }

    @Override
    public PBuiltinFunction getCallee() {
        return function;
    }

    public CallTarget getCallTarget() {
        return function.getCallTarget();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            globalScopeUnchanged.check();
            builtinModuleUnchanged.check();
        } catch (InvalidAssumptionException e) {
            return uninitialize(frame);
        }

        final Object[] args = DispatchCallNode.executeArguments(frame, arguments);
        final PArguments pargs = new PArguments(null, args);
        return functionRoot.execute(createInlinedFrame(frame, pargs));
    }

}
