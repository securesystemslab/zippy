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

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.attribute.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;

public class WithNode extends StatementNode {

    @Child protected PNode withContext;
    @Child protected PNode asName;
    @Child protected BlockNode body;

    @Child protected PNode enter;
    @Child protected PNode exit;

    private final PythonContext context;

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

        // RUN __enter__()
        PNode enterAttr = new GetAttributeNode.UninitializedGetAttributeNode(context, "__enter__", this.withContext);
        PNode enterCall = new UninitializedCallFunctionNode(enterAttr, new PNode[0], new KeywordLiteralNode[0], this.context);
        enter = adoptChild(enterCall);
        enter.execute(frame);

        try {
            body.execute(frame);
        } catch (RuntimeException e) {
            exception = e;
        } finally {
            // RUN __exit__(exception)
            PNode exitAttr = new GetAttributeNode.UninitializedGetAttributeNode(context, "__exit__", this.withContext);

            // TODO: write the exception to the variables
// FrameDescriptor fd = frame.getFrameDescriptor();
// PNode type = WriteLocalVariableNodeFactory.create(fd.findOrAddFrameSlot("type"), null);
// PNode value = WriteLocalVariableNodeFactory.create(fd.findOrAddFrameSlot("value"), null);
// PNode trace = WriteLocalVariableNodeFactory.create(fd.findOrAddFrameSlot("trace"), null);
            PNode[] exceptionArgs = new PNode[0];

            PNode[] args = (exception == null) ? new PNode[0] : exceptionArgs;
            PNode exitCall = new UninitializedCallFunctionNode(exitAttr, args, new KeywordLiteralNode[0], this.context);
            exit = adoptChild(exitCall);
            exit.execute(frame);
        }
        return null;
    }
}
