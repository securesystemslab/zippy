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

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;
import static edu.uci.python.nodes.call.PythonCallUtil.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.argument.*;
import edu.uci.python.nodes.call.CallDispatchBoxedNode.LinkedDispatchBoxedNode;
import edu.uci.python.nodes.call.CallDispatchNoneNode.LinkedDispatchNoneNode;
import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.optimize.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class PythonCallNode extends PNode {

    @Child protected PNode primaryNode;
    @Child protected PNode calleeNode;
    @Child protected ArgumentsNode argumentsNode;
    @Child protected ArgumentsNode keywordsNode;

    protected final String calleeName;
    protected final boolean passPrimaryAsTheFirstArgument;
    protected final PythonContext context;

    public PythonCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords, boolean passPrimary) {
        this.context = context;
        this.calleeName = calleeName;
        this.primaryNode = primary;
        this.calleeNode = callee;
        this.argumentsNode = arguments;
        this.keywordsNode = keywords;
        this.passPrimaryAsTheFirstArgument = passPrimary;
    }

    public static PythonCallNode create(PythonContext context, PNode calleeNode, PNode[] argumentNodes, PNode[] keywords) {
        PNode primaryNode;
        String calleeName;

        if (calleeNode instanceof HasPrimaryNode) {
            HasPrimaryNode hasPrimary = (HasPrimaryNode) calleeNode;
            primaryNode = NodeUtil.cloneNode(hasPrimary.extractPrimary());
            PNodeUtil.clearSourceSections(primaryNode);
            calleeName = ((HasPrimaryNode) calleeNode).getAttributeId();
        } else {
            primaryNode = EmptyNode.create();
            calleeName = "~unknown";
        }

        return new UninitializedCallNode(context, primaryNode, calleeName, calleeNode, new ArgumentsNode(argumentNodes), new ArgumentsNode(keywords));
    }

    public final String getCalleeName() {
        return calleeName;
    }

    public final PNode getPrimaryNode() {
        return primaryNode;
    }

    public final PNode getCalleeNode() {
        return calleeNode;
    }

    public final ArgumentsNode getArgumentsNode() {
        return argumentsNode;
    }

    public final boolean passPrimaryAsArgument() {
        return passPrimaryAsTheFirstArgument;
    }

    @Override
    public boolean hasSideEffectAsAnExpression() {
        return true;
    }

    public abstract boolean isInlined();

    protected Object rewriteAndExecuteCall(VirtualFrame frame, Object primary, Object callee) {
        CompilerDirectives.transferToInterpreterAndInvalidate();

        /**
         * Calls into Jython runtime.
         */
        if (callee instanceof PyObject) {
            PyObject pyobj = (PyObject) callee;
            logJythonRuntime(pyobj);
            return replace(new JythonCallNode(context, pyobj.toString(), primaryNode, calleeNode, argumentsNode, keywordsNode)).executeCall(frame, pyobj);
        }

        PythonCallable callable = null;
        boolean isSpecialMethodDispatch = false;

        try {
            callable = PythonTypesGen.PYTHONTYPES.expectPythonCallable(callee);
        } catch (UnexpectedResultException e) {
            // fall through
        }

        /**
         * Try to resolve __call__.
         */
        if (callable == null) {
            callable = resolveSpecialMethod(callee, "__call__");
            isSpecialMethodDispatch = callable != null;
        }

        /**
         * Failed to resolve a valid callable.
         */
        if (callable == null) {
            throw Py.TypeError("'" + getPythonTypeName(callee) + "' object is not callable");
        }

        /**
         * Determines the shape of the call site.<br>
         * Performs the arith check.<br>
         * Evaluates the arguments.
         */
        boolean passPrimaryAsArgument = PythonCallUtil.haveToPassPrimary(primary, callable, this) || isSpecialMethodDispatch;
        callable.arityCheck(passPrimaryAsArgument ? argumentsNode.length() + 1 : argumentsNode.length(), keywordsNode.length(), PythonCallUtil.getKeywordNames(this));
        Object[] arguments = argumentsNode.executeArguments(frame, passPrimaryAsArgument, isSpecialMethodDispatch ? callee : primary);
        PKeyword[] keywords = keywordsNode.executeKeywordArguments(frame);

        if (isSpecialMethodDispatch) {
            CallDispatchBoxedNode dispatch = CallDispatchBoxedNode.create((PythonObject) callee, "__call__", callable, NodeUtil.cloneNode(calleeNode), PKeyword.EMPTY_KEYWORDS, passPrimaryAsArgument);
            replace(new PythonObjectCallNode(context, callable.getName(), primaryNode, calleeNode, argumentsNode, keywordsNode, dispatch));
            return dispatch.executeCall(frame, (PythonObject) callee, arguments, PKeyword.EMPTY_KEYWORDS);
        }

        if (PythonOptions.IntrinsifyBuiltinCalls && IntrinsifiableBuiltin.isIntrinsifiable(callable)) {
            BuiltinIntrinsifier intrinsifier = new BuiltinIntrinsifier(context, AlwaysValidAssumption.INSTANCE, AlwaysValidAssumption.INSTANCE, this);
            intrinsifier.synthesize();
        }

        /**
         * zwei: Non built-in constructors use ConstructorCallNode. <br>
         * Built-in constructors use regular BoxedCallNode with no special calling convention.
         */
        if (isConstructorCall(primary, callable)) {
            CallDispatchBoxedNode dispatch = CallDispatchBoxedNode.create((PythonObject) primary, calleeName, callable, NodeUtil.cloneNode(calleeNode), keywords, passPrimaryAsArgument);
            CallConstructorNode specialized = null;
            PythonClass clazz = (PythonClass) callable;

            /**
             * If the callee class has switched to a flexible object storage then no need to
             * bootstrap the constructor call again.
             */
            if (PythonOptions.GenerateObjectStorage && !(clazz.getInstanceObjectLayout() instanceof ConservativeObjectLayout)) {
                specialized = new CallConstructorBootstrappingNode(context, (PythonClass) callable, primaryNode, calleeNode, argumentsNode, keywordsNode, dispatch);
            } else {
                specialized = new CallConstructorFastNode(context, (PythonClass) callable, primaryNode, calleeNode, argumentsNode, keywordsNode, dispatch);
            }

            return replace(specialized).executeCall(frame, (PythonObject) primary, (PythonClass) callable);
        }

        if (isPrimaryNone(primary, this)) {
            CallDispatchNoneNode dispatch = CallDispatchNoneNode.create(callable, keywords);
            replace(new NoneCallNode(context, callable.getName(), primaryNode, calleeNode, argumentsNode, keywordsNode, dispatch));
            return dispatch.executeCall(frame, callable, arguments, keywords);
        }

        if (isClassMethodCall(primary, callable)) {
            PythonClass cls = ((PythonObject) primary).asPythonClass();
            CallDispatchBoxedNode dispatch = CallDispatchBoxedNode.create(cls, calleeName, callable, calleeNode, keywords, passPrimaryAsArgument);
            replace(new ClassMethodCallNode(context, callable.getName(), primaryNode, calleeNode, argumentsNode, keywordsNode, dispatch));
            PArguments.setArgument(arguments, 0, cls);
            return dispatch.executeCall(frame, cls, arguments, keywords);
        }

        if (isPrimaryBoxed(primary)) {
            CallDispatchBoxedNode dispatch = CallDispatchBoxedNode.create((PythonObject) primary, calleeName, callable, calleeNode, keywords, passPrimaryAsArgument);
            replace(new BoxedCallNode(context, callable.getName(), primaryNode, calleeNode, argumentsNode, keywordsNode, dispatch, passPrimaryAsArgument));
            return dispatch.executeCall(frame, (PythonObject) primary, arguments, keywords);
        }

        CallDispatchUnboxedNode dispatch = CallDispatchUnboxedNode.create(primary, callable, calleeNode, keywords);
        replace(new UnboxedCallNode(context, callable.getName(), primaryNode, calleeNode, argumentsNode, keywordsNode, dispatch, passPrimaryAsArgument));
        return dispatch.executeCall(frame, primary, arguments, keywords);
    }

    public static class BoxedCallNode extends PythonCallNode implements InlineableCallNode {

        @Child protected CallDispatchBoxedNode dispatchNode;

        public BoxedCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords, CallDispatchBoxedNode dispatch, boolean passPrimary) {
            super(context, calleeName, primary, callee, arguments, keywords, passPrimary);
            dispatchNode = dispatch;
        }

        public CallDispatchNode getDispatchNode() {
            return dispatchNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary;

            try {
                primary = primaryNode.executePythonObject(frame);
            } catch (UnexpectedResultException e) {
                return rewriteAndExecuteCall(frame, e.getResult(), calleeNode.execute(frame));
            }

            Object[] arguments = argumentsNode.executeArguments(frame, passPrimaryAsTheFirstArgument, primary);
            PKeyword[] keywords = keywordsNode.executeKeywordArguments(frame);
            return dispatchNode.executeCall(frame, primary, arguments, keywords);
        }

        @Override
        public boolean isInlined() {
            return dispatchNode.isInlined();
        }

        public RootNode getInlinedCalleeRoot() {
            assert isInlined();
            assert dispatchNode instanceof LinkedDispatchBoxedNode;
            LinkedDispatchBoxedNode linked = (LinkedDispatchBoxedNode) dispatchNode;
            DirectCallNode direct = linked.getInvokeNode().getDirectCallNode();
            RootNode root = direct.getCurrentRootNode();
            assert root != null;
            return root;
        }
    }

    public static final class ClassMethodCallNode extends BoxedCallNode {

        public ClassMethodCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords, CallDispatchBoxedNode dispatch) {
            super(context, calleeName, primary, callee, arguments, keywords, dispatch, true);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonClass primary;

            try {
                primary = primaryNode.executePythonObject(frame).asPythonClass();
            } catch (UnexpectedResultException e) {
                return rewriteAndExecuteCall(frame, e.getResult(), calleeNode.execute(frame));
            }

            Object[] arguments = argumentsNode.executeArguments(frame, true, primary);
            PKeyword[] keywords = keywordsNode.executeKeywordArguments(frame);
            return dispatchNode.executeCall(frame, primary, arguments, keywords);
        }
    }

    public static final class UnboxedCallNode extends PythonCallNode {

        @Child protected CallDispatchUnboxedNode dispatchNode;

        public UnboxedCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords, CallDispatchUnboxedNode dispatch,
                        boolean passPrimary) {
            super(context, calleeName, primary, callee, arguments, keywords, passPrimary);
            dispatchNode = dispatch;
        }

        public CallDispatchNode getDispatchNode() {
            return dispatchNode;
        }

        @Override
        public boolean isInlined() {
            return dispatchNode.isInlined();
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object primary = primaryNode.execute(frame);
            Object[] arguments = argumentsNode.executeArguments(frame, passPrimaryAsTheFirstArgument, primary);
            PKeyword[] keywords = keywordsNode.executeKeywordArguments(frame);
            return dispatchNode.executeCall(frame, primary, arguments, keywords);
        }
    }

    public static final class NoneCallNode extends PythonCallNode implements InlineableCallNode {

        @Child protected CallDispatchNoneNode dispatchNode;

        public NoneCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords, CallDispatchNoneNode dispatch) {
            super(context, calleeName, primary, callee, arguments, keywords, false);
            this.dispatchNode = dispatch;
        }

        public CallDispatchNode getDispatchNode() {
            return dispatchNode;
        }

        @Override
        public boolean isInlined() {
            return dispatchNode.isInlined();
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonCallable callee;

            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                return rewriteAndExecuteCall(frame, PNone.NONE, e.getResult());
            }

            Object[] arguments = argumentsNode.executeArguments(frame);
            PKeyword[] keywords = keywordsNode.executeKeywordArguments(frame);
            return dispatchNode.executeCall(frame, callee, arguments, keywords);
        }

        public RootNode getInlinedCalleeRoot() {
            assert isInlined();
            assert dispatchNode instanceof LinkedDispatchNoneNode;
            LinkedDispatchNoneNode linked = (LinkedDispatchNoneNode) dispatchNode;
            DirectCallNode direct = linked.getInvokeNode().getDirectCallNode();
            RootNode root = direct.getCurrentRootNode();
            assert root != null;
            return root;
        }
    }

    public static abstract class CallConstructorNode extends PythonCallNode {

        @Child protected NewInstanceNode instanceNode;
        @Child protected CallDispatchBoxedNode dispatchNode;

        protected final PythonClass pythonClass;

        public CallConstructorNode(PythonContext context, PythonClass pythonClass, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords, CallDispatchBoxedNode dispatch) {
            super(context, pythonClass.getName(), primary, callee, arguments, keywords, true);
            dispatchNode = dispatch;
            instanceNode = NewInstanceNode.create(pythonClass);
            this.pythonClass = pythonClass;
        }

        public CallDispatchNode getDispatchNode() {
            return dispatchNode;
        }

        @Override
        public boolean isInlined() {
            return dispatchNode.isInlined();
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary;

            try {
                primary = primaryNode.executePythonObject(frame);
            } catch (UnexpectedResultException e) {
                return rewriteAndExecuteCall(frame, e.getResult(), calleeNode.execute(frame));
            }

            PythonClass callee;

            try {
                callee = calleeNode.executePythonClass(frame);
            } catch (UnexpectedResultException e) {
                return rewriteAndExecuteCall(frame, primary, e.getResult());
            }

            return executeCall(frame, primary, callee);
        }

        protected abstract Object executeCall(VirtualFrame frame, PythonObject primary, PythonClass clazz);
    }

    public static final class CallConstructorBootstrappingNode extends CallConstructorNode {

        public CallConstructorBootstrappingNode(PythonContext context, PythonClass pythonClass, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords,
                        CallDispatchBoxedNode dispatch) {
            super(context, pythonClass, primary, callee, arguments, keywords, dispatch);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonObject primary, PythonClass clazz) {
            PythonObject newInstance = instanceNode.createNewInstance(clazz);
            Object[] arguments = argumentsNode.executeArguments(frame, true, newInstance);
            PKeyword[] keywords = keywordsNode.executeKeywordArguments(frame);
            dispatchNode.executeCall(frame, primary, arguments, keywords);

            // Switch to generated object storage.
            clazz.switchToGeneratedStorageClass();
            this.replace(new CallConstructorFastNode(context, pythonClass, primaryNode, calleeNode, argumentsNode, keywordsNode, dispatchNode));

            // Instantiate and migrate to the generated object storage.
            PythonObject generatedInstance = instanceNode.createNewInstance(clazz);
            newInstance.migrateTo(generatedInstance);

            return generatedInstance;
        }
    }

    public static final class CallConstructorFastNode extends CallConstructorNode {

        public CallConstructorFastNode(PythonContext context, PythonClass pythonClass, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords, CallDispatchBoxedNode dispatch) {
            super(context, pythonClass, primary, callee, arguments, keywords, dispatch);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonObject primary, PythonClass clazz) {
            PythonObject newInstance = instanceNode.createNewInstance(clazz);
            Object[] arguments = argumentsNode.executeArguments(frame, true, newInstance);
            PKeyword[] keywords = keywordsNode.executeKeywordArguments(frame);
            dispatchNode.executeCall(frame, primary, arguments, keywords);
            return newInstance;
        }
    }

    public static final class PythonObjectCallNode extends PythonCallNode {

        @Child protected CallDispatchBoxedNode dispatchNode;

        public PythonObjectCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords, CallDispatchBoxedNode dispatch) {
            super(context, calleeName, primary, callee, arguments, keywords, true);
            dispatchNode = dispatch;
        }

        @Override
        public boolean isInlined() {
            return dispatchNode.isInlined();
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary;

            try {
                primary = calleeNode.executePythonObject(frame);
            } catch (UnexpectedResultException e) {
                return rewriteAndExecuteCall(frame, primaryNode.execute(frame), e.getResult());
            }

            Object[] arguments = argumentsNode.executeArguments(frame, passPrimaryAsTheFirstArgument, primary);
            PKeyword[] keywords = keywordsNode.executeKeywordArguments(frame);
            return dispatchNode.executeCall(frame, primary, arguments, keywords);
        }
    }

    public static final class JythonCallNode extends PythonCallNode {

        public JythonCallNode(PythonContext context, String calleeName, PNode primary, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords) {
            super(context, calleeName, primary, callee, arguments, keywords, false);
        }

        @Override
        public boolean isInlined() {
            return false;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PyObject callee;

            try {
                callee = calleeNode.executePyObject(frame);
            } catch (UnexpectedResultException e) {
                return rewriteAndExecuteCall(frame, PNone.NONE, e.getResult());
            }

            return executeCall(frame, callee);
        }

        private Object executeCall(VirtualFrame frame, PyObject callee) {
            Object[] arguments = argumentsNode.executeArgumentsForJython(frame);
            PyObject[] pyargs = adaptToPyObjects(arguments);
            return unboxPyObject(callee.__call__(pyargs));
        }
    }

    public static final class UninitializedCallNode extends PythonCallNode {

        public UninitializedCallNode(PythonContext context, PNode primary, String calleeName, PNode callee, ArgumentsNode arguments, ArgumentsNode keywords) {
            super(context, calleeName, primary, callee, arguments, keywords, false);
            this.calleeNode = callee;
        }

        public PNode getCallee() {
            return calleeNode;
        }

        @Override
        public boolean isInlined() {
            return false;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object primary = primaryNode.execute(frame);
            return rewriteAndExecuteCall(frame, primary, calleeNode.execute(frame));
        }
    }

    public interface InlineableCallNode {

        RootNode getInlinedCalleeRoot();

    }

}
