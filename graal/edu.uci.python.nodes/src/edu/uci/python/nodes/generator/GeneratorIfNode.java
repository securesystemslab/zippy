/*
 * Copyright (c) 2014, Regents of the University of California
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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;

public final class GeneratorIfNode extends IfNode {

    private final int thenFlagSlot;
    private final int elseFlagSlot;

    public GeneratorIfNode(CastToBooleanNode condition, PNode then, PNode orelse, int thenFlagSlot, int elseFlagSlot) {
        super(condition, then, orelse);
        this.thenFlagSlot = thenFlagSlot;
        this.elseFlagSlot = elseFlagSlot;
    }

    public int getThenFlagSlot() {
        return thenFlagSlot;
    }

    public int getElseFlagSlot() {
        return elseFlagSlot;
    }

    private boolean isThenActive(VirtualFrame frame) {
        return PArguments.getGeneratorArguments(frame).getActive(thenFlagSlot);
    }

    private void setThenActive(VirtualFrame frame, boolean flag) {
        PArguments.getGeneratorArguments(frame).setActive(thenFlagSlot, flag);
    }

    private boolean isElseActive(VirtualFrame frame) {
        return PArguments.getGeneratorArguments(frame).getActive(elseFlagSlot);
    }

    private void setElseActive(VirtualFrame frame, boolean flag) {
        PArguments.getGeneratorArguments(frame).setActive(elseFlagSlot, flag);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (isThenActive(frame)) {
            then.execute(frame);
            setThenActive(frame, false);
            return PNone.NONE;
        }

        if (isElseActive(frame)) {
            orelse.execute(frame);
            setElseActive(frame, false);
            return PNone.NONE;
        }

        if (condition.executeBoolean(frame)) {
            setThenActive(frame, true);
            then.execute(frame);
            setThenActive(frame, false);
        } else {
            setElseActive(frame, true);
            orelse.execute(frame);
            setElseActive(frame, false);
        }

        return PNone.NONE;
    }

}
