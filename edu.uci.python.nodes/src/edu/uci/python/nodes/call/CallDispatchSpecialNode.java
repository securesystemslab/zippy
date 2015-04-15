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

import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;

public abstract class CallDispatchSpecialNode extends CallDispatchNode {

    public CallDispatchSpecialNode(String specialMethodId) {
        super(specialMethodId);
    }

    public abstract Object executeCall(VirtualFrame frame, Object left, Object right);

    protected final Object executeCallAndRewrite(CallDispatchSpecialNode next, VirtualFrame frame, Object left, Object right) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return replace(next).executeCall(frame, left, right);
    }

    protected static CallDispatchSpecialNode create(PythonObject primary, String specialMethodId, PythonCallable callee, boolean reflected) {
        UninitializedDispatchSpecialNode next = new UninitializedDispatchSpecialNode(specialMethodId);

        LayoutCheckNode check = LayoutCheckNode.create(primary, specialMethodId, primary.isOwnAttribute(specialMethodId));
        assert check != null;

        if (callee instanceof PGeneratorFunction) {
            return new GeneratorDispatchSpecialNode((PGeneratorFunction) callee, check, next);
        }

        if (!reflected) {
            return new LinkedDispatchSpecialNode(callee, check, next);
        } else {
            return new LinkedReflectedDispatchSpecialNode(callee, check, next);
        }
    }

    public static class LinkedDispatchSpecialNode extends CallDispatchSpecialNode {

        @Child protected LayoutCheckNode check;
        @Child protected InvokeNode invoke;
        @Child protected CallDispatchSpecialNode next;

        public LinkedDispatchSpecialNode(PythonCallable callee, LayoutCheckNode check, UninitializedDispatchSpecialNode next) {
            super(callee.getName());
            this.check = check;
            this.next = next;
            this.invoke = InvokeNode.create(callee, false);
        }

        @Override
        public NodeCost getCost() {
            return getCost(next);
        }

        protected final boolean accept(Object primary) throws InvalidAssumptionException {
            PythonObject pyobj;

            try {
                pyobj = PythonTypesGen.PYTHONTYPES.expectPythonObject(primary);
            } catch (UnexpectedResultException e) {
                return false;
            }

            return check.accept(pyobj);
        }

        @Override
        public Object executeCall(VirtualFrame frame, Object left, Object right) {
            try {
                if (accept(left)) {
                    return invoke.invoke(frame, left, PArguments.createWithUserArguments(left, right), PKeyword.EMPTY_KEYWORDS);
                } else {
                    return next.executeCall(frame, left, right);
                }
            } catch (InvalidAssumptionException ex) {
                return executeCallAndRewrite(next, frame, left, right);
            }
        }
    }

    public static final class LinkedReflectedDispatchSpecialNode extends LinkedDispatchSpecialNode {

        public LinkedReflectedDispatchSpecialNode(PythonCallable callee, LayoutCheckNode check, UninitializedDispatchSpecialNode next) {
            super(callee, check, next);
        }

        @Override
        public Object executeCall(VirtualFrame frame, Object left, Object right) {
            try {
                if (accept(right)) {
                    return invoke.invoke(frame, right, PArguments.createWithUserArguments(right, left), PKeyword.EMPTY_KEYWORDS);
                } else {
                    return next.executeCall(frame, left, right);
                }
            } catch (InvalidAssumptionException ex) {
                return executeCallAndRewrite(next, frame, left, right);
            }
        }
    }

    public static final class GeneratorDispatchSpecialNode extends LinkedDispatchSpecialNode implements GeneratorDispatch {

        private final PGeneratorFunction genfunc;

        public GeneratorDispatchSpecialNode(PGeneratorFunction genfunc, LayoutCheckNode check, UninitializedDispatchSpecialNode next) {
            super(genfunc, check, next);
            this.genfunc = genfunc;
        }

        @Override
        public Node getCallNode() {
            if (getParent() instanceof GetIteratorNode) {
                return this;
            } else {
                return getParent();
            }
        }

        public PGeneratorFunction getGeneratorFunction() {
            return genfunc;
        }

        @Override
        protected void onAdopt() {
            RootNode root = getRootNode();
            if (root instanceof FunctionRootNode) {
                ((FunctionRootNode) root).reportGeneratorDispatch();
            }
        }

        @Override
        public NodeCost getCost() {
            return getCost(next);
        }

        public LayoutCheckNode getCheckNode() {
            return check;
        }

        @Override
        public Object executeCall(VirtualFrame frame, Object left, Object right) {
            try {
                if (accept(left)) {
                    return genfunc.call(PArguments.createWithUserArguments(left, right));
                } else {
                    return next.executeCall(frame, left, right);
                }
            } catch (InvalidAssumptionException ex) {
                return executeCallAndRewrite(next, frame, left, right);
            }
        }

    }

    @NodeInfo(cost = NodeCost.MEGAMORPHIC)
    public static final class GenericDispatchSpecialNode extends CallDispatchSpecialNode {

        public GenericDispatchSpecialNode(String calleeName) {
            super(calleeName);
        }

        @Override
        public Object executeCall(VirtualFrame frame, Object left, Object right) {
            CompilerAsserts.neverPartOfCompilation();

            String specialMethodId = calleeName;
            PythonCallable callee = PythonCallUtil.resolveSpecialMethod(left, specialMethodId);

            if (callee != null) {
                // Non reflective special method is found.
                return callee.call(new Object[]{left, right});
            }

            specialMethodId = calleeName.replaceFirst("__", "__r");
            callee = PythonCallUtil.resolveSpecialMethod(right, specialMethodId);

            if (callee != null) {
                // Reflective special method is found.
                return callee.call(new Object[]{right, left});
            }

            throw new IllegalStateException("Call to " + calleeName + " not supported.");
        }
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static final class UninitializedDispatchSpecialNode extends CallDispatchSpecialNode {

        public UninitializedDispatchSpecialNode(String calleeName) {
            super(calleeName);
        }

        @Override
        public Object executeCall(VirtualFrame frame, Object left, Object right) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            CallDispatchSpecialNode specialized;

            if (getDispatchDepth() >= PythonOptions.CallSiteInlineCacheMaxDepth) {
                specialized = getTop().replace(new GenericDispatchSpecialNode(calleeName));
            }

            /**
             * Setting up specialized dispatch node.
             */
            String specialMethodId = calleeName;
            PythonCallable callee = PythonCallUtil.resolveSpecialMethod(left, calleeName);

            if (callee != null) {
                // Non reflective special method is found.
                specialized = replace(create((PythonObject) left, specialMethodId, callee, false));
                return specialized.executeCall(frame, left, right);
            }

            specialMethodId = calleeName.replaceFirst("__", "__r");
            callee = PythonCallUtil.resolveSpecialMethod(right, specialMethodId);

            if (callee != null) {
                // Reflective special method is found.
                specialized = replace(create((PythonObject) right, specialMethodId, callee, true));
                return specialized.executeCall(frame, left, right);
            }

            throw new IllegalStateException("Call to " + calleeName + " not supported.");
        }
    }

}
