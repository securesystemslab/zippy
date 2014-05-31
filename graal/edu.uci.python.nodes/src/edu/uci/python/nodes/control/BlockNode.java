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
package edu.uci.python.nodes.control;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.statement.*;

public class BlockNode extends StatementNode {

    @Children protected final PNode[] statements;

    protected BlockNode(PNode[] statements) {
        this.statements = statements;
        assert statements.length > 0;
    }

    public static PNode create(PNode... statements) {
        final int length = statements.length;

        if (length == 0) {
            return EmptyNode.create();
        } else if (length == 1) {
            return statements[0] instanceof YieldNode ? new BlockNode(statements) : statements[0];
        } else {
            return new BlockNode(statements);
        }
    }

    public final PNode[] getStatements() {
        return statements;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
        Object result = null;

        for (int i = 0; i < statements.length; i++) {
            result = statements[i].execute(frame);
        }

        return result;
    }

}
