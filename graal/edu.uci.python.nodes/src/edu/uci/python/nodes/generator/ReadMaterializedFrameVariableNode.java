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

import org.python.core.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.runtime.function.*;

public abstract class ReadMaterializedFrameVariableNode extends FrameSlotNode implements ReadNode {

    public ReadMaterializedFrameVariableNode(FrameSlot slot) {
        super(slot);
    }

    public ReadMaterializedFrameVariableNode(ReadMaterializedFrameVariableNode specialized) {
        this(specialized.frameSlot);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return WriteMaterializedFrameVariableNodeFactory.create(frameSlot, rhs);
    }

    @Specialization(order = 1, guards = "isNotIllegal", rewriteOn = {FrameSlotTypeException.class})
    public int doInteger(VirtualFrame frame) throws FrameSlotTypeException {
        MaterializedFrame mframe = PArguments.get(frame).getMaterializedFrame();
        return getInteger(mframe);
    }

    @Specialization(order = 2, guards = "isNotIllegal", rewriteOn = {FrameSlotTypeException.class})
    public BigInteger doBigInteger(VirtualFrame frame) throws FrameSlotTypeException {
        MaterializedFrame mframe = PArguments.get(frame).getMaterializedFrame();
        return getBigInteger(mframe);
    }

    @Specialization(order = 3, guards = "isNotIllegal", rewriteOn = {FrameSlotTypeException.class})
    public double doDouble(VirtualFrame frame) throws FrameSlotTypeException {
        MaterializedFrame mframe = PArguments.get(frame).getMaterializedFrame();
        return getDouble(mframe);
    }

    @Specialization(order = 4, guards = "isNotIllegal", rewriteOn = {FrameSlotTypeException.class})
    public boolean doBoolean(VirtualFrame frame) throws FrameSlotTypeException {
        MaterializedFrame mframe = PArguments.get(frame).getMaterializedFrame();
        return getBoolean(mframe);
    }

    @Specialization(guards = "isNotIllegal")
    public Object doObject(VirtualFrame frame) {
        MaterializedFrame mframe = PArguments.get(frame).getMaterializedFrame();
        return getObject(mframe);
    }

    @SuppressWarnings("unused")
    @Generic
    public Object doGeneric(VirtualFrame frame) {
        assert !isNotIllegal();
        throw Py.UnboundLocalError("local variable '" + frameSlot.getIdentifier() + "' referenced before assignment");
    }

}
