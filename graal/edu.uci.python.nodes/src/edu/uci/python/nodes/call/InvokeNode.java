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
        CallTarget callTarget;
        MaterializedFrame declarationFrame = null;
        boolean isBuiltin = false;

        if (callee instanceof PFunction) {
            callTarget = callee.getCallTarget();
            declarationFrame = ((PFunction) callee).getDeclarationFrame();
        } else if (callee instanceof PMethod) {
            PMethod method = (PMethod) callee;
            callTarget = method.__func__().getCallTarget();
            declarationFrame = method.__func__().getDeclarationFrame();
        } else if (callee instanceof PBuiltinFunction) {
            // Split built-in constructors.
            isBuiltin = true;
            boolean split = callee.getName().equals("__init__");
            callTarget = split ? callee.getCallTarget() : InvokeNode.split(callee.getCallTarget());
        } else if (callee instanceof PBuiltinMethod) {
            isBuiltin = true;
            PBuiltinMethod method = (PBuiltinMethod) callee;
            callTarget = InvokeNode.split(method.__func__().getCallTarget());
        } else {
            throw new UnsupportedOperationException("Unsupported callee type " + callee);
        }

        if (hasKeyword && isBuiltin) {
            return new InvokeBuiltinWithKeywordNode(callTarget);
        } else if (hasKeyword) {
            return new InvokeWithKeywordNode(callTarget, declarationFrame, callee.getArity());
        } else {
            return new InvokeNoKeywordNode(callTarget, declarationFrame);
        }
    }

    /**
     * Replicate the CallTarget to let each builtin call site executes its won AST.
     */
    protected static CallTarget split(RootCallTarget callTarget) {
        CompilerAsserts.neverPartOfCompilation();
        RootNode rootNode = callTarget.getRootNode();
        return Truffle.getRuntime().createCallTarget(NodeUtil.cloneNode(rootNode));
    }

    public static final class InvokeNoKeywordNode extends InvokeNode {

        private final MaterializedFrame declarationFrame;

        public InvokeNoKeywordNode(CallTarget callTarget, MaterializedFrame declarationFrame) {
            super(Truffle.getRuntime().createCallNode(callTarget));
            this.declarationFrame = declarationFrame;
        }

        @Override
        protected Object invoke(VirtualFrame frame, Object primary, Object[] arguments, PKeyword[] keywords) {
            PArguments arg = new PArguments(declarationFrame, arguments);
            return callNode.call(frame.pack(), arg);
        }
    }

    public static final class InvokeWithKeywordNode extends InvokeNode {

        private final MaterializedFrame declarationFrame;
        private final Arity arity;

        public InvokeWithKeywordNode(CallTarget callTarget, MaterializedFrame declarationFrame, Arity arity) {
            super(Truffle.getRuntime().createCallNode(callTarget));
            this.declarationFrame = declarationFrame;
            this.arity = arity;
        }

        @Override
        protected Object invoke(VirtualFrame frame, Object primary, Object[] arguments, PKeyword[] keywords) {
            Object[] combined = PFunction.applyKeywordArgs(arity, arguments, keywords);
            PArguments arg = new PArguments(declarationFrame, combined);
            return callNode.call(frame.pack(), arg);
        }
    }

    public static final class InvokeBuiltinWithKeywordNode extends InvokeNode {

        public InvokeBuiltinWithKeywordNode(CallTarget callTarget) {
            super(Truffle.getRuntime().createCallNode(callTarget));
        }

        @Override
        protected Object invoke(VirtualFrame frame, Object primary, Object[] arguments, PKeyword[] keywords) {
            PArguments arg = new PArguments(null, arguments, keywords);
            return callNode.call(frame.pack(), arg);
        }
    }

}
