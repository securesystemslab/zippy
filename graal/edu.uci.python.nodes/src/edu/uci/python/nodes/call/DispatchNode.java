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

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;

public abstract class DispatchNode extends Node {

    private static DispatchNode create(PythonCallable callee, UninitializedDispatchNode next) {
        if (callee instanceof PFunction) {
            return new DispatchFunctionNode((PFunction) callee, next);
        } else if (callee instanceof PBuiltinFunction) {
            return new DispatchBuiltinNode((PBuiltinFunction) callee, next);
        }

        throw new UnsupportedOperationException("Unsupported callee type " + callee);
    }

    protected abstract Object executeCall(VirtualFrame frame, PythonCallable callee, Object... arguments);

    public static final class DispatchFunctionNode extends DispatchNode {

        protected final PFunction cachedCallee;
        protected final CallTarget cachedCallTarget;
        protected final Assumption cachedCallTargetStable;
        private final MaterializedFrame declarationFrame;

        @Child protected CallNode callNode;
        @Child protected DispatchNode nextNode;

        public DispatchFunctionNode(PFunction callee, DispatchNode next) {
            cachedCallee = callee;
            cachedCallTarget = callee.getCallTarget();
            declarationFrame = callee.getDeclarationFrame();
            // TODO: replace holder for now.
            cachedCallTargetStable = AlwaysValidAssumption.INSTANCE;
            callNode = adoptChild(CallNode.create(cachedCallTarget));
            nextNode = adoptChild(next);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object... arguments) {
            if (this.cachedCallee == callee) {
                try {
                    cachedCallTargetStable.check();

                    PArguments arg = new PArguments(null, declarationFrame, arguments);
                    return callNode.call(frame.pack(), arg);
                } catch (InvalidAssumptionException ex) {
                    /*
                     * Remove ourselfs from the polymorphic inline cache, so that we fail the check
                     * only once.
                     */
                    replace(nextNode);
                    /*
                     * Execute the next node in the chain by falling out of the if block.
                     */
                }
            }

            return nextNode.executeCall(frame, callee, arguments);
        }
    }

    public static final class DispatchBuiltinNode extends DispatchNode {

        protected final PBuiltinFunction cachedCallee;
        protected final CallTarget cachedCallTarget;
        protected final Assumption cachedCallTargetStable;

        @Child protected CallNode callNode;
        @Child protected DispatchNode nextNode;

        public DispatchBuiltinNode(PBuiltinFunction callee, DispatchNode next) {
            cachedCallee = callee;
            cachedCallTarget = callee.getCallTarget();
            // TODO: replace holder for now.
            cachedCallTargetStable = AlwaysValidAssumption.INSTANCE;
            callNode = adoptChild(CallNode.create(cachedCallTarget));
            nextNode = adoptChild(next);
        }

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object... arguments) {
            if (this.cachedCallee == callee) {
                try {
                    cachedCallTargetStable.check();

                    PArguments arg = new PArguments(PNone.NONE, null, arguments);
                    return callNode.call(frame.pack(), arg);
                } catch (InvalidAssumptionException ex) {
                    replace(nextNode);
                }
            }

            return nextNode.executeCall(frame, callee, arguments);
        }

    }

    public static final class GenericDispatchNode extends DispatchNode {

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object... arguments) {
            return callee.call(frame.pack(), arguments);
        }

    }

    public static final class UninitializedDispatchNode extends DispatchNode {

        @Override
        protected Object executeCall(VirtualFrame frame, PythonCallable callee, Object... arguments) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            DispatchNode current = this;
            int depth = 0;
            while (current.getParent() instanceof DispatchNode) {
                current = (DispatchNode) current.getParent();
                depth++;
            }

            DispatchNode specialized;
            if (depth < PythonOptions.CallSiteInlineCacheMax) {
                UninitializedDispatchNode next = new UninitializedDispatchNode();
                DispatchNode direct = create(callee, next);
                specialized = replace(direct);
            } else {
                DispatchNode generic = new GenericDispatchNode();
                // TODO: should replace the dispatch node of the parent call node.
                specialized = replace(generic);
            }

            return specialized.executeCall(frame, callee, arguments);
        }

    }

}
