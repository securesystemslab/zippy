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
package edu.uci.python.nodes.statements;


import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;

public class PrintNode extends StatementNode {

    @Child PNode[] values;

    private final boolean nl;

    private StringBuilder out = null;

    public PrintNode(PNode[] values, boolean nl) {
        this.values = adoptChildren(values);
        this.nl = nl;
    }

    public void setOutStream(StringBuilder out) {
        this.out = out;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            PNode e = values[i];
            sb.append(e.execute(frame) + " ");
        }

        if (nl) {
            sb.append(System.getProperty("line.separator"));
        }

        // CheckStyle: stop system..print check
        System.out.print(sb.toString());
        // CheckStyle: resume system..print check

        if (out != null) {
            out.append(sb.toString());
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            PNode e = values[i];
            sb.append(e.execute(frame) + " ");
        }

        if (nl) {
            sb.append(System.getProperty("line.separator"));
        }

        // CheckStyle: stop system..print check
        System.out.print(sb.toString());
        // CheckStyle: resume system..print check

        if (out != null) {
            out.append(sb.toString());
        }

        return null;
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            ASTInterpreter.trace("    ");
        }
        ASTInterpreter.trace(this);

        level++;
        for (PNode val : values) {
            val.visualize(level);
        }
    }

}
