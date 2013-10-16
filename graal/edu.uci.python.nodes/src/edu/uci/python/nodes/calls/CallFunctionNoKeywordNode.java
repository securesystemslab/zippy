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
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.datatypes.*;

public class CallFunctionNoKeywordNode extends PNode {

    @Child protected PNode callee;

    @Children protected final PNode[] arguments;

    public CallFunctionNoKeywordNode(PNode callee, PNode[] arguments) {
        this.callee = adoptChild(callee);
        this.arguments = adoptChildren(arguments);
    }

    public static CallFunctionNoKeywordNode create(PNode calleeNode, PNode[] argumentNodes, PCallable callable) {
        if (calleeNode instanceof ReadGlobalScopeNode) {
            Assumption globalScopeUnchanged = ((ReadGlobalScopeNode) calleeNode).getGlobaScope().getUnmodifiedAssumption();
            return new CallFunctionNoKeywordNode.CallFunctionNoKeywordCachedNode(calleeNode, argumentNodes, callable, globalScopeUnchanged);
        } else {
            return new CallFunctionNoKeywordNode(calleeNode, argumentNodes);
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

    public Object executeCall(VirtualFrame frame, PCallable callable) {
        final Object[] args = CallFunctionNode.executeArguments(frame, arguments);
        return callable.call(frame.pack(), args);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final PCallable callable = (PCallable) callee.execute(frame);
        return executeCall(frame, callable);
    }

    @SlowPath
    protected Object uninitialize(VirtualFrame frame) {
        return replace(new CallFunctionNoKeywordNode(this.callee, this.arguments)).execute(frame);
    }

    /**
     * The callee node of a cached call function node should not be local accessor node, since we
     * don't make assumption about local variables.
     * 
     */
    public static class CallFunctionNoKeywordCachedNode extends CallFunctionNoKeywordNode {

        private final PCallable cached;

        private final Assumption globalScopeUnchanged;

        public CallFunctionNoKeywordCachedNode(PNode callee, PNode[] arguments, PCallable cached, Assumption globalScopeUnchanged) {
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
}
