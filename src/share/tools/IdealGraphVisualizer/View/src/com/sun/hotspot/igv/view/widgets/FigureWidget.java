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
package com.sun.hotspot.igv.view.widgets;

import com.sun.hotspot.igv.data.InputGraph;
import com.sun.hotspot.igv.data.Properties;
import com.sun.hotspot.igv.data.services.GraphViewer;
import com.sun.hotspot.igv.graph.Figure;
import com.sun.hotspot.igv.util.DoubleClickAction;
import com.sun.hotspot.igv.util.DoubleClickHandler;
import com.sun.hotspot.igv.util.PropertiesSheet;
import com.sun.hotspot.igv.view.DiagramScene;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author Thomas Wuerthinger
 */
public class FigureWidget extends Widget implements Properties.Provider, PopupMenuProvider, DoubleClickHandler {

    public static final boolean VERTICAL_LAYOUT = true;
    //public static final int MAX_STRING_LENGTH = 20;
    private static final double LABEL_ZOOM_FACTOR = 0.3;
    private static final double ZOOM_FACTOR = 0.1;
    private Font font;
    private Font boldFont;
    private Figure figure;
    private Widget leftWidget;
    private Widget rightWidget;
    private Widget middleWidget;
    private ArrayList<LabelWidget> labelWidgets;
    private DiagramScene diagramScene;
    private boolean boundary;
    private Node node;
    private Widget dummyTop;

    public void setBoundary(boolean b) {
        boundary = b;
    }

    public boolean isBoundary() {
        return boundary;
    }

    public Node getNode() {
        return node;
    }

	@Override
	public boolean isHitAt(Point localLocation) {
		return middleWidget.isHitAt(localLocation);
	}
    

    public FigureWidget(final Figure f, WidgetAction hoverAction, WidgetAction selectAction, DiagramScene scene, Widget parent) {

        super(scene);

        assert this.getScene() != null;
        assert this.getScene().getView() != null;

        this.figure = f;
        font = f.getDiagram().getFont();
        boldFont = f.getDiagram().getFont().deriveFont(Font.BOLD);
        this.setCheckClipping(true);
        this.diagramScene = scene;
        parent.addChild(this);

	Widget outer = new Widget(scene);
	outer.setBackground(f.getColor());
	outer.setLayout(LayoutFactory.createOverlayLayout());
	
        middleWidget = new Widget(scene);
        middleWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        middleWidget.setBackground(f.getColor());
        middleWidget.setOpaque(true);
        //middleWidget.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        middleWidget.getActions().addAction(new DoubleClickAction(this));
	middleWidget.setCheckClipping(true);

        labelWidgets = new ArrayList<>();

        String[] strings = figure.getLines();

        dummyTop = new Widget(scene);
        dummyTop.setMinimumSize(new Dimension(Figure.INSET / 2, 1));
        middleWidget.addChild(dummyTop);


        for (String cur : strings) {

            String displayString = cur;

            LabelWidget lw = new LabelWidget(scene);
            labelWidgets.add(lw);
            middleWidget.addChild(lw);
            lw.setLabel(displayString);
            lw.setFont(font);
            lw.setForeground(Color.BLACK);
            lw.setAlignment(LabelWidget.Alignment.CENTER);
            lw.setVerticalAlignment(LabelWidget.VerticalAlignment.CENTER);
	    lw.setBorder(BorderFactory.createEmptyBorder());
        }

        Widget dummyBottom = new Widget(scene);
        dummyBottom.setMinimumSize(new Dimension(Figure.INSET / 2, 1));
        middleWidget.addChild(dummyBottom);

        middleWidget.setPreferredBounds(new Rectangle(0, Figure.SLOT_WIDTH - Figure.OVERLAPPING, f.getWidth(), f.getHeight()));
	//outer.addChild(middleWidget);
        this.addChild(middleWidget);

        // Initialize node for property sheet
        node = new AbstractNode(Children.LEAF) {

            @Override
            protected Sheet createSheet() {
                Sheet s = super.createSheet();
                PropertiesSheet.initializeSheet(f.getProperties(), s);
                return s;
            }
        };
        node.setDisplayName(getName());
    }

    public Widget getLeftWidget() {
        return leftWidget;
    }

    public Widget getRightWidget() {
        return rightWidget;
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);

        Color borderColor = Color.BLACK;
	Color innerBorderColor = getFigure().getColor();
        int thickness = 1;
        boolean repaint = false;
        Font f = font;
        if (state.isSelected() || state.isHighlighted()) {
            thickness = 2;
	}
	if(state.isSelected()) {
            f = boldFont;
		innerBorderColor = borderColor;
        } else {
	}

        if (state.isHighlighted()) {
		innerBorderColor = borderColor = Color.BLUE;
		repaint = true;
        } else {
		repaint = true;
	}

        if (state.isHovered() != previousState.isHovered()) {

		/*
            if (state.isHovered()) {
                diagramScene.addAllHighlighted(this.getFigure().getSource().getSourceNodesAsSet());
            } else {
                diagramScene.removeAllHighlighted(this.getFigure().getSource().getSourceNodesAsSet());
            }*/
            repaint = true;
        }

        if (state.isSelected() != previousState.isSelected()) {
            repaint = true;
        }

        if (repaint) {
            middleWidget.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, 1), BorderFactory.createLineBorder(innerBorderColor, 1)));
            for (LabelWidget labelWidget : labelWidgets) {
                labelWidget.setFont(f);
            }
            repaint();
        }
    }

    public String getName() {
        return getProperties().get("name");
    }

    @Override
    public Properties getProperties() {
        return figure.getProperties();
    }

    public Figure getFigure() {
        return figure;
    }

    @Override
    protected void paintChildren() {
        Composite oldComposite = null;
        if (boundary) {
            oldComposite = getScene().getGraphics().getComposite();
            float alpha = DiagramScene.ALPHA;
            this.getScene().getGraphics().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        if (diagramScene.getZoomFactor() < LABEL_ZOOM_FACTOR) {

            for (LabelWidget labelWidget : labelWidgets) {
                labelWidget.setVisible(false);
            }
            super.paintChildren();
            for (LabelWidget labelWidget : labelWidgets) {
                labelWidget.setVisible(true);
            }

        } else {
            super.paintChildren();
        }

        if (boundary) {
            getScene().getGraphics().setComposite(oldComposite);
        }
    }
 
    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        JPopupMenu menu = diagramScene.createPopupMenu();
        menu.addSeparator();

        JMenu predecessors = new JMenu("Nodes Above");
        predecessors.addMenuListener(new NeighborMenuListener(predecessors, getFigure(), false));
        menu.add(predecessors);

        JMenu successors = new JMenu("Nodes Below");
        successors.addMenuListener(new NeighborMenuListener(successors, getFigure(), true));
        menu.add(successors);

        if (getFigure().getSubgraphs() != null) {
            menu.addSeparator();
            JMenu subgraphs = new JMenu("Subgraphs");
            menu.add(subgraphs);

            final GraphViewer viewer = Lookup.getDefault().lookup(GraphViewer.class);
            for(final InputGraph subgraph : getFigure().getSubgraphs()) {
                Action a = new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        viewer.view(subgraph, true);
                    }
                };

                a.setEnabled(true);
                a.putValue(Action.NAME, subgraph.getName());
                subgraphs.add(a);
            }
        }

        return menu;
    }

    /**
     * Builds the submenu for a figure's neighbors on demand.
     */
    private class NeighborMenuListener implements MenuListener {

        private final JMenu menu;
        private final Figure figure;
        private final boolean successors;

        public NeighborMenuListener(JMenu menu, Figure figure, boolean successors) {
            this.menu = menu;
            this.figure = figure;
            this.successors = successors;
        }

        @Override
        public void menuSelected(MenuEvent e) {
            if (menu.getItemCount() > 0) {
                // already built before
                return;
            }

            Set<Figure> set = figure.getPredecessorSet();
            if (successors) {
                set = figure.getSuccessorSet();
            }

            boolean first = true;
            for (Figure f : set) {
                if (f == figure) {
                    continue;
                }

                if (first) {
                    first = false;
                } else {
                    menu.addSeparator();
                }

                Action go = diagramScene.createGotoAction(f);
                menu.add(go);

                JMenu preds = new JMenu("Nodes Above");
                preds.addMenuListener(new NeighborMenuListener(preds, f, false));
                menu.add(preds);

                JMenu succs = new JMenu("Nodes Below");
                succs.addMenuListener(new NeighborMenuListener(succs, f, true));
                menu.add(succs);
            }

            if (menu.getItemCount() == 0) {
                menu.add("(none)");
            }
        }

        @Override
        public void menuDeselected(MenuEvent e) {
            // ignore
        }

        @Override
        public void menuCanceled(MenuEvent e) {
            // ignore
        }
    }

    @Override
    public void handleDoubleClick(Widget w, WidgetAction.WidgetMouseEvent e) {

        if (diagramScene.isAllVisible()) {
            final Set<Integer> hiddenNodes = new HashSet<>(diagramScene.getModel().getGraphToView().getGroup().getAllNodes());
            hiddenNodes.removeAll(this.getFigure().getSource().getSourceNodesAsSet());
            this.diagramScene.getModel().showNot(hiddenNodes);
        } else if (isBoundary()) {

            final Set<Integer> hiddenNodes = new HashSet<>(diagramScene.getModel().getHiddenNodes());
            hiddenNodes.removeAll(this.getFigure().getSource().getSourceNodesAsSet());
            this.diagramScene.getModel().showNot(hiddenNodes);
        } else {
            final Set<Integer> hiddenNodes = new HashSet<>(diagramScene.getModel().getHiddenNodes());
            hiddenNodes.addAll(this.getFigure().getSource().getSourceNodesAsSet());
            this.diagramScene.getModel().showNot(hiddenNodes);
        }
    }
}
