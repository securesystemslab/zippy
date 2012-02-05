/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 */
package com.oracle.graal.visualizer.editor;

import com.sun.hotspot.igv.data.*;
import com.sun.hotspot.igv.difference.Difference;
import com.sun.hotspot.igv.filter.CustomFilter;
import com.sun.hotspot.igv.filter.FilterChain;
import com.sun.hotspot.igv.graph.Diagram;
import com.sun.hotspot.igv.graph.Figure;
import com.sun.hotspot.igv.settings.Settings;
import java.util.*;

public class DiagramViewModel {

    private Set<Integer> hiddenNodes;
    private Set<Integer> onScreenNodes;
    private Set<Integer> selectedNodes;
    private FilterChain filterChain;
    private FilterChain sequenceFilterChain;
    private Diagram diagram;
    private InputGraph inputGraph;
    private ChangedEvent<DiagramViewModel> diagramChangedEvent;
    private ChangedEvent<DiagramViewModel> viewChangedEvent;
    private ChangedEvent<DiagramViewModel> hiddenNodesChangedEvent;
    private ChangedEvent<DiagramViewModel> viewPropertiesChangedEvent;
    private boolean showNodeHull;
    private final InputGraph secondGraph;
    private final InputGraph firstGraph;
    private ChangedListener<FilterChain> filterChainChangedListener = new ChangedListener<FilterChain>() {

        @Override
        public void changed(FilterChain source) {
            diagramChanged();
        }
    };

    public boolean getShowNodeHull() {
        return showNodeHull;
    }

    public void setShowNodeHull(boolean b) {
        showNodeHull = b;
        viewPropertiesChangedEvent.fire();
    }

    public DiagramViewModel(InputGraph firstGraph, InputGraph secondGraph, Group g, FilterChain filterChain, FilterChain sequenceFilterChain) {

        this.firstGraph = firstGraph;
        this.secondGraph = secondGraph;
        this.showNodeHull = true;
        assert filterChain != null;
        this.filterChain = filterChain;
        assert sequenceFilterChain != null;
        this.sequenceFilterChain = sequenceFilterChain;
        hiddenNodes = new HashSet<>();
        onScreenNodes = new HashSet<>();
        selectedNodes = new HashSet<>();
        diagramChangedEvent = new ChangedEvent<>(this);
        viewChangedEvent = new ChangedEvent<>(this);
        hiddenNodesChangedEvent = new ChangedEvent<>(this);
        viewPropertiesChangedEvent = new ChangedEvent<>(this);

        filterChain.getChangedEvent().addListener(filterChainChangedListener);
        sequenceFilterChain.getChangedEvent().addListener(filterChainChangedListener);
    }

    public ChangedEvent<DiagramViewModel> getDiagramChangedEvent() {
        return diagramChangedEvent;
    }

    public ChangedEvent<DiagramViewModel> getViewChangedEvent() {
        return viewChangedEvent;
    }

    public ChangedEvent<DiagramViewModel> getHiddenNodesChangedEvent() {
        return hiddenNodesChangedEvent;
    }

    public ChangedEvent<DiagramViewModel> getViewPropertiesChangedEvent() {
        return viewPropertiesChangedEvent;
    }

    public Set<Integer> getSelectedNodes() {
        return selectedNodes;
    }

    public Set<Integer> getHiddenNodes() {
        return hiddenNodes;
    }

    public Set<Integer> getOnScreenNodes() {
        return onScreenNodes;
    }

    public void setSelectedNodes(Set<Integer> nodes) {
        this.selectedNodes = nodes;
       /* List<Color> colors = new ArrayList<>();
        for (int i = 0; i < group.getGraphs().size(); ++i) {
            colors.add(Color.black);
        }
        if (nodes.size() >= 1) {
            for (Integer id : nodes) {
                if (id < 0) {
                    id = -id;
                }
                InputNode last = null;
                int index = 0;
                for (InputGraph g : group.getGraphs()) {
                    Color curColor = colors.get(index);
                    InputNode cur = g.getNode(id);
                    if (cur != null) {
                        if (last == null) {
                            curColor = Color.green;
                        } else {
                            if (last.equals(cur)) {
                                if (curColor == Color.black) {
                                    curColor = Color.white;
                                }
                            } else {
                                if (curColor != Color.green) {
                                    curColor = Color.orange;
                                }
                            }
                        }
                    }
                    last = cur;
                    colors.set(index, curColor);
                    index++;
                }
            }
        }
        compilationViewModel.setColors(colors);*/
        // TODO: Add colorization.
        viewChangedEvent.fire();
    }

    public void showNot(final Set<Integer> nodes) {
        System.out.println("Shownot called with " + nodes);
        setHiddenNodes(nodes);
    }

    public void showFigures(Collection<Figure> f) {
        HashSet<Integer> newHiddenNodes = new HashSet<>(getHiddenNodes());
        for (Figure fig : f) {
            newHiddenNodes.removeAll(fig.getSource().getSourceNodesAsSet());
        }
        setHiddenNodes(newHiddenNodes);
    }

    public Set<Figure> getSelectedFigures() {
        Set<Figure> result = new HashSet<>();
        for (Figure f : diagram.getFigures()) {
            for (InputNode node : f.getSource().getSourceNodes()) {
                if (getSelectedNodes().contains(node.getId())) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    public void showAll(final Collection<Figure> f) {
        showFigures(f);
    }

    public void showOnly(final Set<Integer> nodes) {
        final HashSet<Integer> allNodes = new HashSet<>(getGraphToView().getGroup().getAllNodes());
        allNodes.removeAll(nodes);
        setHiddenNodes(allNodes);
    }

    public void setHiddenNodes(Set<Integer> nodes) {
        this.hiddenNodes = nodes;
        hiddenNodesChangedEvent.fire();
    }

    public void setOnScreenNodes(Set<Integer> onScreenNodes) {
        this.onScreenNodes = onScreenNodes;
        viewChangedEvent.fire();
    }

    public FilterChain getSequenceFilterChain() {
        return filterChain;
    }

    public void setSequenceFilterChain(FilterChain chain) {
        assert chain != null : "sequenceFilterChain must never be null";
        sequenceFilterChain.getChangedEvent().removeListener(filterChainChangedListener);
        sequenceFilterChain = chain;
        sequenceFilterChain.getChangedEvent().addListener(filterChainChangedListener);
        diagramChanged();
    }

    private void diagramChanged() {
        // clear diagram
        diagram = null;
        getDiagramChangedEvent().fire();

    }

    public FilterChain getFilterChain() {
        return filterChain;
    }

    public void setFilterChain(FilterChain chain) {
        assert chain != null : "filterChain must never be null";
        filterChain.getChangedEvent().removeListener(filterChainChangedListener);
        filterChain = chain;
        filterChain.getChangedEvent().addListener(filterChainChangedListener);
        diagramChanged();
    }

    private static List<String> calculateStringList(Group g) {
        List<String> result = new ArrayList<>();
        for (InputGraph graph : g.getGraphs()) {
            result.add(graph.getName());
        }
        return result;
    }

    public InputGraph getFirstGraph() {
        return firstGraph;
    }

    public InputGraph getSecondGraph() {
        return secondGraph;
    }

    public Diagram getDiagramToView() {

        if (diagram == null) {
            diagram = Diagram.createDiagram(getGraphToView(), Settings.get().get(Settings.NODE_TEXT, Settings.NODE_TEXT_DEFAULT));
            getFilterChain().apply(diagram, getSequenceFilterChain());
            if (getFirstGraph() != getSecondGraph()) {
                CustomFilter f = new CustomFilter(
                        "difference", "colorize('state', 'same', white);"
                        + "colorize('state', 'changed', orange);"
                        + "colorize('state', 'new', green);"
                        + "colorize('state', 'deleted', red);");
                f.apply(diagram);
            }
        }

        return diagram;
    }

    public InputGraph getGraphToView() {
        if (inputGraph == null) {
            if (getFirstGraph() != getSecondGraph()) {
                inputGraph = Difference.createDiffGraph(getFirstGraph(), getSecondGraph());
            } else {
                inputGraph = getFirstGraph();
            }
        }

        return inputGraph;
    }

    void setSelectedFigures(List<Figure> list) {
        Set<Integer> newSelectedNodes = new HashSet<>();
        for (Figure f : list) {
            newSelectedNodes.addAll(f.getSource().getSourceNodesAsSet());
        }
        this.setSelectedNodes(newSelectedNodes);
    }
}
