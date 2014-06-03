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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;

/**
 * @author zwei
 */
public final class GeneratorWhileNode extends WhileNode implements GeneratorControlNode {

    private final int flagSlot;
    private int count;

    public GeneratorWhileNode(CastToBooleanNode condition, PNode body, int flagSlot) {
        super(condition, body);
        this.flagSlot = flagSlot;
    }

    private boolean isActive(VirtualFrame frame) {
        return PArguments.getControlData(frame).getActive(flagSlot);
    }

    private void setActive(VirtualFrame frame, boolean flag) {
        PArguments.getControlData(frame).setActive(flagSlot, flag);
    }

    private void incrementCounter() {
        if (CompilerDirectives.inInterpreter()) {
            count++;
        }
    }

    private Object doReturn(VirtualFrame frame) {
        if (CompilerDirectives.inInterpreter()) {
            reportLoopCount(count);
            count = 0;
        }

        assert !isActive(frame);
        return PNone.NONE;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            while (isActive(frame) || condition.executeBoolean(frame)) {
                setActive(frame, true);
                body.executeVoid(frame);
                setActive(frame, false);
                incrementCounter();
            }
        } catch (BreakException ex) {
            setActive(frame, false);
        }

        return doReturn(frame);
    }

}
