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

import org.python.core.*;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

@NodeChild(value = "callee", type = PNode.class)
public abstract class CallNode extends PNode {

    public abstract PNode getCallee();

    @Children private final PNode[] arguments;

    @Children private final PNode[] keywords;

    public CallNode(PNode[] arguments, PNode[] keywords) {
        this.arguments = adoptChildren(arguments);
        this.keywords = adoptChildren(keywords);
    }

    protected CallNode(CallNode node) {
        this(node.arguments, node.keywords);
    }

    public PNode[] getArguments() {
        return arguments;
    }

    @Specialization
    public Object doPCallable(VirtualFrame frame, PCallable callee) {
        Object[] args = executeArguments(frame, arguments);
        Object[] kwords = executeArguments(frame, keywords);
        return callee.call(frame.pack(), args, kwords);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object callee) {
        Object[] args = executeArguments(frame, arguments);

        if (callee instanceof PythonClass) {
            PNode specialized = new CallConstructorNode(getCallee(), arguments);
            replace(specialized);
            return specialized.execute(frame);
        } else if (callee instanceof PyObject) {
            PyObject[] pyargs = adaptToPyObjects(args);
            PyObject pyCallable = (PyObject) callee;
            return unboxPyObject(pyCallable.__call__(pyargs));
        } else {
            throw Py.SystemError("Unexpected callable type");
        }
    }

    @ExplodeLoop
    protected static Object[] executeArguments(VirtualFrame frame, PNode[] arguments) {
        Object[] evaluated = new Object[arguments.length];
        int index = 0;

        for (int i = 0; i < arguments.length; i++) {
            Object arg = arguments[i].execute(frame);

            if (arg instanceof PyObject) {
                arg = unboxPyObject((PyObject) arg);
            }

            evaluated[index] = arg;
            index++;
        }

        return evaluated;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callee=" + getCallee() + ")";
    }
}
