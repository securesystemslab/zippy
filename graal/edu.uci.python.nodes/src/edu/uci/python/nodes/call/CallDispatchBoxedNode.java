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
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class CallDispatchBoxedNode extends CallDispatchNode {

    public CallDispatchBoxedNode(String calleeName) {
        super(calleeName);
    }

    protected abstract Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments);

    protected final Object executeCallAndRewrite(CallDispatchBoxedNode next, VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
        replace(next);
        return next.executeCall(frame, primaryObj, arguments);
    }

    protected static CallDispatchBoxedNode create(PythonContext context, PythonBasicObject primary, PythonCallable callee, PNode calleeNode) {
        UninitializedDispatchBoxedNode next = new UninitializedDispatchBoxedNode(context, callee.getName(), calleeNode);
        /**
         * Treat generator as slow path for now.
         */
        if (callee instanceof PGeneratorFunction) {
            return new GenericDispatchBoxedNode(callee.getName(), calleeNode);
        }

        if (callee instanceof PFunction) {
            return new DispatchFunctionNode(primary, (PFunction) callee, next);
        } else if (callee instanceof PBuiltinFunction) {
            return new DispatchBuiltinFunctionNode(primary, (PBuiltinFunction) callee, next);
        } else if (callee instanceof PythonBuiltinClass && primary instanceof PythonModule) {
            return new DispatchBuiltinConstructorNode(primary, (PythonBuiltinClass) callee, next);
        } else if (callee instanceof PMethod) {
            return new DispatchMethodNode(primary, (PMethod) callee, next);
        }

        throw new UnsupportedOperationException("Unsupported callee type " + callee);
    }

    /**
     * The primary could be:
     * <p>
     * 1. The global {@link PythonModule}. <br>
     * 3. A {@link PythonModule}. <br>
     * 2. A {@link PythonClass}.
     *
     */
    public static final class DispatchFunctionNode extends CallDispatchBoxedNode {

        @Child protected InvokeNode invokeNode;
        @Child protected CallDispatchBoxedNode nextNode;

        private final PythonBasicObject cachedPrimary;
        private final Assumption dispatchStable;

        public DispatchFunctionNode(PythonBasicObject primary, PFunction callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            nextNode = next;
            invokeNode = InvokeNode.create(callee);
            cachedPrimary = primary;
            dispatchStable = primary.getStableAssumption();
            assert primary instanceof PythonModule || primary instanceof PythonClass;
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            if (primaryObj == cachedPrimary) {
                try {
                    dispatchStable.check();
                    return invokeNode.invoke(frame, primaryObj, arguments);
                } catch (InvalidAssumptionException ex) {
                    return executeCallAndRewrite(nextNode, frame, primaryObj, arguments);
                }
            }

            return nextNode.executeCall(frame, primaryObj, arguments);
        }
    }

    /**
     * The primary could be:
     * <p>
     * 1. The global {@link PythonModule}. <br>
     * 2. A built-in {@link PythonModule}. <br>
     * 3. A built-in {@link PythonBuiltinClass}.
     *
     */
    public static final class DispatchBuiltinFunctionNode extends CallDispatchBoxedNode {

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        private final PythonBasicObject cachedPrimary;
        private final Assumption dispatchStable;

        public DispatchBuiltinFunctionNode(PythonBasicObject primary, PBuiltinFunction callee, UninitializedDispatchBoxedNode next) {
            super(callee.getName());
            callNode = Truffle.getRuntime().createCallNode(callee.getCallTarget());
            nextNode = next;
            cachedPrimary = primary;

            if (primary instanceof PythonModule) {
                Assumption globalStable = primary.getStableAssumption();
                Assumption builtinStable = next.context.getBuiltins().getStableAssumption();
                dispatchStable = new UnionAssumption("global and builtin", globalStable, builtinStable);
            } else if (primary instanceof PythonBuiltinClass) {
                dispatchStable = AlwaysValidAssumption.INSTANCE;
            } else {
                throw new IllegalStateException("Unsupported primary type " + primary);
            }
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            if (primaryObj == cachedPrimary) {
                try {
                    dispatchStable.check();

                    PArguments arg = new PArguments(PNone.NONE, null, arguments);
                    return callNode.call(frame.pack(), arg);
                } catch (InvalidAssumptionException ex) {
                    return executeCallAndRewrite(nextNode, frame, primaryObj, arguments);
                }
            }

            return nextNode.executeCall(frame, primaryObj, arguments);
        }
    }

    /**
     * The primary could be:
     * <p>
     * 1. The global {@link PythonModule}. <br>
     * 2. The built-in {@link PythonModule}. <br>
     */
    public static final class DispatchBuiltinConstructorNode extends CallDispatchBoxedNode {

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        private final PythonBasicObject cachedPrimary;
        private final Assumption dispatchStable;

        public DispatchBuiltinConstructorNode(PythonBasicObject primary, PythonBuiltinClass callee, UninitializedDispatchBoxedNode next) {
            super(callee.getName());
            PythonCallable constructor = callee.lookUpMethod("__init__");
            callNode = Truffle.getRuntime().createCallNode(split(constructor.getCallTarget()));
            nextNode = next;

            assert primary instanceof PythonModule;
            cachedPrimary = primary;
            if (primary.equals(next.context.getBuiltins())) {
                dispatchStable = primary.getStableAssumption();
            } else {
                Assumption globalStable = primary.getStableAssumption();
                Assumption builtinStable = next.context.getBuiltins().getStableAssumption();
                dispatchStable = new UnionAssumption("global and builtin", globalStable, builtinStable);
            }
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            if (cachedPrimary == primaryObj) {
                try {
                    dispatchStable.check();

                    PArguments arg = new PArguments(PNone.NONE, null, arguments);
                    return callNode.call(frame.pack(), arg);
                } catch (InvalidAssumptionException ex) {
                    return executeCallAndRewrite(nextNode, frame, primaryObj, arguments);
                }
            }

            return nextNode.executeCall(frame, primaryObj, arguments);
        }
    }

    /**
     * The primary is a {@link PythonObject}
     *
     */
    public static final class DispatchMethodNode extends CallDispatchBoxedNode {

        @Child protected CallNode callNode;
        @Child protected CallDispatchBoxedNode nextNode;

        private final PythonClass cachedClass;
        private final Assumption dispatchStable;
        private final MaterializedFrame declarationFrame;

        public DispatchMethodNode(PythonBasicObject primary, PMethod callee, CallDispatchBoxedNode next) {
            super(callee.getName());
            callNode = Truffle.getRuntime().createCallNode(callee.getCallTarget());
            nextNode = next;
            cachedClass = primary.getPythonClass();
            declarationFrame = callee.__func__().getDeclarationFrame();
            dispatchStable = primary.getStableAssumption();
            assert primary instanceof PythonObject && !(primary instanceof PythonClass);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonBasicObject primaryObj, Object... arguments) {
            if (primaryObj.getPythonClass() == cachedClass) {
                try {
                    dispatchStable.check();

                    PArguments arg = new PArguments(primaryObj, declarationFrame, arguments);
                    return callNode.call(frame.pack(), arg);
                } catch (InvalidAssumptionException ex) {
                    return executeCallAndRewrite(nextNode, frame, primaryObj, arguments);
                }
            }

            return nextNode.executeCall(frame, primaryObj, arguments);
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
                    callee = calleeNode.executePythonCallable(frame);
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
