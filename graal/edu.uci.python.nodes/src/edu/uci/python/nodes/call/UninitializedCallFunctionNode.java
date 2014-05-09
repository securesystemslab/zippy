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
package edu.uci.python.nodes.call;

import static com.oracle.truffle.api.CompilerDirectives.*;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.runtime.*;

public class UninitializedCallFunctionNode extends CallFunctionNode {

    @Child protected PNode callee;

    public UninitializedCallFunctionNode(PNode callee, PNode[] arguments, KeywordLiteralNode[] keywords, PythonContext context) {
        super(arguments, keywords, context);
        this.callee = callee;
    }

    @Override
    public PNode getCallee() {
        return callee;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        transferToInterpreterAndInvalidate();
        Object calleeObj = callee.execute(frame);
// if (calleeObj instanceof PythonCallable) {
// PythonCallable callable = (PythonCallable) calleeObj;
// callable.arityCheck(arguments.length, keywords.length, getKeywordNames());
//
// if (keywords.length == 0) {
// DispatchCallNode callNode = DispatchCallNode.create(callable, callee, arguments);
// replace(callNode);
// return callNode.execute(frame);
// } else {
// CallFunctionNode callFunction = CallFunctionNodeFactory.create(arguments, keywords, getContext(),
// callee);
// replace(callFunction);
// return callFunction.execute(frame);
// }
// } else if (calleeObj instanceof PythonClass) {
// CallConstructorNode specialized = new CallConstructorNode(getCallee(), arguments);
// replace(specialized);
// Object[] args = CallFunctionNode.executeArguments(frame, arguments);
// return specialized.callConstructor(frame, (PythonClass) calleeObj, args);
// } else {
// if ((calleeObj instanceof PyObject) && (PythonOptions.TraceJythonRuntime)) {
// // CheckStyle: stop system..print check
// System.out.println("[ZipPy]: calling jython runtime function " + calleeObj);
// // CheckStyle: resume system..print check
// }
// CallFunctionNode callFunction = CallFunctionNodeFactory.create(arguments, keywords, getContext(),
// callee);
// replace(callFunction);
// return callFunction.execute(frame);
// }
        return null;
    }

    private String[] getKeywordNames() {
        String[] keywordNames = new String[keywords.length];

        for (int i = 0; i < keywords.length; i++) {
            keywordNames[i] = keywords[i].getName();
        }

        return keywordNames;
    }

}
