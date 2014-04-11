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
import com.oracle.truffle.api.utilities.*;

import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;

public abstract class CallDispatchBoxedNode extends CallDispatchNode {

    public CallDispatchBoxedNode(String calleeName) {
        super(calleeName);
    }

    @Override
    protected Object executeCall(VirtualFrame frame, Object primaryObj, Object... arguments) {
        throw new UnsupportedOperationException();
    }

    protected abstract Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments);

    protected static CallDispatchBoxedNode create(PythonCallable callee, CallDispatchBoxedNode.UninitializedDispatchNode next) {
        /**
         * Treat generator as slow path for now.
         */
        if (callee instanceof PGeneratorFunction) {
            return new GenericDispatchNode(callee.getName());
        }

        if (callee instanceof PFunction) {
            return new DispatchFunctionNode((PFunction) callee, next);
        } else if (callee instanceof PMethod) {
            return new GenericDispatchNode(callee.getName());
        } else if (callee instanceof PythonBuiltinClass) {
            return new DispatchConstructorNode((PythonBuiltinClass) callee, next);
        }

        throw new UnsupportedOperationException("Unsupported callee type " + callee);
    }

    public static final class DispatchFunctionNode extends CallDispatchBoxedNode {

        protected final CallTarget cachedCallTarget;
        protected final Assumption cachedCallTargetStable;
        private final MaterializedFrame declarationFrame;

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        public DispatchFunctionNode(PFunction callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            cachedCallTarget = callee.getCallTarget();
            declarationFrame = callee.getDeclarationFrame();
            // TODO: replace holder for now.
            cachedCallTargetStable = AlwaysValidAssumption.INSTANCE;
            callNode = Truffle.getRuntime().createCallNode(cachedCallTarget);
            nextNode = next;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            try {
                cachedCallTargetStable.check();

                PArguments arg = new PArguments(null, declarationFrame, arguments);
                return callNode.call(frame.pack(), arg);
            } catch (InvalidAssumptionException ex) {
                replace(nextNode);
                return nextNode.executeCall(frame, primaryObj, arguments);
            }
        }
    }

    public static final class DispatchMethodNode extends CallDispatchBoxedNode {

        protected final PMethod cachedCallee;
        protected final CallTarget cachedCallTarget;
        protected final Assumption cachedCallTargetStable;
        private final MaterializedFrame declarationFrame;

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        public DispatchMethodNode(PMethod callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            cachedCallee = callee;
            cachedCallTarget = callee.getCallTarget();
            declarationFrame = callee.__func__().getDeclarationFrame();
            // TODO: replace holder for now.
            cachedCallTargetStable = AlwaysValidAssumption.INSTANCE;
            callNode = Truffle.getRuntime().createCallNode(cachedCallTarget);
            nextNode = next;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            try {
                cachedCallTargetStable.check();

                PArguments arg = new PArguments(cachedCallee.__self__(), declarationFrame, arguments);
                return callNode.call(frame.pack(), arg);
            } catch (InvalidAssumptionException ex) {
                replace(nextNode);
                return nextNode.executeCall(frame, primaryObj, arguments);
            }
        }
    }

    public static final class DispatchConstructorNode extends CallDispatchBoxedNode {

        protected final PythonBuiltinClass cachedCallee;
        protected final CallTarget cachedCallTarget;
        protected final Assumption cachedCallTargetStable;

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        public DispatchConstructorNode(PythonBuiltinClass callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            cachedCallee = callee;
            PythonCallable constructor = callee.lookUpMethod("__init__");
            cachedCallTarget = split(constructor.getCallTarget());
            // TODO: PythonBuiltinClass should return always valid assumption.
            cachedCallTargetStable = callee.getStableAssumption();

            callNode = Truffle.getRuntime().createCallNode(cachedCallTarget);
            nextNode = next;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            try {
                cachedCallTargetStable.check();

                PArguments arg = new PArguments(PNone.NONE, null, arguments);
                return callNode.call(frame.pack(), arg);
            } catch (InvalidAssumptionException ex) {
                replace(nextNode);
                return nextNode.executeCall(frame, primaryObj, arguments);
            }
        }
    }

    public static final class UninitializedDispatchNode extends CallDispatchBoxedNode {

        public UninitializedDispatchNode(String calleeName) {
            super(calleeName);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            CallDispatchNode current = this;
            int depth = 0;
            while (current.getParent() instanceof CallDispatchNode) {
                current = (CallDispatchNode) current.getParent();
                depth++;
            }

            CallDispatchNode specialized;
            if (depth < PythonOptions.CallSiteInlineCacheMaxDepth) {
                PythonCallable callee;
                try {
                    callee = PythonTypesGen.PYTHONTYPES.expectPythonCallable(primaryObj.getAttribute(calleeName));
                } catch (UnexpectedResultException e) {
                    throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
                }

                UninitializedDispatchNode next = new UninitializedDispatchNode(callee.getName());
                CallDispatchNode direct = create(callee, next);
                specialized = replace(direct);
            } else {
                CallDispatchNode generic = new GenericDispatchNode(calleeName);
                // TODO: should replace the dispatch node of the parent call node.
                specialized = replace(generic);
            }

            return specialized.executeCall(frame, primaryObj, arguments);
        }
    }

    public static final class GenericDispatchNode extends CallDispatchBoxedNode {

        public GenericDispatchNode(String calleeName) {
            super(calleeName);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            PythonCallable callee;

            try {
                callee = PythonTypesGen.PYTHONTYPES.expectPythonCallable(primaryObj.getAttribute(calleeName));
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
            }

            return callee.call(frame.pack(), arguments);
        }
    }

}
