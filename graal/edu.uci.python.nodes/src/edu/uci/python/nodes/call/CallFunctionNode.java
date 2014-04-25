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

import org.python.core.*;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.profiler.*;
import edu.uci.python.profiler.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;
import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

@NodeChild(value = "callee", type = PNode.class)
public abstract class CallFunctionNode extends PNode {

    @Children protected final PNode[] arguments;
    @Children protected final KeywordLiteralNode[] keywords;
    private final PythonContext context;

    @Child private CallSiteProfilerNode callSiteProfiler;

    public CallFunctionNode(PNode[] arguments, KeywordLiteralNode[] keywords, PythonContext context) {
        this.arguments = arguments;
        this.keywords = keywords;
        this.context = context;
        if (PythonOptions.ProfileCallSites) {
            this.callSiteProfiler = new CallSiteProfilerNode(this);
        }
    }

    protected CallFunctionNode(CallFunctionNode node) {
        this(node.arguments, node.keywords, node.context);
    }

    public abstract PNode getCallee();

    public PNode[] getArguments() {
        return arguments;
    }

    public PythonContext getContext() {
        return context;
    }

    @Specialization
    public Object doPythonCallable(VirtualFrame frame, PythonCallable callee) {
        Object[] args = executeArguments(frame, arguments);
        PKeyword[] kwords = executeKeywordArguments(frame, keywords);

        if (PythonOptions.ProfileFunctionCalls) {
            Profiler.getInstance().increment(callee.getCallableName());
        }

        if (PythonOptions.ProfileCallSites) {
            callSiteProfiler.executeWithCallableName(frame, callee.getCallTarget().getRootNode());
        }

        return callee.call(frame.pack(), args, kwords);
    }

// @Specialization
// public Object doPythonObject(VirtualFrame frame, PythonObject callee) {
// /**
// * Calls __call__() method of an object if it exists. Otherwise, throws a type error
// */
// Object[] args = executeArguments(frame, arguments);
// PKeyword[] kwords = executeKeywordArguments(frame, keywords);
// Object callAttribute = callee.getAttribute("__call__");
//
// if (callAttribute instanceof PFunction) {
// PFunction callFunction = (PFunction) callAttribute;
// PMethod callMethod = CallAttributeNode.createPMethodFor(callee, callFunction);
//
// if (PythonOptions.ProfileFunctionCalls) {
// Profiler.getInstance().increment(callMethod.getCallableName());
// }
//
// if (PythonOptions.ProfileCallSites) {
// callSiteProfiler.executeWithCallableName(frame, callMethod.getCallTarget().getRootNode());
// }
//
// if (keywords.length == 0) {
// return callMethod.call(frame.pack(), args);
// } else {
// return callMethod.call(frame.pack(), args, kwords);
// }
// } else {
// throw Py.TypeError("'" + getPythonTypeName(callee) + "' object is not callable");
// }
// }
//
// @Specialization
// public Object doPyObject(VirtualFrame frame, PyObject callee) {
// Object[] args = executeArguments(frame, arguments);
// PyObject[] pyargs = adaptToPyObjects(args);
//
// if (PythonOptions.ProfileFunctionCalls) {
// Profiler.getInstance().increment(callee.toString());
// }
//
// return unboxPyObject(callee.__call__(pyargs));
// }

    @SuppressWarnings("unused")
    @Generic
    public Object doGeneric(VirtualFrame frame, Object callee) {
        throw Py.TypeError("'" + getPythonTypeName(callee) + "' object is not callable");
    }

    @ExplodeLoop
    public static final Object[] executeArguments(VirtualFrame frame, PNode[] arguments) {
        Object[] evaluated = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            evaluated[i] = arguments[i].execute(frame);
        }

        return evaluated;
    }

    @ExplodeLoop
    protected static final PKeyword[] executeKeywordArguments(VirtualFrame frame, PNode[] arguments) {
        PKeyword[] evaluated = new PKeyword[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            evaluated[i] = (PKeyword) arguments[i].execute(frame);
        }

        return evaluated;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callee=" + getCallee() + ")";
    }

}
