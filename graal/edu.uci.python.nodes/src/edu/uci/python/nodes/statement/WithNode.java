/*
 * Copyright (c) 2014, Regents of the University of California
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

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

/**
 * @author Qunaibit
 * 
 */

public class WithNode extends StatementNode {

    @Child protected PNode withContext;
    @Child protected PNode asName;
    @Child protected BlockNode body;

    @SuppressWarnings("unused") private final PythonContext context;

    protected WithNode(PythonContext context, PNode withContext, PNode asName, BlockNode body) {
        this.context = context;
        this.withContext = adoptChild(withContext);
        this.asName = adoptChild(asName);
        this.body = adoptChild(body);
    }

    public static WithNode create(PythonContext context, PNode withContext, PNode asName, BlockNode body) {
        return new WithNode(context, withContext, asName, body);
    }

    @Override
    public Object execute(VirtualFrame frame) {

        if (asName != null) {
            asName.execute(frame);
        }

        RuntimeException exception = null;

        PythonObject pythonObj = (PythonObject) this.withContext.execute(frame);
        PythonCallable enterCall = (PythonCallable) pythonObj.getAttribute("__enter__");
        enterCall.call(frame.pack(), new PNode[0]);

        try {
            body.execute(frame);
        } catch (RuntimeException e) {
            exception = e;
        } finally {

            PythonCallable exitCall = (PythonCallable) pythonObj.getAttribute("__exit__");

            if (exception instanceof ArithmeticException && exception.getMessage().endsWith("divide by zero")) {
                exception = Py.ZeroDivisionError("divide by zero");
            }

            if (exception instanceof PyException) {
                Object type = ((PyException) exception).type;
                Object value = ((PyException) exception).value;
                Object trace = ((PyException) exception).traceback;
                exitCall.call(frame.pack(), new Object[]{type, value, trace});
            } else if (exception == null) {
                exitCall.call(frame.pack(), new PNode[0]);
            } else {
                throw exception;
            }

        }
        return null;
    }
}
