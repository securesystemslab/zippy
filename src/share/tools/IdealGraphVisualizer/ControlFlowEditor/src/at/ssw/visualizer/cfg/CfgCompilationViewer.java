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

package at.ssw.visualizer.cfg;

import at.ssw.visualizer.cfg.action.*;
import at.ssw.visualizer.cfg.graph.CfgScene;
import at.ssw.visualizer.cfg.graph.EdgeWidget;
import at.ssw.visualizer.cfg.graph.NodeWidget;
import at.ssw.visualizer.cfg.model.CfgEdge;
import at.ssw.visualizer.cfg.model.CfgNode;
import at.ssw.visualizer.cfg.preferences.CfgPreferences;
import at.ssw.visualizer.cfg.preferences.FlagsSetting;
import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import com.oracle.graal.visualizer.editor.CompilationViewer;
import com.sun.hotspot.igv.data.InputGraph;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.border.Border;
import org.netbeans.api.visual.widget.Widget;
import org.openide.awt.Toolbar;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

class CfgCompilationViewer implements CompilationViewer {

    private CfgScene scene;
    private JScrollPane jScrollPane;
    private ControlFlowGraph cfg;
    private JComponent myView;

    public CfgCompilationViewer(InputGraph cfg) {
        this.cfg = cfg;

       // setIcon(ImageUtilities.loadImage("at/ssw/visualizer/cfg/icons/cfg.gif"));
//        setName(cfg.getParent().getShortName());
//        setToolTipText(cfg.getCompilation().getMethod() + " - " + cfg.getName());
        // TODO(tw): Add title.

        //panel setup
        this.jScrollPane = new JScrollPane();
        this.jScrollPane.setOpaque(true);
        this.jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.jScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        this.scene = new CfgScene(jScrollPane, cfg);
        this.myView = scene.createView();
        this.jScrollPane.setViewportView(myView);
        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.getVerticalScrollBar().setEnabled(true);
        jScrollPane.getHorizontalScrollBar().setEnabled(true);

        //setup enviroment,register listeners
        // TODO(tw): Add to lookup.
//        selection = new Selection();
//        selection.put(cfg);
//        selection.put(scene);
//        selection.addChangeListener(scene);

        scene.validate();
        scene.applyLayout();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (this.scene != null) {

            String propName = evt.getPropertyName();
            CfgPreferences prefs = CfgPreferences.getInstance();
            switch (propName) {
                case CfgPreferences.PROP_BACKGROUND_COLOR:
                    scene.setBackground(prefs.getBackgroundColor());
                    scene.revalidate();
                    break;
                case CfgPreferences.PROP_NODE_COLOR:
                    for (NodeWidget nw : scene.getNodeWidgets()) {
                        //only change the node color if its not a custom color
                        if (!nw.isNodeColorCustomized()) {
                            nw.setNodeColor(prefs.getNodeColor(), false);
                        }
                    }
                    break;
                case CfgPreferences.PROP_EDGE_COLOR:
                    for (CfgEdge e : scene.getEdges()) {
                        if (!e.isBackEdge() && !e.isXhandler()) {
                            EdgeWidget w = (EdgeWidget) scene.findWidget(e);
                            w.setLineColor(prefs.getEdgeColor());
                        }
                    }
                    break;
                case CfgPreferences.PROP_BACK_EDGE_COLOR:
                    for (CfgEdge e : scene.getEdges()) {
                        if (e.isBackEdge()) {
                            EdgeWidget w = (EdgeWidget) scene.findWidget(e);
                            w.setLineColor(prefs.getBackedgeColor());
                        }
                    }
                    break;
                case CfgPreferences.PROP_EXCEPTION_EDGE_COLOR:
                    for (CfgEdge e : scene.getEdges()) {
                        if (e.isXhandler()) {
                            EdgeWidget w = (EdgeWidget) scene.findWidget(e);
                            w.setLineColor(prefs.getExceptionEdgeColor());
                        }
                    }
                    break;
                case CfgPreferences.PROP_BORDER_COLOR:
                    for (CfgNode n : scene.getNodes()) {
                        NodeWidget nw = (NodeWidget) scene.findWidget(n);
                        nw.setBorderColor(prefs.getBorderColor());
                    }
                    break;
                case CfgPreferences.PROP_TEXT_FONT:
                    for (CfgNode n : scene.getNodes()) {
                        NodeWidget nw = (NodeWidget) scene.findWidget(n);
                        nw.adjustFont(prefs.getTextFont());
                    }
                    break;
                case CfgPreferences.PROP_TEXT_COLOR:
                    for (CfgNode n : scene.getNodes()) {
                        NodeWidget nw = (NodeWidget) scene.findWidget(n);
                        nw.setForeground(prefs.getTextColor());
                    }
                    break;
                case CfgPreferences.PROP_FLAGS:
                    FlagsSetting fs = CfgPreferences.getInstance().getFlagsSetting();
                    for (CfgNode n : scene.getNodes()) {
                        NodeWidget nw = (NodeWidget) scene.findWidget(n);
                        Color nodeColor = fs.getColor(n.getBasicBlock().getFlags());
                        if (nodeColor != null) {
                            nw.setNodeColor(nodeColor, true);
                        } else {
                            nw.setNodeColor(CfgPreferences.getInstance().getNodeColor(), false);
                        }
                    }
                    break;
                case CfgPreferences.PROP_SELECTION_COLOR_BG:
                case CfgPreferences.PROP_SELECTION_COLOR_FG:
                    for (CfgNode n : scene.getNodes()) {
                        Widget w = scene.findWidget(n);
                        w.revalidate();
                    }
                    break;
            }
            scene.validate();
        }

    }

    /*@Override
    public Component getToolBarComponent() {
        Toolbar tb = new Toolbar("CfgToolbar");

        tb.setBorder((Border) UIManager.get("Nb.Editor.Toolbar.border"));

        //zoomin/zoomout buttons
        tb.add(SystemAction.get(ZoominAction.class).getToolbarPresenter());
        tb.add(SystemAction.get(ZoomoutAction.class).getToolbarPresenter());
        tb.addSeparator();

        //router buttons
        ButtonGroup routerButtons = new ButtonGroup();
        UseDirectLineRouterAction direct = SystemAction.get(UseDirectLineRouterAction.class);
        UseBezierRouterAction bezier = SystemAction.get(UseBezierRouterAction.class);
        JToggleButton button = (JToggleButton) direct.getToolbarPresenter();
        button.getModel().setGroup(routerButtons);
        button.setSelected(true);
        tb.add(button);
        button = (JToggleButton) bezier.getToolbarPresenter();
        button.getModel().setGroup(routerButtons);
        tb.add(button);
        tb.addSeparator();

        //layout buttons
        tb.add(SystemAction.get(HierarchicalNodeLayoutAction.class).getToolbarPresenter());
        tb.add(SystemAction.get(HierarchicalCompoundLayoutAction.class).getToolbarPresenter());

        tb.addSeparator();
        tb.add(SystemAction.get(ShowAllAction.class).getToolbarPresenter());
        tb.addSeparator();

        //cluster button
        tb.add(SystemAction.get(SwitchLoopClustersAction.class).getToolbarPresenter());
        tb.addSeparator();

        //show/hide edge button
        tb.add(SystemAction.get(ShowEdgesAction.class).getToolbarPresenter());
        tb.add(SystemAction.get(HideEdgesAction.class).getToolbarPresenter());
        tb.addSeparator();

        //color button       
        JComponent colorButton = SystemAction.get(ColorAction.class).getToolbarPresenter();
        scene.addCfgEventListener((CfgEventListener) colorButton);
        tb.add(colorButton);

        //export button           
        tb.add(SystemAction.get(ExportAction.class).getToolbarPresenter());
        tb.doLayout();

        return tb;
    }*/

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(scene);
    }

    @Override
    public Component getComponent() {
        return jScrollPane;
    }
}
