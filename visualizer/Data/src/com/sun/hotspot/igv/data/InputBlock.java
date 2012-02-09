/*
 * Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */
package com.sun.hotspot.igv.data;

import at.ssw.visualizer.model.cfg.BasicBlock;
import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import at.ssw.visualizer.model.cfg.IRInstruction;
import at.ssw.visualizer.model.cfg.State;
import java.util.*;

/**
 *
 * @author Thomas Wuerthinger
 */
public class InputBlock implements BasicBlock {

    private List<InputNode> nodes;
    private String name;
    private InputGraph graph;
    private List<InputBlock> successors;
    private List<InputBlock> predecessors;

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (o == null || (!(o instanceof InputBlock))) {
            return false;
        }
        
        final InputBlock b = (InputBlock)o;
        final boolean result = b.nodes.equals(nodes) && b.name.equals(name) && b.successors.size() == successors.size();
        if (!result) {
            return false;
        }

        final HashSet<String> s = new HashSet<>();
        for (InputBlock succ : successors) {
            s.add(succ.name);
        }

        for (InputBlock succ : b.successors) {
            if (!s.contains(succ.name)) {
                return false;
            }
        }

        return true;
    }

    InputBlock(InputGraph graph, String name) {
        this.graph = graph;
        this.name = name;
        nodes = new ArrayList<>();
        successors = new ArrayList<>();
        predecessors = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<InputNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void addNode(int id) {
        InputNode n = graph.getNode(id);
        assert n != null;
        graph.setBlock(n, this);
        final InputNode node = graph.getNode(id);
        assert node != null;
        assert !nodes.contains(node) : "duplicate : " + node;
        nodes.add(node);
    }

    @Override
    public List<InputBlock> getSuccessors() {
        return Collections.unmodifiableList(successors);
    }

    @Override
    public String toString() {
        return "Block " + this.getName();
    }

    void addSuccessor(InputBlock b) {
        if (!successors.contains(b)) {
            successors.add(b);
            b.predecessors.add(this);
        }
    }

    @Override
    public int getFromBci() {
        // TODO(tw): Implement.
        return -1;
    }

    @Override
    public int getToBci() {
        // TODO(tw): Implement.
        return -1;
    }

    @Override
    public List<InputBlock> getPredecessors() {
        return Collections.unmodifiableList(predecessors);
    }

    @Override
    public List<BasicBlock> getXhandlers() {
        // TODO(tw): Implement.
        return Collections.emptyList();
    }

    @Override
    public List<String> getFlags() {
        // TODO(tw): Implement.
        return Collections.emptyList();
    }

    @Override
    public BasicBlock getDominator() {
        // TODO(tw): Implement.
        return null;
    }

    @Override
    public int getLoopIndex() {
        // TODO(tw): Implement.
        return -1;
    }

    @Override
    public int getLoopDepth() {
        // TODO(tw): Implement.
        return -1;
    }

    @Override
    public boolean hasState() {
        // TODO(tw): Implement.
        return false;
    }

    @Override
    public List<State> getStates() {
        // TODO(tw): Implement.
        return Collections.emptyList();
    }

    @Override
    public boolean hasHir() {
        // TODO(tw): Implement.
        return false;
    }

    @Override
    public List<IRInstruction> getHirInstructions() {
        // TODO(tw): Implement.
        return Collections.emptyList();
    }

    @Override
    public boolean hasLir() {
        // TODO(tw): Implement.
        return false;
    }

    @Override
    public List<IRInstruction> getLirOperations() {
        // TODO(tw): Implement.
        return Collections.emptyList();
    }
}
