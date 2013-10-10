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

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

import org.python.core.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.standardtypes.*;

@NodeChild(value = "callee", type = PNode.class)
public abstract class CallFunctionNoKeywordNode extends PNode {

    @Children protected final PNode[] arguments;

    public abstract PNode getCallee();

    public CallFunctionNoKeywordNode(PNode[] arguments) {
        this.arguments = adoptChildren(arguments);
    }

    protected CallFunctionNoKeywordNode(CallFunctionNoKeywordNode node) {
        this(node.arguments);
    }

    @Specialization
    public Object doPCallable(VirtualFrame frame, PCallable callee) {
        final Object[] args = CallFunctionNode.executeArguments(frame, arguments);
        return callee.call(frame.pack(), args);
    }

    @Specialization
    public Object doPyObject(VirtualFrame frame, PyObject callee) {
        Object[] args = CallFunctionNode.executeArguments(frame, arguments);
        PyObject[] pyargs = adaptToPyObjects(args);
        return unboxPyObject(callee.__call__(pyargs));
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object callee) {
        Object[] args = CallFunctionNode.executeArguments(frame, arguments);

        if (callee instanceof PythonClass) {
            CallConstructorNode specialized = new CallConstructorNode(getCallee(), arguments);
            replace(specialized);
            return specialized.callConstructor(frame, (PythonClass) callee, args);
        } else if (callee instanceof PyObject) {
            PyObject[] pyargs = adaptToPyObjects(args);
            PyObject pyCallable = (PyObject) callee;
            return unboxPyObject(pyCallable.__call__(pyargs));
        } else {
            throw Py.SystemError("Unexpected callable type");
        }
    }

}
