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

import java.math.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.function.*;

@NodeChild(value = "rightNode", type = PNode.class)
public abstract class WriteMaterializedFrameVariableNode extends FrameSlotNode implements WriteNode {

    public abstract PNode getRightNode();

    public WriteMaterializedFrameVariableNode(FrameSlot slot) {
        super(slot);
    }

    public WriteMaterializedFrameVariableNode(WriteMaterializedFrameVariableNode specialized) {
        this(specialized.frameSlot);
    }

    @Override
    public PNode makeReadNode() {
        return null;
    }

    @Override
    public PNode getRhs() {
        return getRightNode();
    }

    @Override
    public Object executeWrite(VirtualFrame frame, Object value) {
        return executeWith(frame, value);
    }

    public abstract Object executeWith(VirtualFrame frame, Object value);

    @Specialization(order = 0, guards = "isBooleanKind")
    public boolean write(VirtualFrame frame, boolean right) {
        MaterializedFrame mframe = PArguments.get(frame).getGeneratorFrame();
        mframe.setBoolean(frameSlot, right);
        return right;
    }

    @Specialization(guards = "isIntegerKind")
    public int doInteger(VirtualFrame frame, int value) {
        MaterializedFrame mframe = PArguments.get(frame).getGeneratorFrame();
        mframe.setInt(frameSlot, value);
        return value;
    }

    @Specialization(guards = "isIntOrObjectKind")
    public BigInteger write(VirtualFrame frame, BigInteger value) {
        MaterializedFrame mframe = PArguments.get(frame).getGeneratorFrame();
        setObject(mframe, value);
        return value;
    }

    @Specialization(guards = "isDoubleKind")
    public double doDouble(VirtualFrame frame, double right) {
        MaterializedFrame mframe = PArguments.get(frame).getGeneratorFrame();
        mframe.setDouble(frameSlot, right);
        return right;
    }

    @Specialization(guards = "isObjectKind")
    public PComplex write(VirtualFrame frame, PComplex right) {
        MaterializedFrame mframe = PArguments.get(frame).getGeneratorFrame();
        setObject(mframe, right);
        return right;
    }

    @Specialization(guards = "isObjectKind")
    public String write(VirtualFrame frame, String right) {
        MaterializedFrame mframe = PArguments.get(frame).getGeneratorFrame();
        setObject(mframe, right);
        return right;
    }

    @Specialization(guards = "isObjectKind")
    public Object write(VirtualFrame frame, Object right) {
        MaterializedFrame mframe = PArguments.get(frame).getGeneratorFrame();
        setObject(mframe, right);
        return right;
    }
}
