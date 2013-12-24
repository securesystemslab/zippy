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
package edu.uci.python.nodes.statement;

import java.util.List;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.argument.*;
import edu.uci.python.runtime.function.*;

public final class ParametersWithDefaultsNode extends ParametersNode {

    protected final ReadDefaultArgumentNode[] defaultReads;

    @Children protected final PNode[] parameters;

    @Children protected final PNode[] defaultWrites;

    public ParametersWithDefaultsNode(PNode[] parameters, List<String> paramNames, ReadDefaultArgumentNode[] defaultReads, PNode[] defaultWrites) {
        super(paramNames);
        this.defaultReads = defaultReads;
        this.parameters = adoptChildren(parameters);
        this.defaultWrites = adoptChildren(defaultWrites);
    }

    public PNode[] getDefaultReads() {
        return defaultReads;
    }

    @Override
    public void evaluateDefaults(VirtualFrame frame) {
        for (ReadDefaultArgumentNode rdan : defaultReads) {
            rdan.evaluateDefault(frame);
        }
    }

    /**
     * invoked when CallTarget is called, applies runtime arguments to the newly created
     * VirtualFrame.
     */
    @ExplodeLoop
    @Override
    public void executeVoid(VirtualFrame frame) {
        PArguments args = frame.getArguments(PArguments.class);
        Object[] values = args.getArgumentsArray();

        // apply defaults
        for (PNode write : defaultWrites) {
            write.executeVoid(frame);
        }

        // update parameters
        for (int i = 0; i < parameters.length; i++) {
            if (i < values.length) {
                Object val = values[i];

                if (val != null) {
                    parameters[i].executeVoid(frame);
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + parameterNames + ")";
    }
}
