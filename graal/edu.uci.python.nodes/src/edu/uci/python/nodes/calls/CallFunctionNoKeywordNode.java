/*
 * Copyright (c) 2013, Regents of the University of California
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
package edu.uci.python.nodes.calls;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.objects.*;

public class CallFunctionNoKeywordNode extends PNode {

    @Child protected PNode callee;

    @Children protected final PNode[] arguments;

    public CallFunctionNoKeywordNode(PNode callee, PNode[] arguments) {
        this.callee = adoptChild(callee);
        this.arguments = adoptChildren(arguments);
    }

    public static CallFunctionNoKeywordNode create(PNode calleeNode, PNode[] argumentNodes, PythonCallable callable, PythonContext context) {
        if (callable instanceof PythonBasicObject) {
            return new CallFunctionNoKeywordNode(calleeNode, argumentNodes);
        }

        if (calleeNode instanceof ReadGlobalScopeNode) {
            Assumption globalScopeUnchanged = ((ReadGlobalScopeNode) calleeNode).getGlobaScope().getUnmodifiedAssumption();
            Assumption builtinsModuleUnchanged = context.getPythonBuiltinsLookup().lookupModule("__builtins__").getUnmodifiedAssumption();

            if (callable instanceof PFunction) {
                PFunction function = (PFunction) callable;
                if (PythonOptions.InlineFunctionCalls) {
                    return new CallFunctionNoKeywordInlinableNode(calleeNode, argumentNodes, function, globalScopeUnchanged);
                } else {
                    return new CallFunctionNoKeywordCachedNode(calleeNode, argumentNodes, function, globalScopeUnchanged);
                }
            } else {
                PBuiltinFunction function = (PBuiltinFunction) callable;
                if (PythonOptions.InlineBuiltinFunctionCalls) {
                    return new CallBuiltinFunctionNokeywordInlinableNode(calleeNode, argumentNodes, function.duplicate(), globalScopeUnchanged, builtinsModuleUnchanged);
                } else {
                    return new CallFunctionNoKeywordNode(calleeNode, argumentNodes);
                }
            }
        } else {
            return new CallFunctionNoKeywordNode(calleeNode, argumentNodes);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final PythonCallable callable = (PythonCallable) callee.execute(frame);
        return executeCall(frame, callable);
    }

    @Override
    public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectInteger(execute(frame));
    }

    @Override
    public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectDouble(execute(frame));
    }

    public Object executeCall(VirtualFrame frame, PythonCallable callable) {
        final Object[] args = CallFunctionNode.executeArguments(frame, arguments);
        return callable.call(frame.pack(), args);
    }

    @SlowPath
    protected Object uninitialize(VirtualFrame frame) {
        CompilerDirectives.transferToInterpreter();
        return replace(new CallFunctionNoKeywordNode(this.callee, this.arguments)).execute(frame);
    }

    /**
     * The callee node of a cached call function node should not be local accessor node, since we
     * don't make assumption about local variables.
     */
    public static class CallFunctionNoKeywordCachedNode extends CallFunctionNoKeywordNode {

        protected final PythonCallable cached;
        protected final Assumption globalScopeUnchanged;

        public CallFunctionNoKeywordCachedNode(PNode callee, PNode[] arguments, PythonCallable cached, Assumption globalScopeUnchanged) {
            super(callee, arguments);
            this.cached = cached;
            this.globalScopeUnchanged = globalScopeUnchanged;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                globalScopeUnchanged.check();
            } catch (InvalidAssumptionException e) {
                return uninitialize(frame);
            }

            return executeCall(frame, cached);
        }
    }

    public static class CallFunctionNoKeywordInlinableNode extends CallFunctionNoKeywordNode implements InlinableCallSite {

        private final PFunction function;
        private final FunctionRootNode functionRoot;
        private final Assumption globalScopeUnchanged;
        @CompilationFinal private int callCount;

        public CallFunctionNoKeywordInlinableNode(PNode callee, PNode[] arguments, PFunction function, Assumption globalScopeUnchanged) {
            super(callee, arguments);
            this.function = function;
            this.globalScopeUnchanged = globalScopeUnchanged;
            functionRoot = (FunctionRootNode) function.getFunctionRootNode();
        }

        public int getCallCount() {
            return callCount;
        }

        public void resetCallCount() {
            callCount = 0;
        }

        public Node getInlineTree() {
            return functionRoot.getInlinedRootNode();
        }

        public CallTarget getCallTarget() {
            return function.getCallTarget();
        }

        public boolean inline(FrameFactory factory) {
            CompilerAsserts.neverPartOfCompilation();

            if (functionRoot != null) {
                CallFunctionNoKeywordNode inlinedCallNode = new CallFunctionNoKeywordInlinedNode(this.callee, this.arguments, this.function, this.globalScopeUnchanged, this.functionRoot, factory);
                replace(inlinedCallNode);
                return true;
            }

            return false;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            if (CompilerDirectives.inInterpreter()) {
                callCount++;
            }
            return super.execute(frame);
        }
    }

    public static class CallBuiltinFunctionNokeywordInlinableNode extends CallFunctionNoKeywordNode implements InlinableCallSite {

        private final PBuiltinFunction function;
        private final BuiltinFunctionRootNode functionRoot;
        private final Assumption globalScopeUnchanged;
        private final Assumption builtinModuleUnchanged;
        @CompilationFinal private int callCount;

        public CallBuiltinFunctionNokeywordInlinableNode(PNode callee, PNode[] arguments, PBuiltinFunction function, Assumption globalScopeUnchanged, Assumption builtinModuleUnchanged) {
            super(callee, arguments);
            this.function = function;
            this.functionRoot = (BuiltinFunctionRootNode) function.getFunctionRootNode();
            this.globalScopeUnchanged = globalScopeUnchanged;
            this.builtinModuleUnchanged = builtinModuleUnchanged;
            this.callCount = 0;
        }

        public int getCallCount() {
            return callCount;
        }

        public void resetCallCount() {
            callCount = 0;
        }

        public Node getInlineTree() {
            return functionRoot.copy();
        }

        public CallTarget getCallTarget() {
            return function.getCallTarget();
        }

        public boolean inline(FrameFactory factory) {
            CompilerAsserts.neverPartOfCompilation();

            if (functionRoot != null) {
                CallFunctionNoKeywordNode inlinedCallNode = new CallBuiltinFunctionNoKeywordInlinedNode(this.callee, this.arguments, this.function, this.functionRoot, this.globalScopeUnchanged,
                                this.builtinModuleUnchanged, factory);
                replace(inlinedCallNode);
                return true;
            }

            return false;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            if (CompilerDirectives.inInterpreter()) {
                callCount++;
            }
            return super.execute(frame);
        }

        /**
         * Invoke the copied built-in function instead of the original one. This way makes sure that
         * every call site's bootstrapping does not depend on a previous call to the same built-in
         * function.
         */
        @Override
        public Object executeCall(VirtualFrame frame, PythonCallable callable) {
            final Object[] args = CallFunctionNode.executeArguments(frame, arguments);
            return function.call(frame.pack(), args);
        }
    }
}
