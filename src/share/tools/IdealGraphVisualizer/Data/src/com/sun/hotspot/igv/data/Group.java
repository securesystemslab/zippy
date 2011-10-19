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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Thomas Wuerthinger
 */
public class Group extends Properties.Entity implements ChangedEventProvider<Group> {

    private final List<InputGraph> graphs;

    private InputMethod method;
    private String assembly;
    private transient ChangedEvent<Group> changedEvent;
    private transient boolean complete = true;

    public Group() {
        graphs = Collections.synchronizedList(new ArrayList<InputGraph>());
        changedEvent = new ChangedEvent<Group>(this);

        // Ensure that name and type are never null
        getProperties().setProperty("name", "");
        getProperties().setProperty("type", "");
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isComplete() {
        return complete;
    }

    public void fireChangedEvent() {
        changedEvent.fire();
    }

    public void setAssembly(String s) {
        this.assembly = s;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setMethod(InputMethod method) {
        this.method = method;
    }

    public InputMethod getMethod() {
        return method;
    }

    public ChangedEvent<Group> getChangedEvent() {
        return changedEvent;
    }

    public List<InputGraph> getGraphs() {
        return Collections.unmodifiableList(graphs);
    }

    public int getGraphsCount() {
        return graphs.size();
    }

    public List<InputGraph> getGraphListCopy() {
        synchronized (graphs) {
            return new ArrayList<InputGraph>(graphs);
        }
    }

    public void addGraph(InputGraph graph) {
        synchronized (graphs) {
            graph.setParent(this, graphs.size());
            graphs.add(graph);
        }
        changedEvent.fire();
    }

    public InputGraph addGraph(String name) {
        return addGraph(name, null);
    }

    public InputGraph addGraph(String name, Pair<InputGraph, InputGraph> pair) {
        InputGraph g;
        synchronized (graphs) {
            g = new InputGraph(graphs.size(), this, name, pair);
            graphs.add(g);
        }
        changedEvent.fire();
        return g;
    }

    public void removeGraph(InputGraph g) {
        if (graphs.remove(g)) {
            changedEvent.fire();
        }
    }

    public Set<Integer> getAllNodes() {
        Set<Integer> result = new HashSet<Integer>();
        synchronized (graphs) {
            for (InputGraph g : graphs) {
                result.addAll(g.getNodesAsSet());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Group " + getProperties().toString() + "\n");
        synchronized (graphs) {
            for (InputGraph g : graphs) {
                sb.append(g.toString());
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public String getName() {
        return getProperties().get("name");
    }

    public String getType() {
        return getProperties().get("type");
    }
}
