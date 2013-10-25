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
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.FunctionRootNode.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.calls.CallFunctionNoKeywordNode.*;
import edu.uci.python.runtime.datatypes.*;

public class CallFunctionNoKeywordInlinedNode extends CallFunctionNoKeywordCachedNode implements InlinedCallSite {

    private final FrameFactory frameFactory;
    private final FrameDescriptor frameDescriptor;

    @Child protected InlinedFunctionRootNode functionRoot;

    public CallFunctionNoKeywordInlinedNode(PNode callee, PNode[] arguments, PFunction cached, Assumption globalScopeUnchanged, FunctionRootNode functionRoot, FrameFactory frameFactory) {
        super(callee, arguments, cached, globalScopeUnchanged);
        this.frameFactory = frameFactory;
        this.frameDescriptor = cached.getFrameDescriptor().copy();
        this.functionRoot = adoptChild(prepareBody(functionRoot.getInlinedRootNode()));
    }

    public CallTarget getCallTarget() {
        return cached.getCallTarget();
    }

    private InlinedFunctionRootNode prepareBody(InlinedFunctionRootNode clonedBody) {
        clonedBody.accept(new NodeVisitor() {

            public boolean visit(Node node) {
                prepareBodyNode(node);
                assert !(node instanceof FunctionRootNode);
                return true;
            }

        });
        return clonedBody;
    }

    private void prepareBodyNode(Node node) {
        NodeFactory factory = NodeFactory.getInstance();

        if (node instanceof FrameSlotNode) {
            FrameSlotNode fsNode = (FrameSlotNode) node;
            FrameSlot origSlot = fsNode.getSlot();
            FrameSlot newSlot = frameDescriptor.findFrameSlot(origSlot.getIdentifier());
            assert newSlot != null;

            if (node instanceof ReadLocalNode) {
                node.replace(factory.createReadLocal(newSlot));
            } else if (node instanceof WriteLocalNode) {
                node.replace(factory.createWriteLocal(((WriteLocalNode) node).getRhs(), newSlot));
            }
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            globalScopeUnchanged.check();
        } catch (InvalidAssumptionException e) {
            return uninitialize(frame);
        }

        final Object[] args = CallFunctionNode.executeArguments(frame, arguments);
        final PArguments pargs = new PArguments(PNone.NONE, args);
        VirtualFrame inlinedFrame = frameFactory.create(frameDescriptor, frame.pack(), pargs);
        return functionRoot.execute(inlinedFrame);
    }

}
