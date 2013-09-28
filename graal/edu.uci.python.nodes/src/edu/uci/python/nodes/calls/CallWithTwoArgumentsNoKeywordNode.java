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

import org.python.core.Py;
import org.python.core.PyObject;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;

@NodeChild("callee")
public abstract class CallWithTwoArgumentsNoKeywordNode extends PNode {

    public abstract PNode getCallee();

    @Child private PNode arg0;

    @Child private PNode arg1;

    public CallWithTwoArgumentsNoKeywordNode(PNode arg0, PNode arg1) {
        this.arg0 = adoptChild(arg0);
        this.arg1 = adoptChild(arg1);
    }

    protected CallWithTwoArgumentsNoKeywordNode(CallWithTwoArgumentsNoKeywordNode node) {
        this(node.arg0, node.arg1);
    }

    @Specialization
    public Object doPCallable(VirtualFrame frame, PCallable callee) {
        Object a0 = arg0.execute(frame);
        Object a1 = arg1.execute(frame);
        return callee.call(frame.pack(), a0, a1);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame, Object callee) {
        Object a0 = arg0.execute(frame);
        Object a1 = arg1.execute(frame);

        if (callee instanceof PyObject) {
            PyObject[] pyargs = adaptToPyObjects(new Object[]{a0, a1});
            PyObject pyCallable = (PyObject) callee;
            return unboxPyObject(pyCallable.__call__(pyargs));
        } else {
            throw Py.SystemError("Unexpected callable type");
        }
    }

}
