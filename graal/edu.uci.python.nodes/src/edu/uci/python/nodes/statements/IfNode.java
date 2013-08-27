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

import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.nodes.expressions.*;
import edu.uci.python.nodes.translation.*;

public class IfNode extends StatementNode {

    @Child protected final BooleanCastNode condition;

    @Child protected final BlockNode then;

    @Child protected final BlockNode orelse;

    public IfNode(BooleanCastNode condition, BlockNode then, BlockNode orelse) {
        this.condition = adoptChild(condition);
        this.then = adoptChild(then);
        this.orelse = adoptChild(orelse);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        if (condition.executeBoolean(frame)) {
            then.executeVoid(frame);
        } else {
            orelse.executeVoid(frame);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (condition.executeBoolean(frame)) {
            then.executeVoid(frame);
        } else {
            orelse.executeVoid(frame);
        }

        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + condition + ")";
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitIfNode(this);
    }
}
