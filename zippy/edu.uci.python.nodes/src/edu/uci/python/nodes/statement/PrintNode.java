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
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;

public class PrintNode extends StatementNode {

    @Children final PNode[] values;

    private final boolean nl;
    private final PythonContext context;

    public PrintNode(PNode[] values, boolean nl, PythonContext context) {
        this.values = values;
        this.nl = nl;
        this.context = context;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            PNode e = values[i];

            if (i == values.length - 1) {
                sb.append(executeToString(frame, e));
            } else {
                sb.append(executeToString(frame, e) + " ");
            }
        }

        if (nl) {
            sb.append(System.getProperty("line.separator"));
        }

        context.getStandardOut().print(sb.toString());
        return PNone.NONE;
    }

    private static String executeToString(VirtualFrame frame, PNode node) {
        Object value = node.execute(frame);

        if (value instanceof Boolean) {
            return (boolean) value ? "True" : "False";
        } else {
            return value.toString();
        }
    }
}
