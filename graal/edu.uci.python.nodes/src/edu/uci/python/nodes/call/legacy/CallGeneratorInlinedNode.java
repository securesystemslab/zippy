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
package edu.uci.python.nodes.call.legacy;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.function.GeneratorExpressionNode.CallableGeneratorExpressionDefinition;
import edu.uci.python.runtime.function.*;

public class CallGeneratorInlinedNode extends InlinedCallNode {

    private final PythonCallable generator;
    private final Assumption globalScopeUnchanged;
    @Child protected PNode generatorRoot;

    public CallGeneratorInlinedNode(PNode callee, PNode[] arguments, PythonCallable generator, FunctionRootNode generatorRoot, Assumption globalScopeUnchanged, FrameFactory frameFactory) {
        super(callee, arguments, generator.getFrameDescriptor().copy(), frameFactory);
        assert generator instanceof PGeneratorFunction || generator instanceof CallableGeneratorExpressionDefinition;
        this.generator = generator;
        this.globalScopeUnchanged = globalScopeUnchanged;
        this.generatorRoot = prepareBody(generatorRoot.getInlinedRootNode());
    }

    @Override
    public PythonCallable getCallee() {
        return generator;
    }

    public CallTarget getCallTarget() {
        return generator.getCallTarget();
    }

    public PNode getGeneratorRoot() {
        return generatorRoot;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            globalScopeUnchanged.check();
        } catch (InvalidAssumptionException e) {
            return uninitialize(frame);
        }

        final Object[] args = PythonCallUtil.executeArguments(frame, arguments);
        final PArguments pargs = new PArguments.VirtualFrameCargoArguments(null, frame, args);
        return generatorRoot.execute(createInlinedFrame(frame, pargs));
    }

    public static final class PeeledGeneratorLoopNode extends PNode {

        @Child protected PNode inlinedRootNode;
        @Children protected final PNode[] argumentNodes;

        private final FrameDescriptor frameDescriptor;

        public PeeledGeneratorLoopNode(FunctionRootNode generatorRoot, FrameDescriptor frameDescriptor, PNode[] argumentNodes) {
            this.frameDescriptor = frameDescriptor;
            this.argumentNodes = argumentNodes;
            this.inlinedRootNode = generatorRoot.getInlinedRootNode();
        }

        public PNode getGeneratorRoot() {
            return inlinedRootNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object[] arguments = PythonCallUtil.executeArguments(frame, argumentNodes);
            PArguments.setVirtualFrameCargoArguments(arguments, frame);
            VirtualFrame generatorFrame = Truffle.getRuntime().createVirtualFrame(arguments, frameDescriptor);
            return inlinedRootNode.execute(generatorFrame);
        }
    }

}
