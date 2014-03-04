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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.function.*;

public class CallFunctionNoKeywordNode extends PNode {

    @Child protected PNode callee;
    @Children protected final PNode[] arguments;

    public CallFunctionNoKeywordNode(PNode callee, PNode[] arguments) {
        this.callee = adoptChild(callee);
        this.arguments = adoptChildren(arguments);
    }

    public PNode[] getArguments() {
        return arguments;
    }

    public static CallFunctionNoKeywordNode create(PNode callee, PNode[] argumentNodes, PythonCallable callable, PythonContext context) {
        /**
         * Any non global scope callable lookup is not optimized.
         */
        if (!(callee instanceof ReadGlobalScopeNode)) {
            return new CallFunctionNoKeywordNode(callee, argumentNodes);
        }

        ReadGlobalScopeNode calleeNode = (ReadGlobalScopeNode) callee;

        if (callable instanceof PGeneratorFunction) {
            return createGeneratorCall((PGeneratorFunction) callable, calleeNode, argumentNodes);
        } else if (callable instanceof PFunction) {
            return createFunctionCall((PFunction) callable, calleeNode, argumentNodes, context);
        } else if (callable instanceof PBuiltinFunction) {
            return createBuiltinCall((PBuiltinFunction) callable, calleeNode, argumentNodes, context);
        } else if (callable instanceof PythonBuiltinClass) {
            /**
             * Built-in class constructor
             */
            PBuiltinFunction function = (PBuiltinFunction) ((PythonBuiltinClass) callable).getAttribute("__init__");
            return createBuiltinCall(function, calleeNode, argumentNodes, context);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static CallFunctionNoKeywordNode createFunctionCall(PFunction function, ReadGlobalScopeNode calleeNode, PNode[] argumentNodes, PythonContext context) {
        Assumption globalScopeUnchanged = calleeNode.getGlobaScope().getUnmodifiedAssumption();

        if (PythonOptions.InlineFunctionCalls) {
            return new InlineableCallNode.CallFunctionInlinableNode(calleeNode, argumentNodes, function, context, globalScopeUnchanged);
        } else {
            return new CallFunctionCachedNode(calleeNode, argumentNodes, function, globalScopeUnchanged);
        }
    }

    private static CallFunctionNoKeywordNode createBuiltinCall(PBuiltinFunction function, ReadGlobalScopeNode calleeNode, PNode[] argumentNodes, PythonContext context) {
        Assumption globalScopeUnchanged = calleeNode.getGlobaScope().getUnmodifiedAssumption();
        Assumption builtinsModuleUnchanged = context.getPythonBuiltinsLookup().lookupModule("__builtins__").getUnmodifiedAssumption();

        if (PythonOptions.InlineBuiltinFunctionCalls) {
            return new InlineableCallNode.CallBuiltinInlinableNode(calleeNode, argumentNodes, function.duplicate(), context, globalScopeUnchanged, builtinsModuleUnchanged);
        } else {
            return new CallFunctionNoKeywordNode(calleeNode, argumentNodes);
        }
    }

    private static CallFunctionNoKeywordNode createGeneratorCall(PGeneratorFunction generator, ReadGlobalScopeNode calleeNode, PNode[] argumentNodes) {
        Assumption globalScopeUnchanged = calleeNode.getGlobaScope().getUnmodifiedAssumption();

        if (PythonOptions.InlineGeneratorCalls) {
            return new CallGeneratorNode(calleeNode, argumentNodes, generator, globalScopeUnchanged);
        } else {
            return new CallFunctionCachedNode(calleeNode, argumentNodes, generator, globalScopeUnchanged);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object calleeResult = callee.execute(frame);
        if (calleeResult instanceof PythonCallable) {
            return executeCall(frame, (PythonCallable) calleeResult);
        } else {
            return calleeResult.toString();
        }
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
    public static class CallFunctionCachedNode extends CallFunctionNoKeywordNode {

        protected final PythonCallable cached;
        protected final Assumption globalScopeUnchanged;

        public CallFunctionCachedNode(PNode callee, PNode[] arguments, PythonCallable cached, Assumption globalScopeUnchanged) {
            super(callee, arguments);
            this.cached = cached;
            this.globalScopeUnchanged = globalScopeUnchanged;
        }

        public PythonCallable getCallee() {
            return cached;
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

}
