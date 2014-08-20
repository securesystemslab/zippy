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

public class PythonProfilerNodeProber implements ASTNodeProber {

    private final PythonContext context;
    private List<ProfilerInstrument> nodeInstruments;
    private List<ProfilerInstrument> callInstruments;
    private List<ProfilerInstrument> loopInstruments;
    private Map<ProfilerInstrument, List<ProfilerInstrument>> ifInstruments;

    public PythonProfilerNodeProber(PythonContext context) {
        this.context = context;
        nodeInstruments = new ArrayList<>();
        callInstruments = new ArrayList<>();
        loopInstruments = new ArrayList<>();
        ifInstruments = new LinkedHashMap<>();
    }

    public Node probeAs(Node astNode, PhylumTag tag, Object... args) {
        return astNode;
    }

    public PythonWrapperNode probeAsStatement(PNode node) {
        PythonWrapperNode wrapper = createWrapper(node);
        wrapper.getProbe().tagAs(StandardTag.STATEMENT);
        ProfilerInstrument profilerInstrument = createAttachProfilerInstrument(wrapper);
        nodeInstruments.add(profilerInstrument);
        return wrapper;
    }

    public PythonWrapperNode probeAsCall(PNode node) {
        PythonWrapperNode wrapper = createWrapper(node);
        wrapper.getProbe().tagAs(StandardTag.CALL);
        ProfilerInstrument profilerInstrument = createAttachProfilerInstrument(wrapper);
        callInstruments.add(profilerInstrument);
        return wrapper;
    }

    public PythonWrapperNode probeAsLoop(PNode node) {
        PythonWrapperNode wrapper = createWrapper(node);
        ProfilerInstrument profilerInstrument = createAttachProfilerInstrument(wrapper);
        loopInstruments.add(profilerInstrument);
        return wrapper;
    }

    public List<PythonWrapperNode> probeAsIf(PNode ifNode, PNode thenNode, PNode elseNode) {
        List<PythonWrapperNode> wrappers = new ArrayList<>();
        PythonWrapperNode ifWrapper = createWrapper(ifNode);
        PythonWrapperNode thenWrapper = createWrapper(thenNode);
        PythonWrapperNode elseWrapper = createWrapper(elseNode);
        wrappers.add(ifWrapper);
        wrappers.add(thenWrapper);
        wrappers.add(elseWrapper);

        List<ProfilerInstrument> instruments = new ArrayList<>();
        ProfilerInstrument ifInstrument = createAttachProfilerInstrument(ifWrapper);
        ProfilerInstrument thenInstrument = createAttachProfilerInstrument(thenWrapper);
        ProfilerInstrument elseInstrument = createAttachProfilerInstrument(elseWrapper);
        instruments.add(thenInstrument);
        instruments.add(elseInstrument);
        ifInstruments.put(ifInstrument, instruments);

        return wrappers;
    }

    public List<PythonWrapperNode> probeAsIfWithoutElse(PNode ifNode, PNode thenNode) {
        List<PythonWrapperNode> wrappers = new ArrayList<>();
        PythonWrapperNode ifWrapper = createWrapper(ifNode);
        PythonWrapperNode thenWrapper = createWrapper(thenNode);
        wrappers.add(ifWrapper);
        wrappers.add(thenWrapper);

        List<ProfilerInstrument> instruments = new ArrayList<>();
        ProfilerInstrument ifInstrument = createAttachProfilerInstrument(ifWrapper);
        ProfilerInstrument thenInstrument = createAttachProfilerInstrument(thenWrapper);
        instruments.add(thenInstrument);
        ifInstruments.put(ifInstrument, instruments);
        return wrappers;
    }

    private PythonWrapperNode createWrapper(PNode node) {
        PythonWrapperNode wrapper;
        if (node instanceof PythonWrapperNode) {
            wrapper = (PythonWrapperNode) node;
        } else if (node.getParent() != null && node.getParent() instanceof PythonWrapperNode) {
            wrapper = (PythonWrapperNode) node.getParent();
        } else {
            wrapper = new PythonWrapperNode(context, node);
            wrapper.assignSourceSection(node.getSourceSection());
        }

        return wrapper;
    }

    private static ProfilerInstrument createAttachProfilerInstrument(PythonWrapperNode wrapper) {
        ProfilerInstrument profilerInstrument = new ProfilerInstrument();
        wrapper.getProbe().addInstrument(profilerInstrument);
        return profilerInstrument;
    }

    public List<ProfilerInstrument> getNodeInstruments() {
        return nodeInstruments;
    }

    public List<ProfilerInstrument> getCallInstruments() {
        return callInstruments;
    }

    public List<ProfilerInstrument> getLoopInstruments() {
        return loopInstruments;
    }

    public Map<ProfilerInstrument, List<ProfilerInstrument>> getIfInstruments() {
        return ifInstruments;
    }

    public PythonContext getContext() {
        return context;
    }
}
