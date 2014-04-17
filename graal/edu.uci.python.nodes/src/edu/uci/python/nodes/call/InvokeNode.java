/*
 * Copyright (c) 2014, Regents of the University of California
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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.function.*;

public abstract class InvokeNode extends Node {

    @Child protected CallNode callNode;

    public InvokeNode(CallNode callNode) {
        this.callNode = callNode;
    }

    protected abstract Object invoke(VirtualFrame frame, Object primary, Object[] arguments, PKeyword[] keywords);

    public static InvokeNode create(PythonCallable callee, boolean hasKeyword) {
        if (callee instanceof PFunction) {
            return createInvokeFunctionNode((PFunction) callee, hasKeyword);
        } else if (callee instanceof PMethod) {
            PMethod method = (PMethod) callee;
            return new InvokeFunctionNode(method.__func__());
        } else if (callee instanceof PBuiltinFunction) {
            return createInvokeBuiltinNode((PBuiltinFunction) callee, hasKeyword);
        } else if (callee instanceof PBuiltinMethod) {
            PBuiltinMethod method = (PBuiltinMethod) callee;
            return new InvokeBuiltinFunctionNode(method.__func__(), false);
        }

        throw new UnsupportedOperationException("Unsupported callee type " + callee);
    }

    private static InvokeNode createInvokeFunctionNode(PFunction callee, boolean hasKeyword) {
        if (!hasKeyword) {
            return new InvokeFunctionNode(callee);
        } else {
            return new InvokeFunctionWithKeywordsNode(callee);
        }
    }

    // Split built-in constructors.
    private static InvokeNode createInvokeBuiltinNode(PBuiltinFunction callee, boolean hasKeyword) {
        boolean split = callee.getName().equals("__init__");

        if (!hasKeyword) {
            return new InvokeBuiltinFunctionNode(callee, split);
        } else {
            return new InvokeBuiltinFunctionWithKeywordsNode(callee, split);
        }
    }

    public static final class InvokeFunctionNode extends InvokeNode {

        private final MaterializedFrame declarationFrame;

        public InvokeFunctionNode(PFunction callee) {
            super(Truffle.getRuntime().createCallNode(callee.getCallTarget()));
            this.declarationFrame = callee.getDeclarationFrame();
        }

        @Override
        protected Object invoke(VirtualFrame frame, Object primary, Object[] arguments, PKeyword[] keywords) {
            PArguments arg = new PArguments(declarationFrame, arguments);
            return callNode.call(frame.pack(), arg);
        }
    }

    public static final class InvokeBuiltinFunctionNode extends InvokeNode {

        public InvokeBuiltinFunctionNode(PBuiltinFunction callee, boolean split) {
            super(Truffle.getRuntime().createCallNode(split ? callee.getCallTarget() : CallDispatchNode.split(callee.getCallTarget())));
        }

        @Override
        protected Object invoke(VirtualFrame frame, Object primary, Object[] arguments, PKeyword[] keywords) {
            PArguments arg = new PArguments(null, arguments);
            return callNode.call(frame.pack(), arg);
        }
    }

    public static final class InvokeFunctionWithKeywordsNode extends InvokeNode {

        private final MaterializedFrame declarationFrame;
        private final Arity arity;

        public InvokeFunctionWithKeywordsNode(PFunction callee) {
            super(Truffle.getRuntime().createCallNode(callee.getCallTarget()));
            this.declarationFrame = callee.getDeclarationFrame();
            this.arity = callee.getArity();
        }

        @Override
        protected Object invoke(VirtualFrame frame, Object primary, Object[] arguments, PKeyword[] keywords) {
            Object[] combined = PFunction.applyKeywordArgs(arity, arguments, keywords);
            PArguments arg = new PArguments(declarationFrame, combined);
            return callNode.call(frame.pack(), arg);
        }
    }

    public static final class InvokeBuiltinFunctionWithKeywordsNode extends InvokeNode {

        private final Arity arity;

        public InvokeBuiltinFunctionWithKeywordsNode(PBuiltinFunction callee, boolean split) {
            super(Truffle.getRuntime().createCallNode(split ? callee.getCallTarget() : CallDispatchNode.split(callee.getCallTarget())));
            this.arity = callee.getArity();
        }

        @Override
        protected Object invoke(VirtualFrame frame, Object primary, Object[] arguments, PKeyword[] keywords) {
            Object[] combined = PFunction.applyKeywordArgs(arity, arguments, keywords);
            PArguments arg = new PArguments(null, combined);
            return callNode.call(frame.pack(), arg);
        }
    }

}
