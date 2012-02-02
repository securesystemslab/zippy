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
import com.oracle.graal.visualizer.editor.ExportCookie;
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
    private JToolBar toolBar;
    private PredSuccAction predSuccAction;
    
    private ExportCookie exportCookie = new ExportCookie() {

        @Override
        public void export(File f) {

            Graphics2D svgGenerator = BatikSVG.createGraphicsObject();

            if (svgGenerator == null) {
                NotifyDescriptor message = new NotifyDescriptor.Message("For export to SVG files the Batik SVG Toolkit must be intalled.", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(message);
            } else {
                scene.paint(svgGenerator);
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(f);
                    Writer out = new OutputStreamWriter(os, "UTF-8");
                    BatikSVG.printToStream(svgGenerator, out, true);
                } catch (FileNotFoundException e) {
                    NotifyDescriptor message = new NotifyDescriptor.Message("For export to SVG files the Batik SVG Toolkit must be intalled.", NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(message);

                } catch (UnsupportedEncodingException e) {
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                        }
                    }
                }

            }
        }
    };
    private Lookup lookup;

    GraphCompilationViewer(DiagramViewModel model) {
        
        scene = new DiagramScene(model);
        
        Action[] actions = new Action[]{
            ExtractAction.get(ExtractAction.class),
            ShowAllAction.get(HideAction.class),
            ShowAllAction.get(ShowAllAction.class),
            null,
          //  new ZoomInAction(scene),
            //new ZoomOutAction(scene),
            null,
            ExpandPredecessorsAction.get(ExpandPredecessorsAction.class),
            ExpandSuccessorsAction.get(ExpandSuccessorsAction.class)
        };
        
        scene.setActions(actions);
        
        toolBar = new JToolBar();
        toolBar.add(ExtractAction.get(ExtractAction.class));
        toolBar.add(ShowAllAction.get(HideAction.class));
        toolBar.add(ShowAllAction.get(ShowAllAction.class));
        toolBar.addSeparator();
        //toolBar.add(ShowAllAction.get(ZoomInAction.class));
        //toolBar.add(ShowAllAction.get(ZoomOutAction.class));

        predSuccAction = new PredSuccAction();
        JToggleButton button = new JToggleButton(predSuccAction);
        button.setSelected(true);
        toolBar.add(button);
        predSuccAction.addPropertyChangeListener(this);
        
        lookup = new ProxyLookup(scene.getLookup(), Lookups.singleton(exportCookie));
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public Component getComponent() {
        return scene.getComponent();
    }

    @Override
    public Component getToolBarComponent() {
        return toolBar;
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
