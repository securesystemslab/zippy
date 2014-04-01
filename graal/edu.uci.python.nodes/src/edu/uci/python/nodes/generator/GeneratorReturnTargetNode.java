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

public final class GeneratorReturnTargetNode extends ReturnTargetNode {

    @Child protected PNode parameters;

    public GeneratorReturnTargetNode(PNode parameters, PNode body, PNode returnValue) {
        super(body, returnValue);
        this.parameters = parameters;
    }

    public PNode getParameters() {
        return parameters;
    }

    private static boolean getFirstEntry(VirtualFrame frame) {
        return PArguments.getGeneratorArguments(frame).isFirstEntry();
    }

    private static void setFirstEntry(VirtualFrame frame, boolean value) {
        PArguments.getGeneratorArguments(frame).setFirstEntry(value);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (getFirstEntry(frame)) {
            parameters.executeVoid(frame);
            setFirstEntry(frame, false);
        }

        try {
            body.execute(frame);
            setFirstEntry(frame, true);
        } catch (YieldException eye) {
            return returnValue.execute(frame);
        } catch (ReturnException ire) {
            // return statement in generators throws StopIteration immediately.
        }

        throw StopIterationException.INSTANCE;
    }

}
