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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;

public final class GeneratorReturnTargetNode extends ReturnTargetNode implements GeneratorControlNode {

    @Child protected PNode parameters;
    private final int flagSlot;

    public GeneratorReturnTargetNode(PNode parameters, PNode body, PNode returnValue, int activeFlagIndex) {
        super(body, returnValue);
        this.parameters = parameters;
        this.flagSlot = activeFlagIndex;
    }

    public PNode getParameters() {
        return parameters;
    }

    private boolean isActive(VirtualFrame frame) {
        return PArguments.getGeneratorArguments(frame).getActive(flagSlot);
    }

    private void setActive(VirtualFrame frame, boolean flag) {
        PArguments.getGeneratorArguments(frame).setActive(flagSlot, flag);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (!isActive(frame)) {
            parameters.executeVoid(frame);
            setActive(frame, true);
        }

        try {
            body.execute(frame);
        } catch (YieldException eye) {
            return returnValue.execute(frame);
        } catch (ReturnException ire) {
            // return statement in generators throws StopIteration.
        }

        throw StopIterationException.INSTANCE;
    }

}
