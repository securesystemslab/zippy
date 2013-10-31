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

import org.python.core.*;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;
import edu.uci.python.runtime.standardtypes.*;
import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

@NodeChild("primary")
public abstract class CallAttributeNode extends PNode {

    @Children protected final PNode[] arguments;

    protected final String attributeId;

    public abstract PNode getPrimary();

    public CallAttributeNode(PNode[] arguments, String name) {
        this.arguments = adoptChildren(arguments);
        this.attributeId = name;
    }

    protected CallAttributeNode(CallAttributeNode node) {
        this(node.arguments, node.attributeId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callee=" + getPrimary() + "," + attributeId + ")";
    }

    @Specialization
    public Object doString(VirtualFrame frame, String prim) {
        Object[] args = doArguments(frame);
        PString primString = new PString(prim);
        return primString.findAttribute(attributeId).call(null, args);
    }

    @Specialization
    public Object doPObject(VirtualFrame frame, PythonBuiltinObject prim) {
        Object[] args = doArguments(frame);
        return prim.findAttribute(attributeId).call(null, args);
    }

    @Specialization
    public Object doPythonClass(VirtualFrame frame, PythonClass prim) {
        Object[] args = doArguments(frame);
        PCallable callable = (PCallable) prim.getAttribute(attributeId);
        return callable.call(frame.pack(), args);
    }

    @Specialization
    public Object doPythonObject(VirtualFrame frame, PythonObject prim) {
        Object[] args = doArgumentsWithSelf(frame, prim);
        PCallable callable = (PCallable) prim.getAttribute(attributeId);
        return callable.call(frame.pack(), args);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object prim) {
        Object[] args = doArguments(frame);

        PyObject primary;
        if (prim instanceof PyObject) {
            primary = (PyObject) prim;
        } else if (prim instanceof PModule) {
            return ((PModule) prim).lookupMethod(attributeId).call(null, args, null);
        } else {
            primary = adaptToPyObject(prim);
        }

        PyObject callable = primary.__findattr__(attributeId);

        // need to box Object to PyObject
        PyObject[] pyargs = new PyObject[args.length];
        int i = 0;
        for (Object arg : args) {
            pyargs[i] = adaptToPyObject(arg);
            i++;
        }

        return unboxPyObject(callable.__call__(pyargs));
    }

    @ExplodeLoop
    protected Object[] doArguments(VirtualFrame frame) {
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

    @ExplodeLoop
    protected Object[] doArgumentsWithSelf(VirtualFrame frame, Object self) {
        Object[] evaluated = new Object[arguments.length + 1];
        evaluated[0] = self;
        int index = 0;

        for (int i = 0; i < arguments.length; i++) {
            Object arg = arguments[i].execute(frame);

            if (arg instanceof PyObject) {
                arg = unboxPyObject((PyObject) arg);
            }

            evaluated[index + 1] = arg;
            index++;
        }

        return evaluated;
    }

}
