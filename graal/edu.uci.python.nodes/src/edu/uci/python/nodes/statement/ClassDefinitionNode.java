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

import org.python.core.*;

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
    private final String moduleName;
    private final String name;

    @Children private final PNode[] baseNodes;

    public ClassDefinitionNode(PythonContext context, String moduleName, String name, PNode[] baseClasses) {
        this.context = context;
        this.moduleName = moduleName;
        this.name = name;
        this.baseNodes = baseClasses;
    }

    protected ClassDefinitionNode(ClassDefinitionNode prev) {
        this(prev.context, prev.moduleName, prev.name, prev.baseNodes);
    }

    @Specialization
    Object doDefine(VirtualFrame frame, PythonCallable definitionFunc) {
        PythonClass[] bases;
        PythonClass newClass;

        try {
            bases = executeBases(frame);
            newClass = new PythonClass(context, moduleName + '.' + name, bases);
        } catch (UnexpectedResultException e) {
            newClass = tryToDefineJythonSubClass(e.getResult());
        }

        definitionFunc.call(PArguments.createWithUserArguments(newClass));
        return newClass;
    }

    @ExplodeLoop
    private PythonClass[] executeBases(VirtualFrame frame) throws UnexpectedResultException {
        final PythonClass[] bases = new PythonClass[baseNodes.length];

        for (int i = 0; i < baseNodes.length; i++) {
            bases[i] = baseNodes[i].executePythonClass(frame);
        }

        return bases;
    }

    private PythonClass tryToDefineJythonSubClass(Object result) {
        if (result instanceof PyType) {
            return new JythonTypeSubClass(context, name, (PyType) result);
        } else {
            throw new IllegalStateException();
        }
    }

}
