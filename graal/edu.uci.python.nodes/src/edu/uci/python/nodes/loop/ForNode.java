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

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.sequence.*;

@NodeChild(value = "iterator", type = PNode.class)
public abstract class ForNode extends LoopNode {

    @Child protected PNode target;

    public ForNode(PNode target, PNode body) {
        super(body);
        this.target = adoptChild(target);
    }

    protected ForNode(ForNode previous) {
        this(previous.target, previous.body);
    }

    public abstract PNode getIterator();

    @Specialization
    public Object doPSequence(VirtualFrame frame, PSequence sequence) {
        loopOnIterator(frame, sequence.iterator());
        return PNone.NONE;
    }

    @Specialization
    public Object doPBaseSet(VirtualFrame frame, PBaseSet set) {
        loopOnIterator(frame, set.iterator());
        return PNone.NONE;
    }

    @Specialization
    public Object doString(VirtualFrame frame, String string) {
        PString pstring = new PString(string);
        loopOnIterator(frame, pstring.iterator());
        return PNone.NONE;
    }

    @Specialization
    public Object doPIterator(VirtualFrame frame, PIterator iterator) {
        Iterator result = iterator.evaluateToJavaIteratore(frame);
        loopOnIterator(frame, result);
        return PNone.NONE;
    }

    @Specialization
    public Object doIterator(VirtualFrame frame, Iterator iterator) {
        loopOnIterator(frame, iterator);
        return PNone.NONE;
    }

    @Specialization
    public Object doEnumerate(VirtualFrame frame, PEnumerate enumerate) {
        loopOnIterator(frame, enumerate.iterator());
        return PNone.NONE;
    }

    protected void loopOnIterator(VirtualFrame frame, Iterator iter) {
        RuntimeValueNode rvn = (RuntimeValueNode) ((WriteNode) target).getRhs();

        while (iter.hasNext()) {
            rvn.setValue(iter.next());
            target.execute(frame);
            body.executeVoid(frame);
        }
    }
}
