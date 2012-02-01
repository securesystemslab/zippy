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

import com.sun.hotspot.igv.coordinator.actions.RemoveAction;
import com.sun.hotspot.igv.coordinator.actions.SaveAsAction;
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

/**
 *
 * @author Thomas Wuerthinger
 */
public class FolderNode extends AbstractNode implements ChangedListener {

    @Override
    public void changed(Object folder) {
        if (this.getChildren() == Children.LEAF) {
            setChildren(createFolderChildren((Folder)folder));
        }
    }

    private static class FolderChildren extends Children.Keys<Folder> implements ChangedListener {

        private final Folder folder;

        public FolderChildren(Folder folder) {
            this.folder = folder;
            folder.getChangedEvent().addListener(this);
        }

        @Override
        protected Node[] createNodes(Folder e) {
            return new Node[]{new FolderNode(e)};
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

        @Override
        public void changed(Object source) {
            addNotify();
        }
    }

    @Override
    public Image getIcon(int i) {
        return ImageUtilities.loadImage("com/sun/hotspot/igv/coordinator/images/folder.png");
    }

    protected FolderNode(Folder folder) {
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

    private FolderNode(final Folder folder, Children children, InstanceContent content) {
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
    public Action[] getActions(boolean b) {
        return new Action[]{(Action) OpenAction.findObject(OpenAction.class, true), RemoveAction.findObject(RemoveAction.class), SaveAsAction.findObject(SaveAsAction.class)};
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
