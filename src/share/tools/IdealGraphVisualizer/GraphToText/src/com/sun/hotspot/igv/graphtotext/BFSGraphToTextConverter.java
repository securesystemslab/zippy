/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.hotspot.igv.graphtotext;

import com.sun.hotspot.igv.data.InputEdge;
import com.sun.hotspot.igv.data.InputGraph;
import com.sun.hotspot.igv.data.InputNode;
import com.sun.hotspot.igv.data.Pair;
import com.sun.hotspot.igv.data.Properties;
import com.sun.hotspot.igv.graph.Diagram;
import com.sun.hotspot.igv.graphtotext.services.GraphToTextVisitor;
import com.sun.hotspot.igv.structuredtext.Element;
import com.sun.hotspot.igv.structuredtext.StructuredText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Thomas
 */
public class BFSGraphToTextConverter {
    
    private GraphToTextVisitor visitor;
    private Map<Properties.PropertyMatcher, GraphToTextVisitor> visitorMap;
    private InputGraph graph;
    private Diagram diagram;
    
    public BFSGraphToTextConverter(GraphToTextVisitor visitor) {
        this.visitor = visitor;
        visitorMap = new HashMap<Properties.PropertyMatcher, GraphToTextVisitor>();
    }
    
    public void registerVisitor(GraphToTextVisitor visitor, Properties.PropertyMatcher matcher) {
        visitorMap.put(matcher, visitor);
    }
    
    private GraphToTextVisitor chooseVisitor(GraphToTextVisitor defaultVisitor, InputNode node) {
        for(Properties.PropertyMatcher matcher : visitorMap.keySet()) {
            if(node.getProperties().selectSingle(matcher) != null) {
                return visitorMap.get(matcher);
            }
        }
        
        return defaultVisitor;
    }

    private Element cyclicVisit(GraphToTextVisitor visitor, InputNode node, List<InputEdge> path) {
        return chooseVisitor(visitor, node).cyclicVisit(node, path);
    }
    
    private Element visit(GraphToTextVisitor visitor, InputNode node, List<InputEdge> path, List<Pair<InputEdge, Element>> children) {
        return chooseVisitor(visitor, node).visit(node, path, children);
    }
    
    protected Diagram getDiagram() {
        return diagram;
    }
    
    public StructuredText convert(InputGraph graph, Diagram diagram) {
        
        this.graph = graph;
        this.diagram = diagram;
        StructuredText text = new StructuredText(graph.getName());
        
        Map<InputNode, List<InputEdge>> outgoing = graph.findAllOutgoingEdges();
        Map<InputNode, List<InputEdge>> pathMap = new HashMap<InputNode, List<InputEdge>>();
        Queue<InputNode> queue = new LinkedList<InputNode>();
        List<InputNode> rootNodes = graph.findRootNodes();
        queue.addAll(rootNodes);
        for(InputNode node : rootNodes) {
            pathMap.put(node, new ArrayList<InputEdge>());
        }
        
        Set<InputNode> visited = new HashSet<InputNode>();
        visited.addAll(rootNodes);
        
        Set<InputEdge> fullEdges = new HashSet<InputEdge>();
        List<InputNode> visitOrder = new ArrayList<InputNode>();
        while(!queue.isEmpty()) {
            
            InputNode current = queue.remove();
            visitOrder.add(current);
            List<InputEdge> path = pathMap.get(current);
            
            List<InputEdge> edges = outgoing.get(current);
            for(InputEdge e : edges) {
                InputNode dest = graph.getNode(e.getTo());
                if(!visited.contains(dest)) {
                    queue.add(dest);
                    visited.add(dest);
                    List<InputEdge> curPath = new ArrayList<InputEdge>(path);
                    curPath.add(e);
                    pathMap.put(dest, curPath);
                    fullEdges.add(e);
                }
            }
        }
        
        
        
        Map<InputNode, Element> fullVisitCache = new HashMap<InputNode, Element>();
        for(int i=visitOrder.size() - 1; i>=0; i--) {
            InputNode current = visitOrder.get(i);
            List<InputEdge> path = pathMap.get(current);
            List<InputEdge> edges = outgoing.get(current);
            List<Pair<InputEdge, Element>> list = new ArrayList<Pair<InputEdge, Element>>();
            for(InputEdge e : edges) {
                if(fullEdges.contains(e)) {
                    assert fullVisitCache.containsKey(graph.getNode(e.getTo()));
                    list.add(new Pair<InputEdge, Element>(e, fullVisitCache.get(graph.getNode(e.getTo()))));
                } else {
//                    assert fullVisitCache.containsKey(graph.getNode(e.getTo()));
                    List<InputEdge> curPath = new ArrayList<InputEdge>(path);
                    curPath.add(e);
                    list.add(new Pair<InputEdge, Element>(e, cyclicVisit(visitor, graph.getNode(e.getTo()), curPath)));
                }
            }
            
            Element e = visit(visitor, current, pathMap.get(current), list);
            fullVisitCache.put(current, e);
        }
        
        for(InputNode node : rootNodes) {
            text.addChild(fullVisitCache.get(node));
        }
        
        return text;
    }
}
