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

public class PythonProfiler {

    private final PythonContext context;
    private List<ProfilerNode> nodes;

    public PythonProfiler(PythonContext context) {
        this.context = context;
        nodes = new ArrayList<>();

    }

    public ProfilerNode probeAsNode(PNode node) {
        ProfilerNode wrapper = createWrapper(node);
        nodes.add(wrapper);
        return wrapper;
    }

    private ProfilerNode createWrapper(PNode node) {
        ProfilerNode wrapper;
        if (node instanceof ProfilerNode) {
            wrapper = (ProfilerNode) node;
        } else if (node.getParent() != null && node.getParent() instanceof PythonWrapperNode) {
            wrapper = (ProfilerNode) node.getParent();
        } else {
            wrapper = new ProfilerNode(node);
            wrapper.assignSourceSection(node.getSourceSection());
        }

        return wrapper;
    }

    public List<ProfilerNode> getNodes() {
        return nodes;
    }

    public PythonContext getContext() {
        return context;
    }
}
