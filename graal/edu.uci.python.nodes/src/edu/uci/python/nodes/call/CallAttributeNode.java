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
package edu.uci.python.nodes.call;

import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.argument.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;
import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

@NodeChild("primary")
public abstract class CallAttributeNode extends PNode {

    @Children protected final PNode[] arguments;

    protected final String attributeId;

    public abstract PNode getPrimary();

    public CallAttributeNode(String name, PNode[] arguments) {
        this.attributeId = name;
        this.arguments = arguments;
    }

    protected CallAttributeNode(CallAttributeNode node) {
        this(node.attributeId, node.arguments);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callee=" + getPrimary() + "," + attributeId + ")";
    }

    @Specialization
    public Object doString(VirtualFrame frame, String prim) {
        Object[] args = doArguments(frame);
        PString primString = new PString(prim);
        PythonCallable callable = applyBuiltinMethodDescriptor(primString, primString.__getattribute__(attributeId));
        return callable.call(frame.pack(), args);
    }

    @Specialization
    public Object doPythonBuiltinObject(VirtualFrame frame, PythonBuiltinObject prim) {
        Object[] args = doArguments(frame);
        PythonCallable callable = applyBuiltinMethodDescriptor(prim, prim.__getattribute__(attributeId));
        return callable.call(frame.pack(), args);
    }

    @Specialization
    public Object doPythonModule(VirtualFrame frame, PythonModule prim) {
        Object[] args = doArguments(frame);
        Object attribute = prim.getAttribute(attributeId);
        return ((PythonCallable) attribute).call(frame.pack(), args);
    }

    @Specialization
    public Object doPythonClass(VirtualFrame frame, PythonClass prim) {
        Object[] args = doArguments(frame);
        PythonCallable callable = (PythonCallable) prim.getAttribute(attributeId);
        return callable.call(frame.pack(), args);
    }

    @Specialization
    public Object doPythonObject(VirtualFrame frame, PythonObject prim) {
        Object[] args = doArguments(frame);
        PythonCallable callable = applyMethodDescriptor(prim, (PythonCallable) prim.getAttribute(attributeId));
        return callable.call(frame.pack(), args);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object prim) {

        Object[] args = doArguments(frame);

        PyObject primary;
        if (prim instanceof PyObject) {
            primary = (PyObject) prim;
        } else if (prim instanceof Iterator<?>) {
            Iterator<?> iterator = (Iterator<?>) prim;
            return iterator.next();
        } else if (prim instanceof PythonModule) {
            Object attribute = ((PythonModule) prim).getAttribute(attributeId);
            return ((PythonCallable) attribute).call(frame.pack(), args);
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

    /**
     * Ugly, but works for now.<br>
     * Mimic the behavior of method descriptor to bind built-in and user functions.
     */
    protected PythonCallable applyMethodDescriptor(PythonObject primaryObj, PythonCallable attribute) {
        if (attribute instanceof PFunction) {
            CompilerDirectives.transferToInterpreter();
            return createPMethodFor(primaryObj, (PFunction) attribute);
        }

        return attribute;
    }

    public static PMethod createPMethodFor(PythonObject primaryObj, PFunction function) {
        RootNode root = (RootNode) function.getFunctionRootNode().copy();
        redirectFirstArgumentToSelf(root);
        return new PMethod(primaryObj, PFunction.duplicate(function, Truffle.getRuntime().createCallTarget(root)));
    }

    protected PythonCallable applyBuiltinMethodDescriptor(PythonBuiltinObject primaryObj, PythonCallable callable) {
        if (callable instanceof PBuiltinFunction) {
            CompilerDirectives.transferToInterpreter();
            return createPBuiltinMethodFor(primaryObj, (PBuiltinFunction) callable);
        }

        return callable;
    }

    public static PBuiltinMethod createPBuiltinMethodFor(PythonBuiltinObject primaryObj, PBuiltinFunction function) {
        PBuiltinFunction copied = function.duplicate();
        RootNode root = copied.getFunctionRootNode();
        redirectFirstArgumentToSelf(root);
        return new PBuiltinMethod(primaryObj, copied);
    }

    private static void redirectFirstArgumentToSelf(RootNode root) {
        /**
         * No need to redirect argument access.
         */
        if (NodeUtil.findFirstNodeInstance(root, ReadSelfArgumentNode.class) != null) {
            return;
        }

        List<ReadIndexedArgumentNode> argReads = NodeUtil.findAllNodeInstances(root, ReadIndexedArgumentNode.class);

        for (ReadIndexedArgumentNode read : argReads) {
            if (read.getIndex() == 0) {
                read.replace(new ReadSelfArgumentNode());
            } else {
                int index = read.getIndex();
                if (read instanceof ReadVarArgsNode) {
                    read.replace(new ReadVarArgsNode(index - 1));
                } else {
                    read.replace(ReadIndexedArgumentNode.create(index - 1));
                }
            }
        }
    }
}
