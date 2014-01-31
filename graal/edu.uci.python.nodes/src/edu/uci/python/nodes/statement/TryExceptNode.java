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

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.runtime.*;

public abstract class TryExceptNode extends StatementNode {

    @Child protected BlockNode body;

    @Child protected BlockNode orelse;

    @Child protected PNode exceptType;
    @Child protected PNode exceptName;
    @Child protected BlockNode exceptBody;

    protected final PythonContext context;

    protected TryExceptNode(PythonContext context, BlockNode body, BlockNode orelse, PNode exceptType, PNode exceptName, BlockNode exceptBody) {
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);

        this.exceptName = adoptChild(exceptName);
        this.exceptType = adoptChild(exceptType);
        this.exceptBody = adoptChild(exceptBody);

        this.context = context;
    }

    public static TryExceptNode create(PythonContext context, BlockNode body, BlockNode orelse, PNode exceptType, PNode exceptName, BlockNode exceptBody) {
        if (exceptBody == null) {
            if (orelse == null) {
                return new TryOnlyNode(context, body);
            }
            return new TryElseNode(context, body, orelse);
        }
        return new ExceptOnlyNode(context, body, orelse, exceptType, exceptName, exceptBody);
    }

}

class TryOnlyNode extends TryExceptNode {

    protected TryOnlyNode(PythonContext context, BlockNode body) {
        super(context, body, null, null, null, null);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            return body.execute(frame);
        } catch (ControlFlowException c) {
            return null;
        }
    }
}

class ExceptOnlyNode extends TryExceptNode {

    protected ExceptOnlyNode(PythonContext context, BlockNode body, BlockNode orelse, PNode exceptType, PNode exceptName, BlockNode exceptBody) {
        super(context, body, orelse, exceptType, exceptName, exceptBody);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            return body.execute(frame);
        } catch (RuntimeException ex) {
            return executeExcept(frame, ex);
        }
    }

    protected Object executeExcept(VirtualFrame frame, RuntimeException excep) {
        PyException e = null;
        if (excep instanceof PyException) {
            e = (PyException) excep;
        } else if (excep instanceof ArithmeticException && excep.getMessage().endsWith("divide by zero")) {
            e = Py.ZeroDivisionError("divide by zero");
        } else {
            throw excep;
        }

        context.setCurrentException(e);

        /**
         * TODO: need to support exceptType instance of type e.g. 'divide by zero' instance of
         * 'Exception'
         * 
         * TODO: need to make exception messages consistent with Python 3.3 e.g. 'division by zero'
         */

        if (exceptType != null) {
            PyObject type = (PyObject) exceptType.execute(frame);
            if (e.type == type) {
                if (exceptName != null) {
                    ((WriteLocalVariableNode) exceptName).executeWith(frame, e);
                }
            } else {
                throw excep;
            }
        }

        exceptBody.execute(frame);

        // clear the exception after executing the except body.
        context.setCurrentException(null);
        throw new ControlFlowException();

    }
}

class TryElseNode extends TryExceptNode {

    protected TryElseNode(PythonContext context, BlockNode body, BlockNode orelse) {
        super(context, body, orelse, null, null, null);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            body.execute(frame);
        } catch (ControlFlowException c) {
            return null;
        }
        return orelse.execute(frame);
    }
}
