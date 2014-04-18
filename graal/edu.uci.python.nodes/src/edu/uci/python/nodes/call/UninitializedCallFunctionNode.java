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
import edu.uci.python.runtime.function.*;

public class UninitializedCallFunctionNode extends CallFunctionNode {

    @Child protected PNode calleeNode;

    public UninitializedCallFunctionNode(PNode callee, PNode[] arguments, KeywordLiteralNode[] keywords, PythonContext context) {
        super(arguments, keywords, context);
        this.calleeNode = callee;
    }

    @Override
    public PNode getCallee() {
        return calleeNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        transferToInterpreterAndInvalidate();
        Object callee = calleeNode.execute(frame);

        String calleeName;
        if (callee instanceof PythonCallable) {
            PythonCallable callable = (PythonCallable) callee;
            // callable.arityCheck(arguments.length, keywords.length, getKeywordNames());
            calleeName = callable.getName();
        } else {
            calleeName = callee.toString();
        }

        DispatchCallNode callNode = DispatchCallNode.create(getContext(), calleeName, calleeNode, arguments, keywords);
        replace(callNode);
        return callNode.execute(frame);
    }

    @SuppressWarnings("unused")
    private String[] getKeywordNames() {
        String[] keywordNames = new String[keywords.length];

        for (int i = 0; i < keywords.length; i++) {
            keywordNames[i] = keywords[i].getName();
        }

        return keywordNames;
    }

}
