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

import edu.uci.python.nodes.*;
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

    protected abstract Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments);

    protected static CallDispatchBoxedNode create(PythonContext context, PythonBasicObject primary, PythonCallable callee, PNode calleeNode) {
        UninitializedDispatchBoxedNode next = new UninitializedDispatchBoxedNode(context, callee.getName(), calleeNode);
        /**
         * Treat generator as slow path for now.
         */
        if (callee instanceof PGeneratorFunction) {
            return new GenericDispatchBoxedNode(callee.getName(), calleeNode);
        }

        if (callee instanceof PFunction) {
            return new DispatchGlobalFunctionNode(primary, (PFunction) callee, next);
        } else if (callee instanceof PBuiltinFunction) {
            return new DispatchBuiltinFunctionNode((PBuiltinFunction) callee, next);
        } else if (callee instanceof PMethod) {
            return new GenericDispatchBoxedNode(callee.getName(), calleeNode);
        } else if (callee instanceof PythonBuiltinClass) {
            return new DispatchConstructorBoxedNode((PythonBuiltinClass) callee, next);
        }

        throw new UnsupportedOperationException("Unsupported callee type " + callee);
    }

    /**
     * The primary is the global module.
     *
     */
    public static final class DispatchGlobalFunctionNode extends CallDispatchBoxedNode {

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        private final Assumption cachedCallTargetStable;
        private final MaterializedFrame declarationFrame;

        public DispatchGlobalFunctionNode(PythonBasicObject primary, PFunction callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            callNode = Truffle.getRuntime().createCallNode(callee.getCallTarget());
            nextNode = next;
            cachedCallTargetStable = primary.getStableAssumption();
            declarationFrame = callee.getDeclarationFrame();
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

    /**
     * The primary is the global module.
     *
     */
    public static final class DispatchBuiltinFunctionNode extends CallDispatchBoxedNode {

        protected final PBuiltinFunction cachedCallee;
        protected final Assumption cachedCallTargetStable;

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        public DispatchBuiltinFunctionNode(PBuiltinFunction callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            cachedCallee = callee;
            // TODO: replace holder for now.
            cachedCallTargetStable = AlwaysValidAssumption.INSTANCE;
            callNode = Truffle.getRuntime().createCallNode(callee.getCallTarget());
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

    /**
     * The primary is a {@link PythonBasicObject}
     *
     */
    public static final class DispatchMethodBoxedNode extends CallDispatchBoxedNode {

        protected final PMethod cachedCallee;
        protected final Assumption cachedCallTargetStable;
        private final MaterializedFrame declarationFrame;

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        public DispatchMethodBoxedNode(PMethod callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            cachedCallee = callee;
            declarationFrame = callee.__func__().getDeclarationFrame();
            // TODO: replace holder for now.
            cachedCallTargetStable = AlwaysValidAssumption.INSTANCE;
            callNode = Truffle.getRuntime().createCallNode(callee.getCallTarget());
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

    public static final class DispatchConstructorBoxedNode extends CallDispatchBoxedNode {

        protected final PythonBuiltinClass cachedCallee;
        protected final Assumption cachedCallTargetStable;

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        public DispatchConstructorBoxedNode(PythonBuiltinClass callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            cachedCallee = callee;
            PythonCallable constructor = callee.lookUpMethod("__init__");
            // TODO: PythonBuiltinClass should return always valid assumption.
            cachedCallTargetStable = callee.getStableAssumption();

            callNode = Truffle.getRuntime().createCallNode(split(constructor.getCallTarget()));
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

    public static final class UninitializedDispatchBoxedNode extends CallDispatchBoxedNode {

        @Child protected PNode calleeNode;
        private final PythonContext context;

        public UninitializedDispatchBoxedNode(PythonContext context, String calleeName, PNode calleeNode) {
            super(calleeName);
            this.context = context;
            this.calleeNode = calleeNode;
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

            CallDispatchBoxedNode specialized;
            if (depth < PythonOptions.CallSiteInlineCacheMaxDepth) {
                PythonCallable callee;
                try {
                    callee = PythonTypesGen.PYTHONTYPES.expectPythonCallable(primaryObj.getAttribute(calleeName));
                } catch (UnexpectedResultException e) {
                    throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
                }

                CallDispatchBoxedNode direct = create(context, primaryObj, callee, calleeNode);
                specialized = replace(direct);
            } else {
                CallDispatchBoxedNode generic = new GenericDispatchBoxedNode(calleeName, calleeNode);
                specialized = current.replace(generic);
            }

            return specialized.executeCall(frame, primaryObj, arguments);
        }
    }

    public static final class GenericDispatchBoxedNode extends CallDispatchBoxedNode {

        @Child protected PNode calleeNode;

        public GenericDispatchBoxedNode(String calleeName, PNode calleeNode) {
            super(calleeName);
            this.calleeNode = calleeNode;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            PythonCallable callee;

            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
            }

            return callee.call(frame.pack(), arguments);
        }
    }

}
