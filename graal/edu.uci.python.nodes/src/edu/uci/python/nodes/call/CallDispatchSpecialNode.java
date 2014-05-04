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

import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;

public abstract class CallDispatchSpecialNode extends CallDispatchNode {

    public CallDispatchSpecialNode(String specialMethodId) {
        super(specialMethodId);
    }

    public abstract Object executeCall(VirtualFrame frame, PythonObject primary, Object[] arguments);

    protected final Object executeCallAndRewrite(CallDispatchSpecialNode next, VirtualFrame frame, PythonObject primary, Object[] arguments) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return replace(next).executeCall(frame, primary, arguments);
    }

    protected static CallDispatchSpecialNode create(PythonObject primary, String specialMethodId, String resolvedMethodId, PythonCallable callee, boolean reflected) {
        UninitializedDispatchSpecialNode next = new UninitializedDispatchSpecialNode(specialMethodId);

        ShapeCheckNode check = ShapeCheckNode.create(primary, specialMethodId, primary.isOwnAttribute(resolvedMethodId));

        assert check != null;
        return new LinkedDispatchSpecialNode(callee, check, next, reflected);
    }

    protected static void swapArguments(Object[] arguments) {
        final Object temp = arguments[1];
        arguments[1] = arguments[0];
        arguments[0] = temp;
    }

    public static final class LinkedDispatchSpecialNode extends CallDispatchSpecialNode {

        @Child protected ShapeCheckNode check;
        @Child protected InvokeNode invoke;
        @Child protected CallDispatchSpecialNode next;

        private final boolean reflected;

        public LinkedDispatchSpecialNode(PythonCallable callee, ShapeCheckNode check, UninitializedDispatchSpecialNode next, boolean reflected) {
            super(callee.getName());
            this.check = check;
            this.next = next;
            this.invoke = InvokeNode.create(callee, false);
            this.reflected = reflected;
        }

        @Override
        public NodeCost getCost() {
            if (next != null && next.getCost() == NodeCost.MONOMORPHIC) {
                return NodeCost.POLYMORPHIC;
            }
            return super.getCost();
        }

        private PythonObject resolvePrimaryOperand(Object[] arguments) {
            return (PythonObject) (reflected ? arguments[1] : arguments[0]);
        }

        private void resolveArguments(Object[] arguments) {
            if (reflected) {
                swapArguments(arguments);
            }
        }

        @Override
        public Object executeCall(VirtualFrame frame, PythonObject primary, Object[] arguments) {
            try {
                if (check.accept(resolvePrimaryOperand(arguments))) {
                    resolveArguments(arguments);
                    return invoke.invoke(frame, primary, arguments, PKeyword.EMPTY_KEYWORDS);
                } else {
                    return next.executeCall(frame, primary, arguments);
                }
            } catch (InvalidAssumptionException ex) {
                return executeCallAndRewrite(next, frame, primary, arguments);
            }
        }
    }

    @NodeInfo(cost = NodeCost.MEGAMORPHIC)
    public static final class GenericDispatchSpecialNode extends CallDispatchSpecialNode {

        public GenericDispatchSpecialNode(String calleeName) {
            super(calleeName);
        }

        @Override
        public Object executeCall(VirtualFrame frame, PythonObject primary, Object[] arguments) {
            /**
             * Setting up specialized dispatch node.
             */
            String specialMethodId = calleeName;
            PythonCallable callee = resolveSpecialMethod(primary, specialMethodId);

            if (callee != null) {
                // Non reflective special method is found.
                return callee.call(frame.pack(), arguments);
            } else {
                specialMethodId = specialMethodId.replaceFirst("__", "__r");
                PythonObject right = (PythonObject) arguments[1];
                callee = resolveSpecialMethod(right, specialMethodId);

                if (callee == null) {
                    throw new IllegalStateException("Call to " + specialMethodId + " not supported.");
                }

                // Reflective special method is found.
                swapArguments(arguments);
                return callee.call(frame.pack(), arguments);
            }
        }
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static final class UninitializedDispatchSpecialNode extends CallDispatchSpecialNode {

        public UninitializedDispatchSpecialNode(String calleeName) {
            super(calleeName);
        }

        @Override
        public Object executeCall(VirtualFrame frame, PythonObject primary, Object[] arguments) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            CallDispatchSpecialNode specialized;

            if (getDispatchDepth() >= PythonOptions.CallSiteInlineCacheMaxDepth) {
                specialized = getTop().replace(new GenericDispatchSpecialNode(calleeName));
            }

            /**
             * Setting up specialized dispatch node.
             */
            String specialMethodId = calleeName;
            PythonCallable callee = resolveSpecialMethod(primary, specialMethodId);

            if (callee != null) {
                // Non reflective special method is found.
                specialized = replace(create(primary, calleeName, specialMethodId, callee, false));
            } else {
                specialMethodId = specialMethodId.replaceFirst("__", "__r");
                PythonObject right = (PythonObject) arguments[1];
                callee = resolveSpecialMethod(right, specialMethodId);

                if (callee == null) {
                    throw new IllegalStateException("Call to " + specialMethodId + " not supported.");
                }

                // Reflective special method is found.
                specialized = replace(create(right, calleeName, specialMethodId, callee, true));
            }

            return specialized.executeCall(frame, primary, arguments);
        }
    }

    protected static PythonCallable resolveSpecialMethod(PythonObject operand, String specialMethodId) {
        PythonCallable callee;

        try {
            callee = PythonTypesGen.PYTHONTYPES.expectPythonCallable(operand.getAttribute(specialMethodId));
        } catch (UnexpectedResultException e) {
            return null;
        }

        return callee;
    }

}
