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
package edu.uci.python.nodes.statement;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

@NodeChild(value = "definitionFunction", type = PNode.class)
public abstract class ClassDefinitionNode extends StatementNode {

    private final PythonContext context;
    private final String name;

    @Children private final PNode[] baseNodes;

    public ClassDefinitionNode(PythonContext context, String name, PNode[] baseClasses) {
        this.context = context;
        this.name = name;
        this.baseNodes = baseClasses;
    }

    protected ClassDefinitionNode(ClassDefinitionNode prev) {
        this(prev.context, prev.name, prev.baseNodes);
    }

    @ExplodeLoop
    @Specialization
    Object doDefine(VirtualFrame frame, PythonCallable definitionFunc) {
        final PythonClass[] bases = executeBases(frame);
        final PythonClass newClass = new PythonClass(context, name, bases);
        definitionFunc.call(PArguments.createWithUserArguments(newClass));
        return newClass;
    }

    private PythonClass[] executeBases(VirtualFrame frame) {
        final PythonClass[] bases = new PythonClass[baseNodes.length];

        for (int i = 0; i < baseNodes.length; i++) {
            try {
                bases[i] = baseNodes[i].executePythonClass(frame);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException();
            }
        }

        return bases;
    }

}
