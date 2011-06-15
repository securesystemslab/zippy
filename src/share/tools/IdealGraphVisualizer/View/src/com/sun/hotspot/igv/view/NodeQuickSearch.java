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
package com.sun.hotspot.igv.view;

import com.sun.hotspot.igv.data.InputGraph;
import com.sun.hotspot.igv.data.InputNode;
import com.sun.hotspot.igv.data.Properties;
import com.sun.hotspot.igv.data.Properties.RegexpPropertyMatcher;
import com.sun.hotspot.igv.data.Property;
import com.sun.hotspot.igv.data.services.InputGraphProvider;
import com.sun.hotspot.igv.util.LookupHistory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Thomas Wuerthinger
 */
public class NodeQuickSearch implements SearchProvider {

    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with
     * apropriate results
     *
     * @param request Search request object that contains information what to search for
     * @param response Search response object that stores search results. Note that it's important to react to return value of SearchResponse.addResult(...) method and stop computation if false value is returned.
     */
    public void evaluate(SearchRequest request, SearchResponse response) {

        final String[] parts = request.getText().split("=");
        if (parts.length == 0) {
            return;
        }

        String tmpName = parts[0];

        String value = null;
        if (parts.length == 2) {
            value = parts[1];
        }

        if (parts.length == 1 && request.getText().endsWith("=")) {
            value = ".*";
        }

        final String name = tmpName;

        if (value != null) {
            final InputGraphProvider p = LookupHistory.getLast(InputGraphProvider.class);//)Utilities.actionsGlobalContext().lookup(InputGraphProvider.class);
            if (p != null) {
                final InputGraph graph = p.getGraph();
                if (graph != null) {
                    final RegexpPropertyMatcher matcher = new RegexpPropertyMatcher(name, value);
                    final Properties.PropertySelector<InputNode> selector = new Properties.PropertySelector<InputNode>(graph.getNodes());
                    final List<InputNode> list = selector.selectMultiple(matcher);
                    final Set<InputNode> set = new HashSet<InputNode>(list);

                    response.addResult(new Runnable() {

                        public void run() {

                            final EditorTopComponent comp = EditorTopComponent.getActive();
                            if (comp != null) {
                                comp.setSelectedNodes(set);
                                comp.requestActive();
                            }
                        }
                    }, "All " + list.size() + " matching nodes (" + name + "=" + value + ")");
                    for (final InputNode n : list) {


                        response.addResult(new Runnable() {

                            public void run() {
                                final EditorTopComponent comp = EditorTopComponent.getActive();
                                if (comp != null) {
                                    final Set<InputNode> tmpSet = new HashSet<InputNode>();
                                    tmpSet.add(n);
                                    comp.setSelectedNodes(tmpSet);
                                    comp.requestActive();
                                }
                            }
                        }, n.getProperties().get(name) + " (" + n.getId() + " " + n.getProperties().get("name") + ")");
                    }
                }

            } else {
                System.out.println("no input graph provider!");
            }

        } else if (parts.length == 1) {

            final InputGraphProvider p = LookupHistory.getLast(InputGraphProvider.class);//Utilities.actionsGlobalContext().lookup(InputGraphProvider.class);

            if (p != null) {

                final InputGraph graph = p.getGraph();
                if (p != null && graph != null) {

                    Set<String> properties = new HashSet<String>();
                    for (InputNode n : p.getGraph().getNodes()) {
                        for (Property property : n.getProperties()) {
                            properties.add(property.getName());
                        }
                    }

                    for (final String propertyName : properties) {

                        if (propertyName.startsWith(name)) {

                            response.addResult(new Runnable() {

                                public void run() {

                                    NotifyDescriptor.InputLine d =
                                            new NotifyDescriptor.InputLine("Value of the property?", "Property Value Input");
                                    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                                        String value = d.getInputText();
                                        final RegexpPropertyMatcher matcher = new RegexpPropertyMatcher(propertyName, value);
                                        final Properties.PropertySelector<InputNode> selector = new Properties.PropertySelector<InputNode>(graph.getNodes());
                                        final List<InputNode> list = selector.selectMultiple(matcher);
                                        final Set<InputNode> set = new HashSet<InputNode>(list);

                                        final EditorTopComponent comp = EditorTopComponent.getActive();
                                        if (comp != null) {
                                            comp.setSelectedNodes(set);
                                            comp.requestActive();
                                        }

                                    }


                                }
                            }, propertyName + "=");
                        }
                    }

                } else {
                    System.out.println("no input graph provider!");
                }
            }
        }
    }
}
