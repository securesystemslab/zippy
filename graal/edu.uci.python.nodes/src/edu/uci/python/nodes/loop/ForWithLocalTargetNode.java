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
package edu.uci.python.nodes.loop;

import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.runtime.datatypes.*;

@NodeChild(value = "iterator", type = PNode.class)
public abstract class ForWithLocalTargetNode extends LoopNode {

    @Child protected WriteLocalVariableNode target;

    public ForWithLocalTargetNode(WriteLocalVariableNode target, PNode body) {
        super(body);
        this.target = adoptChild(target);
    }

    protected ForWithLocalTargetNode(ForWithLocalTargetNode previous) {
        this(previous.target, previous.body);
    }

    public abstract PNode getIterator();

    @Specialization(order = 0)
    public Object doPRange(VirtualFrame frame, PRange range) {
        final int start = range.getStart();
        final int stop = range.getStop();
        final int step = range.getStep();
        int count = 0;

        try {
            for (int i = start; i < stop; i += step) {
                target.executeWith(frame, i);
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } finally {
            if (CompilerDirectives.inInterpreter()) {
                reportLoopCount(count);
            }
        }

        return PNone.NONE;
    }

    @Specialization(order = 1)
    public Object doPSequence(VirtualFrame frame, PSequence sequence) {
        loopOnIterator(frame, sequence.iterator());
        return PNone.NONE;
    }

    @Specialization(order = 2)
    public Object doPBaseSet(VirtualFrame frame, PBaseSet set) {
        loopOnIterator(frame, set.iterator());
        return PNone.NONE;
    }

    private void loopOnIterator(VirtualFrame frame, Iterator iter) {
        int count = 0;

        try {
            while (iter.hasNext()) {
                target.executeWith(frame, iter.next());
                body.executeVoid(frame);

                if (CompilerDirectives.inInterpreter()) {
                    count++;
                }
            }
        } finally {
            if (CompilerDirectives.inInterpreter()) {
                reportLoopCount(count);
            }
        }
    }

    @Specialization
    public Object doPIterator(VirtualFrame frame, PIterator iterator) {
        Iterator result = iterator.evaluateToJavaIteratore(frame);
        loopOnIterator(frame, result);
        return PNone.NONE;
    }
}
