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
package edu.uci.python.nodes.function;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;

public class GeneratorFunctionDefinitionNode extends FunctionDefinitionNode {

    protected final int numOfActiveFlags;
    protected final int numOfGeneratorBlockNode;
    protected final int numOfGeneratorForNode;

    public GeneratorFunctionDefinitionNode(String name, PythonContext context, Arity arity, PNode defaults, RootCallTarget callTarget, FrameDescriptor frameDescriptor, boolean needsDeclarationFrame,
                    int numOfActiveFlags, int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
        super(name, context, arity, defaults, callTarget, frameDescriptor, needsDeclarationFrame);
        this.numOfActiveFlags = numOfActiveFlags;
        this.numOfGeneratorBlockNode = numOfGeneratorBlockNode;
        this.numOfGeneratorForNode = numOfGeneratorForNode;
    }

    public static GeneratorFunctionDefinitionNode create(String name, PythonContext context, Arity arity, PNode defaults, RootCallTarget callTarget, FrameDescriptor frameDescriptor,
                    boolean needsDeclarationFrame, int numOfActiveFlags, int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
        if (needsDeclarationFrame || !EmptyNode.isEmpty(defaults)) {
            return new GeneratorFunctionDefinitionNode(name, context, arity, defaults, callTarget, frameDescriptor, needsDeclarationFrame, numOfActiveFlags, numOfGeneratorBlockNode,
                            numOfGeneratorForNode);
        }

        return new StatelessGeneratorFunctionDefinitionNode(name, context, arity, callTarget, frameDescriptor, numOfActiveFlags, numOfGeneratorBlockNode, numOfGeneratorForNode);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        defaults.executeVoid(frame);
        MaterializedFrame declarationFrame = needsDeclarationFrame ? frame.materialize() : null;
        return new PGeneratorFunction(name, arity, callTarget, frameDescriptor, declarationFrame, numOfActiveFlags, numOfGeneratorBlockNode, numOfGeneratorForNode);
    }

    /**
     * Creates a generator function that does not capture any state. Therefore, it can always return
     * the same generator function instance.
     */
    public static final class StatelessGeneratorFunctionDefinitionNode extends GeneratorFunctionDefinitionNode {

        private final PGeneratorFunction cached;

        public StatelessGeneratorFunctionDefinitionNode(String name, PythonContext context, Arity arity, RootCallTarget callTarget, FrameDescriptor frameDescriptor, int numOfActiveFlags,
                        int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
            super(name, context, arity, EmptyNode.create(), callTarget, frameDescriptor, false, numOfActiveFlags, numOfGeneratorBlockNode, numOfGeneratorForNode);
            cached = new PGeneratorFunction(name, arity, callTarget, frameDescriptor, null, numOfActiveFlags, numOfGeneratorBlockNode, numOfGeneratorForNode);
        }

        public StatelessGeneratorFunctionDefinitionNode(GeneratorExpressionNode prev) {
            super(prev.getName(), prev.context, Arity.DUMMY, EmptyNode.create(), prev.getCallTarget(), prev.getFrameDescriptor(), false, prev.getNumOfActiveFlags(), prev.getNumOfGeneratorBlockNode(),
                            prev.getNumOfGeneratorForNode());
            cached = new PGeneratorFunction(name, arity, callTarget, frameDescriptor, null, numOfActiveFlags, numOfGeneratorBlockNode, numOfGeneratorForNode);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return cached;
        }
    }

}
