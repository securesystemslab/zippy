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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

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
            return new GeneratorDispatchNoneNode((PGeneratorFunction) callee, next);
        }

        if (callee instanceof PFunction) {
            return new LinkedDispatchNoneNode(callee, next);
        }

        if (callee instanceof PythonClass) {
            PythonClass clazz = (PythonClass) callee;
            return new LinkedDispatchNoneNode((PythonCallable) clazz.getAttribute("__init__"), next);
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
    public static final class LinkedDispatchNoneNode extends CallDispatchNoneNode {

        @Child protected InvokeNode invoke;
        @Child protected CallDispatchNoneNode next;
        private final PythonCallable cachedCallee;

        public LinkedDispatchNoneNode(PythonCallable callee, UninitializedDispatchNoneNode next) {
            super(callee.getName());
            this.invoke = InvokeNode.create(callee, next.hasKeyword);
            this.next = next;
            this.cachedCallee = callee;
        }

        protected InvokeNode getInvokeNode() {
            return invoke;
        }

        @Override
        public NodeCost getCost() {
            return getCost(next);
        }

        @Override
        public boolean isInlined() {
            return getCost() == NodeCost.MONOMORPHIC && invoke.isInlined();
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object[] arguments, PKeyword[] keywords) {
            if (cachedCallee == callee) {
                return invoke.invoke(frame, null, arguments, keywords);
            }

            return next.executeCall(frame, callee, arguments, keywords);
        }
    }

    public static final class GeneratorDispatchNoneNode extends CallDispatchNoneNode implements GeneratorDispatch {

        @Child protected CallDispatchNoneNode next;
        private final PGeneratorFunction generator;

        public GeneratorDispatchNoneNode(PGeneratorFunction callee, UninitializedDispatchNoneNode next) {
            super(callee.getName());
            this.next = next;
            this.generator = callee;
        }

        @Override
        protected void onAdopt() {
            RootNode root = getRootNode();
            if (root instanceof FunctionRootNode) {
                ((FunctionRootNode) root).reportGeneratorDispatch();
            }
        }

        @Override
        public NodeCost getCost() {
            return getCost(next);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object[] arguments, PKeyword[] keywords) {
            if (generator == callee) {
                return generator.call(arguments);
            }

            return next.executeCall(frame, callee, arguments, keywords);
        }

        @Override
        public PGeneratorFunction getGeneratorFunction() {
            return generator;
        }

        @Override
        public PNode getCallNode() {
            return (PNode) getTop().getParent();
        }
    }

    @NodeInfo(cost = NodeCost.MEGAMORPHIC)
    public static final class GenericDispatchNoneNode extends CallDispatchNoneNode {

        public GenericDispatchNoneNode(String calleeName) {
            super(calleeName);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object[] arguments, PKeyword[] keywords) {
            return callee.call(arguments);
        }
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static final class UninitializedDispatchNoneNode extends CallDispatchNoneNode {

        private final boolean hasKeyword;

        public UninitializedDispatchNoneNode(String calleeName, boolean hasKeyword) {
            super(calleeName);
            this.hasKeyword = hasKeyword;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object[] arguments, PKeyword[] keywords) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            CallDispatchNoneNode specialized;

            if (getDispatchDepth() < PythonOptions.CallSiteInlineCacheMaxDepth) {
                specialized = replace(CallDispatchNoneNode.create(callee, keywords));
            } else {
                specialized = getTop().replace(new GenericDispatchNoneNode(calleeName));
            }

            return specialized.executeCall(frame, callee, arguments, keywords);
        }
    }

}
