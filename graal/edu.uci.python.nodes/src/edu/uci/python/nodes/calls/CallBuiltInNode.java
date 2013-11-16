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

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.function.*;
import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

public abstract class CallBuiltInNode extends PNode {

    protected final String name;

    protected final PythonCallable callee;

    @Children protected final PNode[] arguments;

    @Children protected final PNode[] keywords;

    public CallBuiltInNode(PythonCallable callee, String name, PNode[] arguments, PNode[] keywords) {
        this.callee = callee;
        this.name = name;
        this.arguments = adoptChildren(arguments);
        this.keywords = adoptChildren(keywords);
    }

    protected CallBuiltInNode(CallBuiltInNode node) {
        this(node.callee, node.name, node.arguments, node.keywords);
    }

    public String getName() {
        return name;
    }

    public PNode[] getArguments() {
        return arguments;
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        Object[] args = executeArguments(frame, arguments);

        if (keywords.length == 0) {
            return callee.call(frame.pack(), args);
        }

        PKeyword[] kwords = CallFunctionNode.executeKeywordArguments(frame, keywords);
        return callee.call(frame.pack(), args, kwords);
    }

    @ExplodeLoop
    private static Object[] executeArguments(VirtualFrame frame, PNode[] arguments) {
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
        return getClass().getSimpleName() + "(callee=" + name + ")";
    }
}
