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
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;

public abstract class CallDispatchUnboxedNode extends CallDispatchNode {

    public CallDispatchUnboxedNode(String calleeName) {
        super(calleeName);
    }

    protected abstract Object executeCall(VirtualFrame frame, Object primaryObj, Object[] arguments, PKeyword[] keywords);

    protected static CallDispatchUnboxedNode create(Object primary, PythonCallable callee, PNode calleeNode, PKeyword[] keywords) {
        assert !(primary instanceof PythonObject);
        UninitializedDispatchUnboxedNode next = new UninitializedDispatchUnboxedNode(callee.getName(), calleeNode, keywords.length != 0);

        if (callee instanceof PBuiltinMethod) {
            return new LinkedDispatchUnboxedNode(primary, (PBuiltinMethod) callee, next);
        }

        throw new UnsupportedOperationException("Unsupported callee type " + callee + " calleeNode type " + calleeNode);
    }

    /**
     * The primary is an unboxed object.
     */
    public static final class LinkedDispatchUnboxedNode extends CallDispatchUnboxedNode {

        @Child protected InvokeNode invoke;
        @Child protected CallDispatchUnboxedNode next;
        private final Class cachedPrimaryType;

        public LinkedDispatchUnboxedNode(Object primary, PBuiltinMethod callee, UninitializedDispatchUnboxedNode next) {
            super(callee.getName());
            this.invoke = InvokeNode.create(callee, next.hasKeyword);
            this.next = next;
            this.cachedPrimaryType = primary.getClass();
        }

        @Override
        public NodeCost getCost() {
            if (next != null && next.getCost() == NodeCost.MONOMORPHIC) {
                return NodeCost.POLYMORPHIC;
            }
            return super.getCost();
        }

        @Override
        protected Object executeCall(VirtualFrame frame, Object primaryObj, Object[] arguments, PKeyword[] keywords) {
            if (primaryObj.getClass() == cachedPrimaryType) {
                return invoke.invoke(frame, primaryObj, arguments, keywords);
            }

            return next.executeCall(frame, primaryObj, arguments, keywords);
        }
    }

    @NodeInfo(cost = NodeCost.MEGAMORPHIC)
    public static final class GenericDispatchUnboxedNode extends CallDispatchUnboxedNode {

        @Child protected PNode calleeNode;

        public GenericDispatchUnboxedNode(String calleeName, PNode callee) {
            super(calleeName);
            calleeNode = callee;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, Object primaryObj, Object[] arguments, PKeyword[] keywords) {
            PythonCallable callee;
            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
            }

            return callee.call(frame.pack(), arguments);
        }
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static final class UninitializedDispatchUnboxedNode extends CallDispatchUnboxedNode {

        @Child protected PNode calleeNode;
        private final boolean hasKeyword;

        public UninitializedDispatchUnboxedNode(String calleeName, PNode calleeNode, boolean hasKeyword) {
            super(calleeName);
            this.calleeNode = calleeNode;
            this.hasKeyword = hasKeyword;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, Object primaryObj, Object[] arguments, PKeyword[] keywords) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            CallDispatchUnboxedNode specialized;

            if (getDispatchDepth() < PythonOptions.CallSiteInlineCacheMaxDepth) {
                PythonCallable callee;
                try {
                    callee = calleeNode.executePythonCallable(frame);
                } catch (UnexpectedResultException e) {
                    throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
                }
                specialized = replace(CallDispatchUnboxedNode.create(primaryObj, callee, calleeNode, keywords));
            } else {
                specialized = getTop().replace(new GenericDispatchUnboxedNode(calleeName, calleeNode));
            }

            return specialized.executeCall(frame, primaryObj, arguments, keywords);
        }
    }

}
