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
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class DispatchCallNode extends PNode {

    @Child protected PNode primaryNode;
    @Children protected final PNode[] argumentNodes;
    @Children protected final PNode[] keywordNodes;

    protected final String calleeName;
    protected final boolean passPrimaryAsTheFirstArgument;
    protected final PythonContext context;

    public DispatchCallNode(PythonContext context, String calleeName, PNode primary, PNode[] arguments, PNode[] keywords, boolean passPrimary) {
        this.context = context;
        this.calleeName = calleeName;
        this.primaryNode = primary;
        this.argumentNodes = arguments;
        this.keywordNodes = keywords;
        this.passPrimaryAsTheFirstArgument = passPrimary;
    }

    public static DispatchCallNode create(PythonContext context, PythonCallable callee, PNode calleeNode, PNode[] argumentNodes, PNode[] keywords) {
        PNode primaryNode;

        if (calleeNode instanceof HasPrimaryNode) {
            HasPrimaryNode hasPrimary = (HasPrimaryNode) calleeNode;
            primaryNode = hasPrimary.extractPrimary();
        } else {
            primaryNode = EmptyNode.INSTANCE;
        }

        return new UninitializedCallNode(context, callee.getName(), primaryNode, calleeNode, argumentNodes, keywords);
    }

    /**
     * Pack primary into the evaluated arguments array if passPrimary is true.
     *
     */
    @ExplodeLoop
    protected static final Object[] executeArguments(VirtualFrame frame, boolean passPrimary, Object primary, PNode[] arguments) {
        final int length = passPrimary ? arguments.length + 1 : arguments.length;
        final Object[] evaluated = new Object[length];
        final int offset;

        if (passPrimary) {
            evaluated[0] = primary;
            offset = 1;
        } else {
            offset = 0;
        }

        for (int i = 0; i < arguments.length; i++) {
            evaluated[i + offset] = arguments[i].execute(frame);
        }

        return evaluated;
    }

    public static final class BoxedCallNode extends DispatchCallNode {

        @Child protected CallDispatchBoxedNode dispatchBoxedNode;

        public BoxedCallNode(PythonContext context, String calleeName, PNode primary, PNode[] arguments, PNode[] keywords, CallDispatchBoxedNode dispatch, boolean passPrimary) {
            super(context, calleeName, primary, arguments, keywords, passPrimary);
            dispatchBoxedNode = dispatch;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonBasicObject primary;

            try {
                primary = primaryNode.executePythonBasicObject(frame);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new IllegalStateException();
            }

            Object[] arguments = executeArguments(frame, passPrimaryAsTheFirstArgument, primary, argumentNodes);
            PKeyword[] keywords = CallFunctionNode.executeKeywordArguments(frame, keywordNodes);
            return dispatchBoxedNode.executeCall(frame, primary, arguments, keywords);
        }
    }

    public static final class UnboxedCallNode extends DispatchCallNode {

        @Child protected CallDispatchUnboxedNode dispatchNode;

        public UnboxedCallNode(PythonContext context, String calleeName, PNode primary, PNode[] arguments, PNode[] keywords, CallDispatchUnboxedNode dispatch, boolean passPrimary) {
            super(context, calleeName, primary, arguments, keywords, passPrimary);
            dispatchNode = dispatch;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object primary = primaryNode.execute(frame);
            Object[] arguments = executeArguments(frame, passPrimaryAsTheFirstArgument, primary, argumentNodes);
            return dispatchNode.executeCall(frame, primary, arguments);
        }
    }

    public static final class NoneCallNode extends DispatchCallNode {

        @Child protected PNode calleeNode;
        @Child protected CallDispatchNoneNode dispatchNode;

        public NoneCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, PNode[] arguments, PNode[] keywords, CallDispatchNoneNode dispatch) {
            super(context, calleeName, primary, arguments, keywords, false);
            this.calleeNode = callee;
            this.dispatchNode = dispatch;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonCallable callee;

            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
            }

            Object[] arguments = executeArguments(frame, false, null, argumentNodes);
            PKeyword[] keywords = CallFunctionNode.executeKeywordArguments(frame, keywordNodes);
            return dispatchNode.executeCall(frame, callee, arguments, keywords);
        }

    }

    public static final class UninitializedCallNode extends DispatchCallNode {

        @Child protected PNode calleeNode;

        public UninitializedCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, PNode[] arguments, PNode[] keywords) {
            super(context, calleeName, primary, arguments, keywords, false);
            this.calleeNode = callee;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            CompilerAsserts.neverPartOfCompilation();

            Object primary = primaryNode.execute(frame);
            boolean passPrimaryAsArgument = haveToPassPrimary(primary);
            Object[] arguments = executeArguments(frame, passPrimaryAsArgument, primary, argumentNodes);
            PKeyword[] keywords = CallFunctionNode.executeKeywordArguments(frame, keywordNodes);
            PythonCallable callee;

            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException();
            }

            if (isPrimaryNone(primary)) {
                CallDispatchNoneNode dispatch = CallDispatchNoneNode.create(callee, keywords);
                replace(new NoneCallNode(context, calleeName, primaryNode, calleeNode, argumentNodes, keywordNodes, dispatch));
                return dispatch.executeCall(frame, callee, arguments, keywords);
            }

            if (isPrimaryBoxed(primary, callee)) {
                CallDispatchBoxedNode dispatch = CallDispatchBoxedNode.create(context, (PythonBasicObject) primary, callee, calleeNode, keywords);
                replace(new BoxedCallNode(context, calleeName, primaryNode, argumentNodes, keywordNodes, dispatch, passPrimaryAsArgument));
                return dispatch.executeCall(frame, (PythonBasicObject) primary, arguments, keywords);
            }

            CallDispatchUnboxedNode dispatch = CallDispatchUnboxedNode.create(primary, callee, calleeNode);
            replace(new UnboxedCallNode(context, calleeName, primaryNode, argumentNodes, keywordNodes, dispatch, passPrimaryAsArgument));
            return dispatch.executeCall(frame, primary, arguments);
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

        private boolean isPrimaryNone(Object primary) {
            return primaryNode == EmptyNode.INSTANCE && primary == PNone.NONE;
        }

        private boolean haveToPassPrimary(Object primary) {
            return !isPrimaryNone(primary) && !(primary instanceof PythonClass) && !(primary instanceof PythonModule);
        }
    }

}
