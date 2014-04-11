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
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.call.CallDispatchNode.UninitializedDispatchNode;
import edu.uci.python.nodes.object.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public class DispatchCallNode extends PNode {

    @Children protected final PNode[] argumentNodes;
    @Child protected PNode primaryNode;
    @Child protected CallDispatchNode dispatchNode;

    protected final String calleeName;
    @SuppressWarnings("unused") private boolean passPrimaryAsTheFirstArgument;

    public DispatchCallNode(String calleeName, PNode primary, PNode[] arguments, CallDispatchNode dispatch) {
        this.calleeName = calleeName;
        this.primaryNode = primary;
        this.argumentNodes = arguments;
        this.dispatchNode = dispatch;
    }

    public static DispatchCallNode create(PythonCallable callee, PNode calleeNode, PNode[] argumentNodes) {
        UninitializedDispatchNode uninitialized = new CallDispatchNode.UninitializedDispatchNode(callee.getName(), calleeNode);
        PNode primaryNode;

        if (calleeNode instanceof HasPrimaryNode) {
            HasPrimaryNode hasPrimary = (HasPrimaryNode) calleeNode;
            primaryNode = hasPrimary.extractPrimary();
        } else {
            primaryNode = EmptyNode.INSTANCE;
        }

        return new UninitializedCallNode(callee.getName(), primaryNode, calleeNode, argumentNodes, CallDispatchNode.create(callee, uninitialized));
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object[] arguments = CallFunctionNode.executeArguments(frame, argumentNodes);
        Object primary = primaryNode.execute(frame);
        return dispatchNode.executeCall(frame, primary, arguments);
    }

    public static final class BoxedCallNode extends DispatchCallNode {

        @Child protected CallDispatchBoxedNode dispatchBoxedNode;

        public BoxedCallNode(String calleeName, PNode primary, PNode[] arguments, CallDispatchBoxedNode dispatch) {
            super(calleeName, primary, arguments, dispatch);
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

        public UnboxedCallNode(String calleeName, PNode primary, PNode[] arguments, CallDispatchNode dispatch) {
            super(calleeName, primary, arguments, dispatch);
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

        public UninitializedCallNode(String calleeName, PNode primary, PNode callee, PNode[] arguments, CallDispatchNode dispatch) {
            super(calleeName, primary, arguments, dispatch);
            this.calleeNode = callee;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object[] arguments = CallFunctionNode.executeArguments(frame, argumentNodes);
            Object primary = primaryNode.execute(frame);
            PythonCallable callee;
            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                // TODO Auto-generated catch block
                throw new IllegalStateException();
            }

            if (primary instanceof PythonBuiltinClass) {
                CallDispatchNode dispatch = CallDispatchNode.create(callee, new CallDispatchNode.UninitializedDispatchNode(calleeName, calleeNode));
                replace(new UnboxedCallNode(calleeName, primaryNode, argumentNodes, dispatch));
                return dispatchNode.executeCall(frame, primary, arguments);
            }

            if (primary instanceof PythonModule && (calleeNode instanceof ReadGlobalNode)) {
                PythonBasicObject primaryObj = (PythonBasicObject) primary;

                if (callee instanceof PFunction) {
                    CallDispatchBoxedNode dispatch = CallDispatchBoxedNode.create(callee, calleeNode, new CallDispatchBoxedNode.UninitializedDispatchNode(calleeName, calleeNode));
                    replace(new BoxedCallNode(calleeName, primaryNode, argumentNodes, dispatch));
                    return dispatch.executeCall(frame, primaryObj, arguments);
                }
            }

            CallDispatchNode dispatch = CallDispatchNode.create(callee, new CallDispatchNode.UninitializedDispatchNode(calleeName, calleeNode));
            replace(new UnboxedCallNode(calleeName, primaryNode, argumentNodes, dispatch));
            return dispatchNode.executeCall(frame, primary, arguments);
        }
    }

}
