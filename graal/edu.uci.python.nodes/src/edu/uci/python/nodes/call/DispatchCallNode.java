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
import edu.uci.python.nodes.object.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class DispatchCallNode extends PNode {

    @Child protected PNode primaryNode;
    @Children protected final PNode[] argumentNodes;

    protected final String calleeName;
    @SuppressWarnings("unused") private boolean passPrimaryAsTheFirstArgument;

    public DispatchCallNode(String calleeName, PNode primary, PNode[] arguments) {
        this.calleeName = calleeName;
        this.primaryNode = primary;
        this.argumentNodes = arguments;
    }

    public static DispatchCallNode create(PythonCallable callee, PNode calleeNode, PNode[] argumentNodes) {
        PNode primaryNode;

        if (calleeNode instanceof HasPrimaryNode) {
            HasPrimaryNode hasPrimary = (HasPrimaryNode) calleeNode;
            primaryNode = hasPrimary.extractPrimary();
        } else {
            primaryNode = EmptyNode.INSTANCE;
        }

        return new UninitializedCallNode(callee.getName(), primaryNode, calleeNode, argumentNodes);
    }

    public static final class BoxedCallNode extends DispatchCallNode {

        @Child protected CallDispatchBoxedNode dispatchBoxedNode;

        public BoxedCallNode(String calleeName, PNode primary, PNode[] arguments, CallDispatchBoxedNode dispatch) {
            super(calleeName, primary, arguments);
            dispatchBoxedNode = dispatch;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object[] arguments = CallFunctionNode.executeArguments(frame, argumentNodes);
            PythonBasicObject primary;
            try {
                primary = primaryNode.executePythonBasicObject(frame);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new IllegalStateException();
            }

            return dispatchBoxedNode.executeCall(frame, primary, arguments);
        }
    }

    public static final class UnboxedCallNode extends DispatchCallNode {

        @Child protected CallDispatchUnboxedNode dispatchNode;

        public UnboxedCallNode(String calleeName, PNode primary, PNode[] arguments, CallDispatchUnboxedNode dispatch) {
            super(calleeName, primary, arguments);
            dispatchNode = dispatch;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object[] arguments = CallFunctionNode.executeArguments(frame, argumentNodes);
            Object primary = primaryNode.execute(frame);
            return dispatchNode.executeCall(frame, primary, arguments);
        }
    }

    public static final class UninitializedCallNode extends DispatchCallNode {

        @Child protected PNode calleeNode;

        public UninitializedCallNode(String calleeName, PNode primary, PNode callee, PNode[] arguments) {
            super(calleeName, primary, arguments);
            this.calleeNode = callee;
        }

        private static boolean isPrimaryBoxed(Object primary, PythonCallable callee) {
            if (primary instanceof PythonModule) {
                return true;
            } else if (primary instanceof PythonClass) {
                return true;
            } else if (primary instanceof PythonObject && callee instanceof PMethod) {
                return true;
            }

            return false;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            CompilerAsserts.neverPartOfCompilation();

            Object[] arguments = CallFunctionNode.executeArguments(frame, argumentNodes);
            Object primary = primaryNode.execute(frame);
            PythonCallable callee;
            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException();
            }

            if (isPrimaryBoxed(primary, callee)) {
                CallDispatchBoxedNode dispatch = CallDispatchBoxedNode.create(callee, calleeNode);
                replace(new BoxedCallNode(calleeName, primaryNode, argumentNodes, dispatch));
                return dispatch.executeCall(frame, (PythonBasicObject) primary, arguments);
            }

            CallDispatchUnboxedNode dispatch = CallDispatchUnboxedNode.create(callee, calleeNode);
            replace(new UnboxedCallNode(calleeName, primaryNode, argumentNodes, dispatch));
            return dispatch.executeCall(frame, primary, arguments);
        }
    }

}
