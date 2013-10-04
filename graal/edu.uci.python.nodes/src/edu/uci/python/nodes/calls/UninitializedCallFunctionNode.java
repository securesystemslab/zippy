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
package edu.uci.python.nodes.calls;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;

public class UninitializedCallFunctionNode extends CallFunctionNode {

    @Child protected final PNode callee;

    public UninitializedCallFunctionNode(PNode callee, PNode[] arguments, PNode[] keywords) {
        super(arguments, keywords);
        this.callee = callee;
    }

    @Override
    public PNode getCallee() {
        return callee;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object calleeObj = callee.execute(frame);

        if (calleeObj instanceof PCallable) {
            PCallable callable = (PCallable) calleeObj;

            if (callable.isBuiltin()) {
                CallBuiltInFunctionNode callBuiltIn = CallBuiltInFunctionNodeFactory.create(callable, callable.getName(), arguments, keywords);
                replace(callBuiltIn);
                return callBuiltIn.doGeneric(frame);
            } else {
                CallFunctionNode callFunction = CallFunctionNodeFactory.create(arguments, keywords, callee);
                replace(callFunction);
                return callFunction.doPCallable(frame, callable);
            }
        } else {
            CallFunctionNode callFunction = CallFunctionNodeFactory.create(arguments, keywords, callee);
            replace(callFunction);
            return callFunction.doGeneric(frame, calleeObj);
        }
    }

}
