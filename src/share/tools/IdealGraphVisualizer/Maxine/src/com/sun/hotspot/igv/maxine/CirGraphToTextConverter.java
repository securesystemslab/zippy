/*
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.hotspot.igv.maxine;

import com.sun.hotspot.igv.data.InputEdge;
import com.sun.hotspot.igv.data.InputGraph;
import com.sun.hotspot.igv.data.InputNode;
import com.sun.hotspot.igv.data.Pair;
import com.sun.hotspot.igv.data.Properties;
import com.sun.hotspot.igv.data.Properties.PropertyMatcher;
import com.sun.hotspot.igv.data.Properties.RegexpPropertyMatcher;
import com.sun.hotspot.igv.data.Properties.StringPropertyMatcher;
import com.sun.hotspot.igv.graph.Diagram;
import com.sun.hotspot.igv.graph.Figure;
import com.sun.hotspot.igv.graphtotext.BFSGraphToTextConverter;
import com.sun.hotspot.igv.graphtotext.services.AbstractGraphToTextVisitor;
import com.sun.hotspot.igv.graphtotext.services.GraphToTextConverter;
import com.sun.hotspot.igv.graphtotext.services.GraphToTextVisitor;
import com.sun.hotspot.igv.structuredtext.Element;
import com.sun.hotspot.igv.structuredtext.MultiElement;
import com.sun.hotspot.igv.structuredtext.SimpleElement;
import com.sun.hotspot.igv.structuredtext.StructuredText;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author Thomas Wuerthinger
 */
public class CirGraphToTextConverter implements GraphToTextConverter {

    private static final String CALL_OPERATOR = " \u2022 ";

    private Map<InputNode, Set<Figure>> map;
    private InputGraph graph;

    public StructuredText convert(InputGraph graph, Diagram diagram) {
        map = diagram.calcSourceToFigureRelation();
        this.graph = graph;
        
        BFSGraphToTextConverter converter = new BFSGraphToTextConverter(nodeVisitor);
        converter.registerVisitor(localVariableVisitor, new StringPropertyMatcher("type", "LocalVariable"));
        converter.registerVisitor(parameterVisitor, new StringPropertyMatcher("type", "Parameter"));
        converter.registerVisitor(closureVisitor, new RegexpPropertyMatcher("type", "Closure"));
        converter.registerVisitor(continuationVisitor, new RegexpPropertyMatcher("type", "Continuation"));
        converter.registerVisitor(callVisitor, new RegexpPropertyMatcher("type", "Call"));
        converter.registerVisitor(blockVisitor, new RegexpPropertyMatcher("type", "Block"));
        return converter.convert(graph, diagram);
    }
    private GraphToTextVisitor nodeVisitor = new NodeVisitor();
    private GraphToTextVisitor localVariableVisitor = new NodeVisitor();
    private GraphToTextVisitor parameterVisitor = new NodeVisitor();
    private GraphToTextVisitor closureVisitor = new ClosureVisitor("proc");
    private GraphToTextVisitor continuationVisitor = new ClosureVisitor("cont");
    private GraphToTextVisitor callVisitor = new CallVisitor();
    private GraphToTextVisitor blockVisitor = new BlockVisitor();

    private void printOffset(List<InputEdge> path, MultiElement elem) {
        for (int i = 0; i < path.size(); i++) {
            InputEdge cur = path.get(i);
            InputNode fromNode = graph.getNode(cur.getFrom());
            SimpleElement simpleElem = new SimpleElement(" ", calcStyle(fromNode));
            simpleElem.addSource(fromNode.getId());
            elem.addChild(simpleElem);
        }
    }

    private class NodeVisitor extends AbstractGraphToTextVisitor {

        @Override
        public Element cyclicVisit(InputNode node, List<InputEdge> path) {
            SimpleElement elem = new SimpleElement(node.getProperties().get("name"), calcStyle(node));
            elem.addSource(node.getId());
            return elem;
        }
    }
    
    private Color calcColor(InputNode node) {
        Set<Figure> figureSet = this.map.get(node);
        if(figureSet != null && figureSet.size() == 1) {
            return figureSet.iterator().next().getColor();
        } else {
            return Color.WHITE;
        }
    }
    
    private Style calcStyle(InputNode node) {
        Color c = calcColor(node);
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style newStyle = StyleContext.getDefaultStyleContext().addStyle(null, defaultStyle);
        
        StyleConstants.setBackground(newStyle, c);
        return newStyle;
    }

    private class ClosureVisitor extends AbstractGraphToTextVisitor {

        private String label;

        protected String getLabel(InputNode node) {
                return label;
        }

        public ClosureVisitor(String label) {
            this.label = label;
        }

        @Override
        public Element cyclicVisit(InputNode node, List<InputEdge> path) {
            return SimpleElement.EMPTY;
        }

        @Override
        public Element visit(InputNode node, List<InputEdge> path, List<Pair<InputEdge, Element>> children) {
            MultiElement e = new MultiElement(calcStyle(node));
            e.print("{", node.getId());
            e.print(getLabel(node), node.getId());

            e.print("[", node.getId());
            for (int i = 0; i < children.size() - 1; i++) {
                Pair<InputEdge, Element> p = children.get(i);
                e.addChild(p.getRight());
                if (i != children.size() - 2) {
                    e.print("|", node.getId());
                }
            }
            e.print("]", node.getId());
            e.print(CALL_OPERATOR, node.getId());
            e.println();
            List<InputEdge> newPath = new ArrayList<InputEdge>(path);
            newPath.add(children.get(children.size() - 1).getLeft());
            printOffset(newPath, e);
            
            MultiElement childElement = new MultiElement("...");
            childElement.addChild(children.get(children.size() - 1).getRight());
            e.addChild(childElement);
            
            e.println();
            printOffset(path, e);
            e.print("}", node.getId());
            MultiElement resElem = new MultiElement();
            resElem.addChild(e);
            return resElem;
        }
    }

    private class CallVisitor extends AbstractGraphToTextVisitor {

        @Override
        public Element cyclicVisit(InputNode node, List<InputEdge> path) {
            return SimpleElement.EMPTY;
        }

        @Override
        public Element visit(InputNode node, List<InputEdge> path, List<Pair<InputEdge, Element>> children) {
            MultiElement e = new MultiElement(calcStyle(node));
            e.print("(", node.getId());
            for (int i = 0; i < children.size(); i++) {
                Pair<InputEdge, Element> p = children.get(i);
                e.addChild(p.getRight());
                if (i != children.size() - 1) {
                    e.print("|", node.getId());
                }
            }
            e.print(")", node.getId());
            MultiElement resElem = new MultiElement();
            resElem.addChild(e);
            return resElem;
        }
    }

    private class BlockVisitor extends ClosureVisitor {

        public BlockVisitor() {
            super("block");
        }
        
        @Override
        protected String getLabel(InputNode node) {
            return node.getProperties().get("name");
        }

        @Override
        public Element cyclicVisit(InputNode node, List<InputEdge> path) {
            MultiElement e = new MultiElement(calcStyle(node));
            e.print(getLabel(node), node);
            return e;
        }

        @Override
        public Element visit(InputNode node, List<InputEdge> path, List<Pair<InputEdge, Element>> children) {
            return super.visit(node, path, children);
        }
    }

    private static final PropertyMatcher MATCHER = new Properties.RegexpPropertyMatcher("type", ".*CIR.*");
    public boolean canConvert(InputGraph graph) {
        return graph.getGroup().getProperties().selectSingle(MATCHER) != null;
    }
}
