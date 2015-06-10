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
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class CallDispatchBoxedNode extends CallDispatchNode {

    public CallDispatchBoxedNode(String calleeName) {
        super(calleeName);
    }

    public abstract Object executeCall(VirtualFrame frame, PythonObject primaryObj, Object[] arguments, PKeyword[] keywords);

    protected final Object executeCallAndRewrite(CallDispatchBoxedNode next, VirtualFrame frame, PythonObject primaryObj, Object[] arguments, PKeyword[] keywords) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return replace(next).executeCall(frame, primaryObj, arguments, keywords);
    }

    protected static CallDispatchBoxedNode create(PythonObject primary, String calleeName, PythonCallable callee, PNode calleeNode, PKeyword[] keywords, boolean passPrimaryAsArgument) {
        UninitializedDispatchBoxedNode next = new UninitializedDispatchBoxedNode(callee.getName(), calleeNode, keywords.length != 0, passPrimaryAsArgument);
        LayoutCheckNode check;

        if (primary instanceof PythonModule && callee instanceof PMethod) {
            return new GenericDispatchBoxedNode(calleeName, calleeNode, passPrimaryAsArgument);
        }

        if (primary instanceof PythonModule) {
            if (calleeNode instanceof ReadGlobalNode) {
                check = ((ReadGlobalNode) calleeNode).extractShapeCheckNode();
            } else {
                check = LayoutCheckNode.create(primary, calleeName, primary.isOwnAttribute(calleeName));
            }
        } else if (primary instanceof PythonBuiltinClass) {
            /**
             * Since built-in classes are immutable, there is no need to probe the exact storage
             * object.
             */
            check = LayoutCheckNode.create(primary, calleeName, false);
        } else {
            check = LayoutCheckNode.create(primary, calleeName, primary.isOwnAttribute(calleeName));
        }

        /**
         * Treat generator as slow path for now.
         */
        if (callee.isGeneratorFunction()) {
            return new GeneratorDispatchBoxedNode(callee, check, next);
        }

        assert check != null;
        return new LinkedDispatchBoxedNode(callee, check, next);
    }

    /**
     * The primary could be:
     * <p>
     * 1. The global {@link PythonModule}. <br>
     * 3. A {@link PythonModule}. <br>
     * 2. A {@link PythonClass}. <br>
     * 4. A {@link PythonObject} <br>
     *
     * 1. The global {@link PythonModule}. <br>
     * 2. A built-in {@link PythonModule}. <br>
     * 3. A built-in {@link PythonBuiltinClass}.
     *
     */
    public static final class LinkedDispatchBoxedNode extends CallDispatchBoxedNode {

        @Child protected LayoutCheckNode check;
        @Child protected InvokeNode invoke;
        @Child protected CallDispatchBoxedNode next;

        public LinkedDispatchBoxedNode(PythonCallable callee, LayoutCheckNode check, UninitializedDispatchBoxedNode next) {
            super(callee.getName());
            this.check = check;
            this.next = next;

            if (callee instanceof PythonClass) {
                this.invoke = InvokeNode.create(((PythonClass) callee).lookUpMethod("__init__"), next.hasKeyword);
            } else {
                this.invoke = InvokeNode.create(callee, next.hasKeyword);
            }
        }

        public InvokeNode getInvokeNode() {
            return invoke;
        }

        public LayoutCheckNode getCheckNode() {
            return check;
        }

        @Override
        public NodeCost getCost() {
            return getCost(next);
        }

        @Override
        public boolean isInlined() {
            return getCost() == NodeCost.MONOMORPHIC && invoke.isInlined();
        }

        @Override
        public Object executeCall(VirtualFrame frame, PythonObject primaryObj, Object[] arguments, PKeyword[] keywords) {
            try {
                if (check.accept(primaryObj)) {
                    return invoke.invoke(frame, primaryObj, arguments, keywords);
                } else {
                    return next.executeCall(frame, primaryObj, arguments, keywords);
                }
            } catch (InvalidAssumptionException ex) {
                return executeCallAndRewrite(next, frame, primaryObj, arguments, keywords);
            }
        }
    }

    public static final class GeneratorDispatchBoxedNode extends CallDispatchBoxedNode implements GeneratorDispatch {

        @Child protected LayoutCheckNode check;
        @Child protected CallDispatchBoxedNode next;
        private final PGeneratorFunction generator;

        public GeneratorDispatchBoxedNode(PythonCallable callee, LayoutCheckNode check, UninitializedDispatchBoxedNode next) {
            super(callee.getName());
            this.check = check;
            this.next = next;

            if (callee instanceof PGeneratorFunction) {
                this.generator = (PGeneratorFunction) callee;
            } else if (callee instanceof PMethod) {
                this.generator = (PGeneratorFunction) ((PMethod) callee).__func__();
            } else {
                throw new IllegalStateException();
            }

            assert this.generator != null;
        }

        @Override
        public NodeCost getCost() {
            return getCost(next);
        }

        @Override
        public Object executeCall(VirtualFrame frame, PythonObject primaryObj, Object[] arguments, PKeyword[] keywords) {
            try {
                if (check.accept(primaryObj)) {
                    return generator.call(arguments);
                } else {
                    return next.executeCall(frame, primaryObj, arguments, keywords);
                }
            } catch (InvalidAssumptionException ex) {
                return executeCallAndRewrite(next, frame, primaryObj, arguments, keywords);
            }
        }

        public LayoutCheckNode getCheckNode() {
            return check;
        }

        @Override
        public PGeneratorFunction getGeneratorFunction() {
            return generator;
        }

        @Override
        public Node getCallNode() {
            return getTop().getParent();
        }
    }

    @NodeInfo(cost = NodeCost.MEGAMORPHIC)
    public static final class GenericDispatchBoxedNode extends CallDispatchBoxedNode {

        @Child protected PNode calleeNode;
        private final boolean isPrimaryPassedInArguments;

        public GenericDispatchBoxedNode(String calleeName, PNode calleeNode, boolean isPrimaryPassedInArguments) {
            super(calleeName);
            this.calleeNode = calleeNode;
            this.isPrimaryPassedInArguments = isPrimaryPassedInArguments;
        }

        @Override
        public Object executeCall(VirtualFrame frame, PythonObject primaryObj, Object[] arguments, PKeyword[] keywords) {
            PythonCallable callee;

            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
            }

            if (callee instanceof PMethod && isPrimaryPassedInArguments) {
                return ((PMethod) callee).__func__().call(arguments);
            }

            return callee.call(arguments);
        }
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static final class UninitializedDispatchBoxedNode extends CallDispatchBoxedNode {

        @Child protected PNode calleeNode;
        private final boolean hasKeyword;
        private final boolean isPrimaryPassedInArguments;

        public UninitializedDispatchBoxedNode(String calleeName, PNode calleeNode, boolean hasKeyword, boolean isPrimaryPassedInArguments) {
            super(calleeName);
            this.calleeNode = calleeNode;
            this.hasKeyword = hasKeyword;
            this.isPrimaryPassedInArguments = isPrimaryPassedInArguments;
        }

        @Override
        public Object executeCall(VirtualFrame frame, PythonObject primaryObj, Object[] arguments, PKeyword[] keywords) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            if (!primaryObj.getStableAssumption().isValid()) {
                primaryObj.syncObjectLayoutWithClass();
            }

            CallDispatchBoxedNode current = this;

            while (current.getParent() instanceof CallDispatchBoxedNode) {
                current = (CallDispatchBoxedNode) current.getParent();

                if (!(current instanceof LinkedDispatchBoxedNode)) {
                    continue;
                }

                /**
                 * After the layout sync, if a previously missed dispatch node caches the updated
                 * layout, we should reuse the existing dispatch node.
                 */
                LinkedDispatchBoxedNode linked = (LinkedDispatchBoxedNode) current;
                try {
                    if (linked.check.accept(primaryObj)) {
                        return linked.invoke.invoke(frame, primaryObj, arguments, keywords);
                    }
                } catch (InvalidAssumptionException e) {
                    throw new RuntimeException();
                }
            }

            CallDispatchBoxedNode specialized;

            if (getDispatchDepth() < PythonOptions.CallSiteInlineCacheMaxDepth) {
                PythonCallable callee;

                try {
                    callee = calleeNode.executePythonCallable(frame);
                } catch (UnexpectedResultException e) {
                    throw new IllegalStateException("Call to " + e.getMessage() + " not supported.");
                }

                specialized = replace(create(primaryObj, calleeName, callee, calleeNode, keywords, isPrimaryPassedInArguments));
            } else {
                specialized = getTop().replace(new GenericDispatchBoxedNode(calleeName, calleeNode, isPrimaryPassedInArguments));
            }

            return specialized.executeCall(frame, primaryObj, arguments, keywords);
        }
    }

}
