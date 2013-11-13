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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.standardtypes.*;

public class UninitializedCallFunctionNode extends CallFunctionNode {

    @Child protected PNode callee;

    public UninitializedCallFunctionNode(PNode callee, PNode[] arguments, PNode[] keywords) {
        super(arguments, keywords);
        this.callee = adoptChild(callee);
    }

    @Override
    public PNode getCallee() {
        return callee;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        CompilerAsserts.neverPartOfCompilation();
        Object calleeObj = callee.execute(frame);

        if (calleeObj instanceof PythonClass) {
            CallConstructorNode specialized = new CallConstructorNode(getCallee(), arguments);
            replace(specialized);
            Object[] args = CallFunctionNode.executeArguments(frame, arguments);
            return specialized.callConstructor(frame, (PythonClass) calleeObj, args);
        } else if (calleeObj instanceof PythonCallable) {
            PythonCallable callable = (PythonCallable) calleeObj;

            if (callable instanceof PBuiltinFunction) {
                PBuiltinFunction builtinFunction = (PBuiltinFunction) calleeObj;
                CallBuiltInNode callBuiltIn = CallBuiltInNodeFactory.create(callable, builtinFunction.getName(), arguments, keywords);
                replace(callBuiltIn);
                return callBuiltIn.doGeneric(frame);
            } else if (callable instanceof PBuiltinClass) {
                PBuiltinClass builtinClass = (PBuiltinClass) calleeObj;
                CallBuiltInNode callBuiltIn = CallBuiltInNodeFactory.create(callable, builtinClass.getName(), arguments, keywords);
                replace(callBuiltIn);
                return callBuiltIn.doGeneric(frame);
            } else if (keywords.length == 0) {
                CallFunctionNoKeywordNode callFunction = CallFunctionNoKeywordNode.create(callee, arguments, (PFunction) callable);
                replace(callFunction);
                return callFunction.executeCall(frame, callable);
            } else {
                CallFunctionNode callFunction = CallFunctionNodeFactory.create(arguments, keywords, callee);
                replace(callFunction);
                return callFunction.doPythonCallable(frame, callable);
            }
        } else {
            CallFunctionNode callFunction = CallFunctionNodeFactory.create(arguments, keywords, callee);
            replace(callFunction);
            /**
             * TODO executes the method twice. One of them should be used
             */
            return callFunction.execute(frame);
            // return callFunction.doGeneric(frame, calleeObj);
        }
    }
}
