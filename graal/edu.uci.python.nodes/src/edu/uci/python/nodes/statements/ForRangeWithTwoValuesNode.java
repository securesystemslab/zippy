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
import edu.uci.python.nodes.utils.*;

public class ForRangeWithTwoValuesNode extends StatementNode {

    @Child protected StatementNode target;

    @Child protected PNode stop;

    @Child protected PNode start;

    @Child protected BlockNode body;

    @Child protected BlockNode orelse;

    public ForRangeWithTwoValuesNode(StatementNode target, PNode start, PNode stop, BlockNode body, BlockNode orelse) {
        this.target = adoptChild(target);
        this.stop = adoptChild(stop);
        this.start = adoptChild(start);
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }

    public void setInternal(BlockNode body, BlockNode orelse) {
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        // TODO need to check for bigInteger
        int stop = (int) this.stop.execute(frame);
        int start = (int) this.start.execute(frame);
        RuntimeValueNode rvn = (RuntimeValueNode) ((WriteNode) target).getRhs();

        for (int i = start; i < stop; i++) {
            // try {
            rvn.setValue(i);
            target.execute(frame);

            try {
                body.executeVoid(frame);
                if (reachedReturn() || isBreak()) {
                    this.setBreak(false);
                    return;
                }
            } catch (ContinueException ex) {
                // Fall through to next loop iteration.
            }
            // } catch (BreakException ex) {
            // Done executing this loop.
            // If there is a break, orelse should not be executed
            // return;
            // }
            orelse.executeVoid(frame);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        int stop = (int) this.stop.execute(frame);
        int start = (int) this.start.execute(frame);
        RuntimeValueNode rvn = (RuntimeValueNode) ((WriteNode) target).getRhs();

        for (int i = start; i < stop; i++) {
            // try {
            rvn.setValue(i);
            target.execute(frame);

            try {
                body.executeVoid(frame);
                if (reachedReturn() || isBreak()) {
                    this.setBreak(false);
                    return null;
                }
            } catch (ContinueException ex) {
                // Fall through to next loop iteration.
            }
            // } catch (BreakException ex) {
            // Done executing this loop.
            // If there is a break, orelse should not be executed
            // return;
            // }
            orelse.executeVoid(frame);
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
// PythonTree p = (PythonTree) target;
// p.visualize(level);
        body.visualize(level);
    }
}
