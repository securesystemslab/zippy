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
package edu.uci.python.nodes.optimize;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;

public abstract class PeeledGeneratorLoopNode extends PNode {

    @Children protected final PNode[] argumentNodes;
    @Child protected PNode inlinedRootNode;
    private final PNode originalLoop;

    protected final String generatorName;
    protected final FrameDescriptor frameDescriptor;

    public PeeledGeneratorLoopNode(FunctionRootNode generatorRoot, FrameDescriptor frameDescriptor, PNode[] argumentNodes, PNode originalLoop) {
        this.frameDescriptor = frameDescriptor;
        this.inlinedRootNode = generatorRoot.split().getBody();
        this.argumentNodes = argumentNodes;
        this.generatorName = generatorRoot.getFunctionName();
        this.originalLoop = originalLoop;
    }

    public static PeeledGeneratorLoopNode create(FunctionRootNode generatorRoot, FrameDescriptor frameDescriptor, PNode primaryNode, PNode[] argumentNodes, ShapeCheckNode checkNode, PNode originalLoop) {
        if (primaryNode != EmptyNode.create()) {
            return new PeeledGeneratorLoopBoxedNode(generatorRoot, frameDescriptor, primaryNode, argumentNodes, checkNode, originalLoop);
        }

        throw new IllegalStateException();
    }

    public PNode getGeneratorRoot() {
        return inlinedRootNode;
    }

    protected Object deoptAndExecute(VirtualFrame frame) {
        CompilerAsserts.neverPartOfCompilation();
        return replace(originalLoop).execute(frame);
    }

    public static final class PeeledGeneratorLoopBoxedNode extends PeeledGeneratorLoopNode {

        @Child protected PNode primaryNode;
        @Child protected ShapeCheckNode checkNode;

        public PeeledGeneratorLoopBoxedNode(FunctionRootNode generatorRoot, FrameDescriptor frameDescriptor, PNode primaryNode, PNode[] argumentNodes, ShapeCheckNode checkNode, PNode originalLoop) {
            super(generatorRoot, frameDescriptor, argumentNodes, originalLoop);
            this.primaryNode = primaryNode;
            this.checkNode = checkNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary;

            try {
                primary = primaryNode.executePythonObject(frame);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return deoptAndExecute(frame);
            }

            try {
                if (checkNode.accept(primary)) {
                    final Object[] arguments = PythonCallUtil.executeArguments(frame, argumentNodes);
                    PArguments.setVirtualFrameCargoArguments(arguments, frame);
                    VirtualFrame generatorFrame = Truffle.getRuntime().createVirtualFrame(arguments, frameDescriptor);
                    return inlinedRootNode.execute(generatorFrame);
                }
            } catch (InvalidAssumptionException e) {
            }

            CompilerDirectives.transferToInterpreterAndInvalidate();
            return deoptAndExecute(frame);
        }
    }

    public static final class PeeledGeneratorLoopNoneNode extends PeeledGeneratorLoopNode {

        @Child protected PNode calleeNode;
        private final PythonCallable cachedCallee;

        public PeeledGeneratorLoopNoneNode(FunctionRootNode generatorRoot, FrameDescriptor frameDescriptor, PNode calleeNode, PNode[] argumentNodes, PythonCallable callee, PNode originalLoop) {
            super(generatorRoot, frameDescriptor, argumentNodes, originalLoop);
            this.calleeNode = calleeNode;
            this.cachedCallee = callee;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonCallable callee;

            try {
                callee = calleeNode.executePythonCallable(frame);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return deoptAndExecute(frame);
            }

            if (cachedCallee == callee) {
                final Object[] arguments = PythonCallUtil.executeArguments(frame, argumentNodes);
                PArguments.setVirtualFrameCargoArguments(arguments, frame);
                VirtualFrame generatorFrame = Truffle.getRuntime().createVirtualFrame(arguments, frameDescriptor);
                return inlinedRootNode.execute(generatorFrame);
            }

            CompilerDirectives.transferToInterpreterAndInvalidate();
            return deoptAndExecute(frame);
        }
    }

}
