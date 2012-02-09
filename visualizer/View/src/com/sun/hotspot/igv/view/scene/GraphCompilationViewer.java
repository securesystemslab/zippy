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

package com.sun.hotspot.igv.view.scene;

import com.sun.hotspot.igv.svg.BatikSVG;
import com.oracle.graal.visualizer.editor.CompilationViewer;
import com.oracle.graal.visualizer.editor.DiagramViewModel;
import com.sun.hotspot.igv.graph.Figure;
import com.sun.hotspot.igv.view.actions.*;
import java.awt.Component;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class GraphCompilationViewer implements CompilationViewer, PropertyChangeListener {
    
    private DiagramScene scene;
    private PredSuccAction predSuccAction;

    GraphCompilationViewer(DiagramViewModel model) {
        
        scene = new DiagramScene(model);
        
        Action[] actions = new Action[]{
        };
        
        scene.setActions(actions);
    }

    @Override
    public Lookup getLookup() {
        return scene.getLookup();
    }

    @Override
    public Component getComponent() {
        return scene.getComponent();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.predSuccAction) {
            boolean b = (Boolean) predSuccAction.getValue(PredSuccAction.STATE);
            scene.getModel().setShowNodeHull(b);
        } else {
            assert false : "Unknown event source";
        }
    }

}
