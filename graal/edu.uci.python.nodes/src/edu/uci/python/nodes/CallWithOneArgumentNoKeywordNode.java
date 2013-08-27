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

import org.python.core.Py;
import org.python.core.PyObject;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.runtime.datatypes.*;
import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

@NodeChild("callee")
public abstract class CallWithOneArgumentNoKeywordNode extends PNode {

    public abstract PNode getCallee();

    @Child private PNode argument;

    public CallWithOneArgumentNoKeywordNode(PNode argument) {
        this.argument = adoptChild(argument);
    }

    protected CallWithOneArgumentNoKeywordNode(CallWithOneArgumentNoKeywordNode node) {
        this(node.argument);
    }

    @Specialization
    public Object doPCallable(VirtualFrame frame, PCallable callee) {
        Object arg = argument.execute(frame);
        return callee.call(frame.pack(), arg);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object callee) {
        Object arg = argument.execute(frame);

        if (callee instanceof PyObject) {
            PyObject pyarg = adaptToPyObject(arg);
            PyObject pyCallable = (PyObject) callee;
            return unboxPyObject(pyCallable.__call__(new PyObject[]{pyarg}));
        } else {
            throw Py.SystemError("Unexpected callable type" + callee.getClass());
        }
    }
}
