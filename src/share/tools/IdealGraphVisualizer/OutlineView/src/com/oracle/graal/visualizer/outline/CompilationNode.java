/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.visualizer.outline;

import com.sun.hotspot.igv.data.*;
import com.sun.hotspot.igv.data.services.GraphViewer;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class CompilationNode extends AbstractNode implements ChangedListener {

    private CompilationNode(final Folder folder, Children children, InstanceContent content) {
        super(children, new AbstractLookup(content));
        this.setDisplayName(folder.getName());

        if (folder instanceof Group) {
            content.add(new OpenCookie() {

                @Override
                public void open() {
                    final List<InputGraph> graphs = ((Group) folder).getGraphs();
                    if (graphs.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Cannot open compilation, because there was no snapshots recorded!");
                    } else {
                        Lookup.getDefault().lookup(GraphViewer.class).view(graphs.get(0));
                    }
                }
            });
        }
        content.add(folder);
        folder.getChangedEvent().addListener(this);
    }

    @Override
    public void changed(Object folder) {
        if (this.getChildren() == Children.LEAF) {
            setChildren(createFolderChildren((Folder) folder));
            this.fireIconChange();
        }
    }

    private static class FolderChildren extends Children.Keys<Folder> {

        private final Folder folder;

        public FolderChildren(Folder folder) {
            this.folder = folder;
            folder.getChangedEvent().addListener(changedListener);
        }

        @Override
        protected Node[] createNodes(Folder e) {
            return new Node[]{new CompilationNode(e)};
        }

        @Override
        public void addNotify() {
            List<Folder> result = new ArrayList<>();
            for (FolderElement o : folder.getElements()) {
                if (o instanceof Folder) {
                    result.add((Folder) o);
                }
            }
            this.setKeys(result);
        }
        private final ChangedListener changedListener = new ChangedListener() {

            @Override
            public void changed(Object source) {
                addNotify();
            }
        };
    }

    @Override
    public Image getIcon(int i) {
        if (this.getChildren() == Children.LEAF) {
            return ImageUtilities.loadImage("com/oracle/graal/visualizer/outline/images/leaf_node.gif");
        } else {
            return ImageUtilities.loadImage("com/oracle/graal/visualizer/outline/images/node.gif");
        }
    }

    protected CompilationNode(Folder folder) {
        this(folder, createFolderChildren(folder), new InstanceContent());
    }

    private static Children createFolderChildren(Folder folder) {
        for (FolderElement elem : folder.getElements()) {
            if (elem instanceof Folder) {
                return new FolderChildren(folder);
            }
        }
        return Children.LEAF;
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>();
        actions.add((Action) OpenAction.findObject(OpenAction.class, true));
        actions.addAll(Lookups.forPath(OutlineTopComponent.NODE_ACTIONS_FOLDER).lookupAll(Action.class));
        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Action getPreferredAction() {
        return (Action) OpenAction.findObject(OpenAction.class, true);
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
}
