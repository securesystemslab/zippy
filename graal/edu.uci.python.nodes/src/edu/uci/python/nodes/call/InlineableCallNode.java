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
package edu.uci.python.nodes.call;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.optimize.*;
import edu.uci.python.profiler.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;

public abstract class InlineableCallNode extends CallFunctionNoKeywordNode implements InlinableCallSite {

    protected final PythonContext context;
    protected final Assumption globalScopeUnchanged;
    protected final Assumption builtinModuleUnchanged;
    @CompilationFinal protected int callCount;

    public InlineableCallNode(PNode callee, PNode[] arguments, PythonContext context, Assumption globalScopeUnchanged, Assumption builtinModuleUnchanged) {
        super(callee, arguments);
        this.context = context;
        this.globalScopeUnchanged = globalScopeUnchanged;
        this.builtinModuleUnchanged = builtinModuleUnchanged;
    }

    public abstract PythonCallable getCallee();

    public int getCallCount() {
        return callCount;
    }

    public void resetCallCount() {
        callCount = 0;
    }

    public void invokeGeneratorExpressionOptimizer() {
        if (!PythonOptions.OptimizeGeneratorExpressions) {
            return;
        }

        RootNode current = getRootNode();
        assert current != null;
        new GeneratorExpressionOptimizer((FunctionRootNode) current).optimize();
    }

    public void invokeBuiltinIntrinsifier(CallBuiltinInlinedNode inlinedCall) {
        if (!PythonOptions.IntrinsifyBuiltinCalls) {
            return;
        }

        new BuiltinIntrinsifier(context, globalScopeUnchanged, builtinModuleUnchanged, inlinedCall).intrinsify();
    }

    public static class CallFunctionInlinableNode extends InlineableCallNode {

        private final PFunction function;
        private final FunctionRootNode functionRoot;

        public CallFunctionInlinableNode(PNode callee, PNode[] arguments, PFunction function, PythonContext context, Assumption globalScopeUnchanged) {
            super(callee, arguments, context, globalScopeUnchanged, AlwaysValidAssumption.INSTANCE);
            this.function = function;
            functionRoot = (FunctionRootNode) function.getFunctionRootNode();
        }

        @Override
        public PFunction getCallee() {
            return function;
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
                CallFunctionNoKeywordNode inlinedCallNode = new CallFunctionInlinedNode(callee, arguments, function, globalScopeUnchanged, functionRoot, factory);
                replace(inlinedCallNode);
                invokeGeneratorExpressionOptimizer();
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

    public static class CallBuiltinInlinableNode extends InlineableCallNode {

        private final PBuiltinFunction function;
        private final BuiltinFunctionRootNode functionRoot;

        public CallBuiltinInlinableNode(PNode callee, PNode[] arguments, PBuiltinFunction function, PythonContext context, Assumption globalScopeUnchanged, Assumption builtinModuleUnchanged) {
            super(callee, arguments, context, globalScopeUnchanged, builtinModuleUnchanged);
            this.function = function;
            this.functionRoot = (BuiltinFunctionRootNode) function.getFunctionRootNode();
        }

        @Override
        public PBuiltinFunction getCallee() {
            return function;
        }

        public Node getInlineTree() {
            return functionRoot;
        }

        public CallTarget getCallTarget() {
            return function.getCallTarget();
        }

        public boolean inline(FrameFactory factory) {
            CompilerAsserts.neverPartOfCompilation();

            if (functionRoot != null) {
                CallBuiltinInlinedNode inlinedCallNode = new CallBuiltinInlinedNode(this.callee, this.arguments, this.function, this.functionRoot, this.globalScopeUnchanged,
                                this.builtinModuleUnchanged, factory);
                replace(inlinedCallNode);
                invokeBuiltinIntrinsifier(inlinedCallNode);
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

            if (PythonOptions.ProfileFunctionCalls) {
                Profiler.getInstance().increment(callable.getCallableName());
            }

            if (PythonOptions.ProfileCallSites) {
                callSiteProfiler.executeWithCallableName(frame, function.getFunctionRootNode());
            }

            return function.call(frame.pack(), args);
        }
    }

}
