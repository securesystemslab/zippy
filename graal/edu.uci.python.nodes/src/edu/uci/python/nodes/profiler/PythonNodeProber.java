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
package edu.uci.python.nodes.profiler;

import java.util.*;

import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem
 */

public class PythonNodeProber implements ASTNodeProber {

    private final PythonContext context;

    private static Map<PythonWrapperNode, ProfilerInstrument> wrapperToInstruments = new HashMap<>();

    public PythonNodeProber(PythonContext context) {
        this.context = context;
    }

    public Node probeAs(Node astNode, PhylumTag tag, Object... args) {
        return astNode;
    }

    public PythonWrapperNode probeAsStatement(PNode node) {
        PythonWrapperNode wrapper;
        if (node instanceof PythonWrapperNode) {
            wrapper = (PythonWrapperNode) node;
        } else {
            wrapper = new PythonWrapperNode(context, node);
            wrapper.getProbe().tagAs(StandardTag.STATEMENT);
            wrapper.assignSourceSection(node.getSourceSection());
        }

        ProfilerInstrument profilerInstrument = new ProfilerInstrument();
        wrapper.getProbe().addInstrument(profilerInstrument);
        wrapperToInstruments.put(wrapper, profilerInstrument);
        return wrapper;
    }

    public PythonWriteNodeWrapperNode probeAsWriteNode(PNode node) {
        PythonWriteNodeWrapperNode wrapper;
        if (node instanceof PythonWriteNodeWrapperNode) {
            wrapper = (PythonWriteNodeWrapperNode) node;
        } else {
            wrapper = new PythonWriteNodeWrapperNode(context, node);
            wrapper.getProbe().tagAs(StandardTag.STATEMENT);
            wrapper.assignSourceSection(node.getSourceSection());
        }

        ProfilerInstrument profilerInstrument = new ProfilerInstrument();
        wrapper.getProbe().addInstrument(profilerInstrument);
        wrapperToInstruments.put(wrapper, profilerInstrument);
        return wrapper;
    }

    public static Map<PythonWrapperNode, ProfilerInstrument> getWrapperToInstruments() {
        return wrapperToInstruments;
    }
}
