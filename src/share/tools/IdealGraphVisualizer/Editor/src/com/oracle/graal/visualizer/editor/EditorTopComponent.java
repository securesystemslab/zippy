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
package com.oracle.graal.visualizer.editor;

import com.oracle.graal.visualizer.editor.actions.NextDiagramAction;
import com.oracle.graal.visualizer.editor.actions.PrevDiagramAction;
import com.sun.hotspot.igv.data.ChangedEvent;
import com.sun.hotspot.igv.data.ChangedListener;
import com.sun.hotspot.igv.data.InputGraph;
import com.sun.hotspot.igv.data.InputNode;
import com.sun.hotspot.igv.data.services.InputGraphProvider;
import com.sun.hotspot.igv.filter.FilterChain;
import com.sun.hotspot.igv.filter.FilterChainProvider;
import com.sun.hotspot.igv.graph.Diagram;
import com.sun.hotspot.igv.graph.Figure;
import com.sun.hotspot.igv.graph.services.DiagramProvider;
import com.sun.hotspot.igv.util.LookupHistory;
import com.sun.hotspot.igv.util.RangeSlider;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import org.openide.actions.RedoAction;
import org.openide.actions.UndoAction;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * 
 * @author Thomas Wuerthinger
 */
public final class EditorTopComponent extends TopComponent {

    private InstanceContent content;
    private InstanceContent graphContent;
    private RangeSlider rangeSlider;
    private static final String PREFERRED_ID = "EditorTopComponent";
    private DiagramViewModel rangeSliderModel;
    private CompilationViewer viewer;
    

    private DiagramProvider diagramProvider = new DiagramProvider() {

        @Override
        public Diagram getDiagram() {
            return getModel().getDiagramToView();
        }

        @Override
        public ChangedEvent<DiagramProvider> getChangedEvent() {
            return diagramChangedEvent;
        }
    };

    private ChangedEvent<DiagramProvider> diagramChangedEvent = new ChangedEvent<>(diagramProvider);
    

    private void updateDisplayName() {
        setDisplayName(getDiagram().getName());
    }

    public EditorTopComponent(InputGraph graph) {

        LookupHistory.init(InputGraphProvider.class);
        LookupHistory.init(DiagramProvider.class);
        this.setFocusable(true);
        FilterChain filterChain;
        FilterChain sequence;
        FilterChainProvider provider = Lookup.getDefault().lookup(FilterChainProvider.class);
        if (provider == null) {
            filterChain = new FilterChain();
            sequence = new FilterChain();
        } else {
            filterChain = provider.getFilterChain();
            sequence = provider.getSequence();
        }

        setName(NbBundle.getMessage(EditorTopComponent.class, "CTL_EditorTopComponent"));
        setToolTipText(NbBundle.getMessage(EditorTopComponent.class, "HINT_EditorTopComponent"));

        initComponents();

        ToolbarPool.getDefault().setPreferredIconSize(16);
        Toolbar toolBar = new Toolbar();
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolBar.setBorder(b);
        this.add(BorderLayout.NORTH, toolBar);

        rangeSliderModel = new DiagramViewModel(graph.getGroup(), filterChain, sequence);
        rangeSlider = new RangeSlider();
        rangeSlider.setModel(rangeSliderModel);

        CompilationViewerFactory factory = Lookup.getDefault().lookup(CompilationViewerFactory.class);
        viewer = factory.createViewer(rangeSliderModel);
        content = new InstanceContent();
        graphContent = new InstanceContent();
        this.associateLookup(new ProxyLookup(new Lookup[]{viewer.getLookup(), new AbstractLookup(graphContent), new AbstractLookup(content)}));
        content.add(rangeSliderModel);
        content.add(diagramProvider);

        rangeSliderModel.getDiagramChangedEvent().addListener(diagramChangedListener);
        rangeSliderModel.selectGraph(graph);

        toolBar.add(PrevDiagramAction.get(PrevDiagramAction.class));
        toolBar.add(NextDiagramAction.get(NextDiagramAction.class));

        toolBar.addSeparator();
        toolBar.add(UndoAction.get(UndoAction.class));
        toolBar.add(RedoAction.get(RedoAction.class));

        toolBar.addSeparator();

        Action action = Utilities.actionsForPath("QuickSearchShadow").get(0);
        Component quicksearch = ((Presenter.Toolbar) action).getToolbarPresenter();
        quicksearch.setMinimumSize(quicksearch.getPreferredSize()); // necessary for GTK LAF
        toolBar.add(quicksearch);

        toolBar.add(Box.createHorizontalGlue());        
        toolBar.add(viewer.getToolBarComponent());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           new JScrollPane(rangeSlider), viewer.getComponent());
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
        this.add(splitPane, BorderLayout.CENTER);
        
        updateDisplayName();
    }

    public DiagramViewModel getDiagramModel() {
        return rangeSliderModel;
    }

    public void showPrevDiagram() {
        int fp = getModel().getFirstPosition();
        int sp = getModel().getSecondPosition();
        if (fp != 0) {
            fp--;
            sp--;
            getModel().setPositions(fp, sp);
        }
    }

    public DiagramViewModel getModel() {
        return rangeSliderModel;
    }

    public FilterChain getFilterChain() {
        return getModel().getFilterChain();
    }

    public static EditorTopComponent getActive() {
        Set<? extends Mode> modes = WindowManager.getDefault().getModes();
        for (Mode m : modes) {
            TopComponent tc = m.getSelectedTopComponent();
            if (tc instanceof EditorTopComponent) {
                return (EditorTopComponent) tc;
            }
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, "jCheckBox1");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private ChangedListener<DiagramViewModel> diagramChangedListener = new ChangedListener<DiagramViewModel>() {

        @Override
        public void changed(DiagramViewModel source) {
            updateDisplayName();
            Collection<Object> list = new ArrayList<>();
            list.add(new EditorInputGraphProvider(EditorTopComponent.this));
            graphContent.set(list, null);
            diagramProvider.getChangedEvent().fire();
        }
        
    };

    public void setSelectedFigures(List<Figure> list) {
        viewer.setSelection(list);
    }

    public void setSelectedNodes(Set<InputNode> nodes) {

        List<Figure> list = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        for (InputNode n : nodes) {
            ids.add(n.getId());
        }

        for (Figure f : getModel().getDiagramToView().getFigures()) {
            for (InputNode n : f.getSource().getSourceNodes()) {
                if (ids.contains(n.getId())) {
                    list.add(f);
                    break;
                }
            }
        }

        setSelectedFigures(list);
    }

    public void extract() {
        getModel().showOnly(getModel().getSelectedNodes());
    }

    public void hideNodes() {
        Set<Integer> selectedNodes = this.getModel().getSelectedNodes();
        HashSet<Integer> nodes = new HashSet<>(getModel().getHiddenNodes());
        nodes.addAll(selectedNodes);
        this.getModel().showNot(nodes);
    }

    public void expandPredecessors() {
        Set<Figure> oldSelection = getModel().getSelectedFigures();
        Set<Figure> figures = new HashSet<>();

        for (Figure f : this.getDiagramModel().getDiagramToView().getFigures()) {
            boolean ok = false;
            if (oldSelection.contains(f)) {
                ok = true;
            } else {
                for (Figure pred : f.getSuccessors()) {
                    if (oldSelection.contains(pred)) {
                        ok = true;
                        break;
                    }
                }
            }

            if (ok) {
                figures.add(f);
            }
        }

        getModel().showAll(figures);
    }

    public void expandSuccessors() {
        Set<Figure> oldSelection = getModel().getSelectedFigures();
        Set<Figure> figures = new HashSet<>();

        for (Figure f : this.getDiagramModel().getDiagramToView().getFigures()) {
            boolean ok = false;
            if (oldSelection.contains(f)) {
                ok = true;
            } else {
                for (Figure succ : f.getPredecessors()) {
                    if (oldSelection.contains(succ)) {
                        ok = true;
                        break;
                    }
                }
            }

            if (ok) {
                figures.add(f);
            }
        }

        getModel().showAll(figures);
    }

    public void showAll() {
        getModel().showNot(new HashSet<Integer>());
    }

    public Diagram getDiagram() {
        return getDiagramModel().getDiagramToView();
    }

    @Override
    public void requestActive() {
        super.requestActive();
        viewer.getComponent().requestFocus();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return viewer.getUndoRedo();
    }

}
