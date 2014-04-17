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

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;

/**
 * Dispatches call sites that do not have the primary part. The specialized dispatch node caches the
 * callee passed from the head of the dispatch chain.
 *
 * @author zwei
 *
 */
public abstract class CallDispatchNoneNode extends CallDispatchNode {

    public CallDispatchNoneNode(String calleeName) {
        super(calleeName);
    }

    protected abstract Object executeCall(VirtualFrame frame, PythonCallable callee, Object[] arguments, PKeyword[] keywords);

    protected static CallDispatchNoneNode create(PythonCallable callee, PKeyword[] keywords) {
        UninitializedDispatchNoneNode next = new UninitializedDispatchNoneNode(callee.getName(), keywords.length != 0);

        if (callee instanceof PGeneratorFunction) {
            return new GenericDispatchNoneNode(callee.getName());
        }

        if (callee instanceof PFunction) {
            return new DispatchVariableFunctionNode((PFunction) callee, next);
        }

        throw new UnsupportedOperationException("Unsupported callee type " + callee);
    }

    /**
     * The callee is not a global attribute or any object's attribute. It could be a local or
     * non-local variable or an intermediate operand.
     * <p>
     * The primary is None for this case.
     *
     */
    public static final class DispatchVariableFunctionNode extends CallDispatchNoneNode {

        @Child protected InvokeNode invokeNode;
        @Child protected CallDispatchNoneNode nextNode;

        private final PythonCallable cachedCallee;

        public DispatchVariableFunctionNode(PFunction callee, UninitializedDispatchNoneNode next) {
            super(callee.getName());
            invokeNode = InvokeNode.create(callee, next.hasKeyword);
            nextNode = next;
            cachedCallee = callee;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object[] arguments, PKeyword[] keywords) {
            if (cachedCallee == callee) {
                return invokeNode.invoke(frame, null, arguments, keywords);
            }

            return nextNode.executeCall(frame, callee, arguments, keywords);
        }
    }

    public static final class GenericDispatchNoneNode extends CallDispatchNoneNode {

        public GenericDispatchNoneNode(String calleeName) {
            super(calleeName);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object[] arguments, PKeyword[] keywords) {
            return callee.call(frame.pack(), arguments);
        }
    }

    public static final class UninitializedDispatchNoneNode extends CallDispatchNoneNode {

        private final boolean hasKeyword;

        public UninitializedDispatchNoneNode(String calleeName, boolean hasKeyword) {
            super(calleeName);
            this.hasKeyword = hasKeyword;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object[] arguments, PKeyword[] keywords) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            CallDispatchNode current = this;
            int depth = 0;
            while (current.getParent() instanceof CallDispatchNode) {
                current = (CallDispatchNode) current.getParent();
                depth++;
            }

            CallDispatchNoneNode specialized;
            if (depth < PythonOptions.CallSiteInlineCacheMaxDepth) {
                CallDispatchNoneNode direct = CallDispatchNoneNode.create(callee, keywords);
                specialized = replace(direct);
            } else {
                CallDispatchNoneNode generic = new GenericDispatchNoneNode(calleeName);
                specialized = current.replace(generic);
            }

            return specialized.executeCall(frame, callee, arguments, keywords);
        }
    }

}
