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
package edu.uci.python.nodes.generator;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.loop.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;

public class GeneratorForNode extends LoopNode {

    @Child protected WriteMaterializedFrameVariableNode target;
    @Child protected GetIteratorNode getIterator;

    protected PIterator iterator;

    public GeneratorForNode(WriteMaterializedFrameVariableNode target, GetIteratorNode getIterator, PNode body) {
        super(body);
        this.target = adoptChild(target);
        this.getIterator = adoptChild(getIterator);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        executeIterator(frame);

        try {
            while (true) {
                body.executeVoid(frame);
                target.executeWith(frame, iterator.__next__());
            }
        } catch (StopIterationException e) {
            iterator = null;
            throw e;
        }
    }

    protected void executeIterator(VirtualFrame frame) {
        if (iterator != null) {
            return;
        }

        try {
            iterator = getIterator.executePIterator(frame);
            target.executeWith(frame, iterator.__next__());
        } catch (UnexpectedResultException e) {
            throw new RuntimeException();
        }
    }

    public static final class InnerGeneratorForNode extends GeneratorForNode {

        public InnerGeneratorForNode(WriteMaterializedFrameVariableNode target, GetIteratorNode getIterator, PNode body) {
            super(target, getIterator, body);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            executeIterator(frame);

            try {
                while (true) {
                    target.executeWith(frame, iterator.__next__());
                    body.executeVoid(frame);
                }
            } catch (StopIterationException e) {
                iterator = null;
            }

            return PNone.NONE;
        }

        @Override
        protected void executeIterator(VirtualFrame frame) {
            if (iterator != null) {
                return;
            }

            try {
                iterator = getIterator.executePIterator(frame);
            } catch (UnexpectedResultException e) {
                throw new RuntimeException();
            }
        }
    }
}
