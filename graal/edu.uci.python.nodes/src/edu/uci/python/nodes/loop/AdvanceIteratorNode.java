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

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.runtime.iterator.*;

@NodeChild(value = "getIterator", type = PNode.class)
public abstract class AdvanceIteratorNode extends PNode {

    @Child protected FrameSlotNode target;

    public AdvanceIteratorNode(FrameSlotNode target) {
        this.target = target;
    }

    protected AdvanceIteratorNode(AdvanceIteratorNode prev) {
        this(prev.target);
    }

    public abstract void executeWithIterator(VirtualFrame frame, Object iterator);

    public FrameSlotNode getTarget() {
        return target;
    }

    @Specialization(order = 0)
    public Object doInt(VirtualFrame frame, int value) {
        return target.executeWrite(frame, value);
    }

    @Specialization(order = 1)
    public Object doPIntegerIterator(VirtualFrame frame, PIntegerIterator iterator) {
        return target.executeWrite(frame, iterator.__nextInt__());
    }

    @Specialization(order = 2)
    public Object doPDoubleIterator(VirtualFrame frame, PDoubleIterator iterator) {
        return target.executeWrite(frame, iterator.__nextDouble__());
    }

    @Specialization(order = 3)
    public Object doPIterator(VirtualFrame frame, PIterator iterator) {
        return target.executeWrite(frame, iterator.__next__());
    }

}
