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
package com.sun.hotspot.igv.coordinator;

import com.sun.hotspot.igv.coordinator.actions.RemoveCookie;
import com.sun.hotspot.igv.data.ChangedListener;
import com.sun.hotspot.igv.data.GraphDocument;
import com.sun.hotspot.igv.data.Group;
import com.sun.hotspot.igv.data.services.GroupOrganizer;
import com.sun.hotspot.igv.data.InputGraph;
import com.sun.hotspot.igv.data.Pair;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Thomas Wuerthinger
 */
public class FolderNode extends AbstractNode {

    private GroupOrganizer organizer;
    private InstanceContent content;
    private List<Pair<String, List<Group>>> structure;
    private List<String> subFolders;
    private FolderChildren children;

    private static class FolderChildren extends Children.Keys<Pair<String, List<Group>>> implements ChangedListener<Group> {

        private FolderNode parent;
        private List<Group> registeredGroups;
        private GraphDocument document;

        public FolderChildren(GraphDocument document) {
            this.document = document;
        }

        public void setParent(FolderNode parent) {
            this.parent = parent;
            this.registeredGroups = new ArrayList<Group>();
        }

        @Override
        protected Node[] createNodes(Pair<String, List<Group>> p) {

            for(Group g : registeredGroups) {
                g.getChangedEvent().removeListener(this);
            }
            registeredGroups.clear();
            
            if (p.getLeft().length() == 0) {

                List<Node> curNodes = new ArrayList<Node>();
                for (Group g : p.getRight()) {
                    for (InputGraph graph : g.getGraphs()) {
                        curNodes.add(new GraphNode(graph));
                    }
                    g.getChangedEvent().addListener(this);
                    registeredGroups.add(g);
                }

                Node[] result = new Node[curNodes.size()];
                for (int i = 0; i < curNodes.size(); i++) {
                    result[i] = curNodes.get(i);
                }
                return result;

            } else {
                return new Node[]{new FolderNode(document, p.getLeft(), parent.organizer, parent.subFolders, p.getRight())};
            }
        }

        @Override
        public void addNotify() {
            this.setKeys(parent.structure);
        }
        
        public void changed(Group source) {
            for(Pair<String, List<Group>> p : parent.structure) {
                refreshKey(p);
            }
         }
    }

    protected InstanceContent getContent() {
        return content;
    }

    @Override
    public Image getIcon(int i) {
        return ImageUtilities.loadImage("com/sun/hotspot/igv/coordinator/images/folder.png");
    }

    protected FolderNode(GraphDocument document, String name, GroupOrganizer organizer, List<String> subFolders, List<Group> groups) {
        this(document, name, organizer, subFolders, groups, new FolderChildren(document), new InstanceContent());
    }

    private FolderNode(final GraphDocument document, String name, GroupOrganizer organizer, List<String> oldSubFolders, final List<Group> groups, FolderChildren children, InstanceContent content) {
        super(children, new AbstractLookup(content));
        children.setParent(this);
        this.content = content;
        this.children = children;
        content.add(new RemoveCookie() {

            public void remove() {
                for (Group g : groups) {
                    document.removeGroup(g);
                    
                }
            }
        });
        init(name, organizer, oldSubFolders, groups);
    }

    public void init(String name, GroupOrganizer organizer, List<String> oldSubFolders, List<Group> groups) {
        this.setDisplayName(name);
        this.organizer = organizer;
        this.subFolders = new ArrayList<String>(oldSubFolders);
        if (name.length() > 0) {
            this.subFolders.add(name);
        }
        structure = organizer.organize(subFolders, groups);
        assert structure != null;
        children.addNotify();
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
}
