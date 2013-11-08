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
package edu.uci.python.nodes;

import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.access.*;
import edu.uci.python.runtime.datatypes.*;

public class GeneratorExpressionDefinitionNode extends PNode {

    private final CallTarget callTarget;
    private final FrameDescriptor frameDescriptor;
    private final boolean needsDeclarationFrame;

    @Child protected GeneratorExpressionRootNode rootNode;

    public GeneratorExpressionDefinitionNode(CallTarget callTarget, GeneratorExpressionRootNode rootNode, FrameDescriptor descriptor, boolean needsDeclarationFrame) {
        this.callTarget = callTarget;
        this.rootNode = adoptChild(rootNode);
        this.frameDescriptor = descriptor;
        this.needsDeclarationFrame = needsDeclarationFrame;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        PGenerator generator = new PGenerator("generator expr", callTarget, frameDescriptor, needsDeclarationFrame);
        // TODO: It's a bad way to determine whether the
        // generator should be evaluated immediately or not.
        if (getParent() instanceof WriteNode) {
            return generator;
        } else {
            return executeGenerator(frame, generator);
        }
    }

    /**
     * This logic should belong to another node that wraps this definition node.
     */
    public static Object executeGenerator(VirtualFrame frame, PGenerator generator) {
        Iterator<?> iter = generator.evaluateToJavaIteratore(frame);
        List<Object> results = new ArrayList<>();

        while (iter.hasNext()) {
            results.add(iter.next());
        }

        return new PList(results);
    }
}
