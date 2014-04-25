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
        assert !(primary instanceof PythonBasicObject);
        UninitializedDispatchUnboxedNode next = new UninitializedDispatchUnboxedNode(callee.getName(), calleeNode, keywords.length != 0);

        if (callee instanceof PBuiltinMethod) {
            return new DispatchBuiltinMethodNode(primary, (PBuiltinMethod) callee, next);
        }

        throw new UnsupportedOperationException("Unsupported callee type " + callee + " calleeNode type " + calleeNode);
    }

    /**
     * The primary is an unboxed object.
     *
     */
    public static final class DispatchBuiltinMethodNode extends CallDispatchUnboxedNode {

        @Child protected InvokeNode invokeNode;
        @Child protected CallDispatchUnboxedNode nextNode;

        private final Class cachedPrimaryType;

        public DispatchBuiltinMethodNode(Object primary, PBuiltinMethod callee, UninitializedDispatchUnboxedNode next) {
            super(callee.getName());
            invokeNode = InvokeNode.create(callee, next.hasKeyword);
            nextNode = next;
            cachedPrimaryType = primary.getClass();
        }

        @Override
        protected Object executeCall(VirtualFrame frame, Object primaryObj, Object[] arguments, PKeyword[] keywords) {
            if (primaryObj.getClass() == cachedPrimaryType) {
                return invokeNode.invoke(frame, primaryObj, arguments, keywords);
            }

            return nextNode.executeCall(frame, primaryObj, arguments, keywords);
        }
    }

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

            CallDispatchNode current = this;
            int depth = 0;
            while (current.getParent() instanceof CallDispatchNode) {
                current = (CallDispatchNode) current.getParent();
                depth++;
            }

            CallDispatchUnboxedNode specialized;
            if (depth < PythonOptions.CallSiteInlineCacheMaxDepth) {
                PythonCallable callee;
                try {
                    callee = calleeNode.executePythonCallable(frame);
                } catch (UnexpectedResultException e) {
                    throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
                }

                CallDispatchUnboxedNode direct = CallDispatchUnboxedNode.create(primaryObj, callee, calleeNode, keywords);
                specialized = replace(direct);
            } else {
                CallDispatchUnboxedNode generic = new GenericDispatchUnboxedNode(calleeName, calleeNode);
                specialized = current.replace(generic);
            }

            return specialized.executeCall(frame, primaryObj, arguments, keywords);
        }
    }

}
