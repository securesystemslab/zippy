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
package com.sun.hotspot.igv.graphtexteditor;

import com.sun.hotspot.igv.data.ChangedListener;
import com.sun.hotspot.igv.texteditor.*;
import com.sun.hotspot.igv.data.InputGraph;
import com.sun.hotspot.igv.data.Pair;
import com.sun.hotspot.igv.graph.Diagram;
import com.sun.hotspot.igv.graph.services.DiagramProvider;
import com.sun.hotspot.igv.graphtotext.services.GraphToTextConverter;
import com.sun.hotspot.igv.selectioncoordinator.SelectionCoordinator;
import com.sun.hotspot.igv.structuredtext.StructuredText;
import com.sun.hotspot.igv.util.LookupHistory;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author Thomas Wuerthinger
 */
final class TextTopComponent extends TopComponent implements LookupListener {

    private static TextTopComponent instance;
    private Lookup.Result result = null;
    private static final String PREFERRED_ID = "TextTopComponent";
    private Diagram lastDiagram;
    private TextEditor leftEditor;
    private TextEditor rightEditor;
    private TextEditor singleEditor;
    private JSplitPane splitPane;
    private CardLayout cardLayout;
    private JPanel cardLayoutPanel;
    private boolean firstTimeSlider = true;

    private static final String TWO_GRAPHS = "twoGraphs";
    private static final String ONE_GRAPH = "oneGraph";
    private static final String NO_GRAPH = "noGraph";

    private DiagramProvider currentDiagramProvider;

    private TextTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TextTopComponent.class, "CTL_TextTopComponent"));
        setToolTipText(NbBundle.getMessage(TextTopComponent.class, "HINT_TextTopComponent"));

        // Card layout for three different views.
        cardLayout = new CardLayout();
        cardLayoutPanel = new JPanel(cardLayout);
        this.setLayout(new BorderLayout());
        this.add(cardLayoutPanel, BorderLayout.CENTER);

        // No graph selected.
        JLabel noGraphLabel = new JLabel("No graph opened");
        noGraphLabel.setBackground(Color.red);
        //noGraphPanel.add(noGraphLabel);
        cardLayoutPanel.add(noGraphLabel, NO_GRAPH);

        // Single graph selected.
        singleEditor = new TextEditor();
        cardLayoutPanel.add(singleEditor.getComponent(), ONE_GRAPH);

        // Graph difference => show split pane with two graphs.
        splitPane = new JSplitPane();
        leftEditor = new TextEditor();
        splitPane.setLeftComponent(leftEditor.getComponent());
        rightEditor = new TextEditor();
        splitPane.setRightComponent(rightEditor.getComponent());
        cardLayoutPanel.add(splitPane, TWO_GRAPHS);
    }


    private StructuredText convert(InputGraph graph, Diagram diagram) {
        Collection<? extends GraphToTextConverter> converters = Lookup.getDefault().lookupAll(GraphToTextConverter.class);
        StructuredText text = null;
        if (converters.size() == 0) {
            text = new StructuredText(graph.getName());
            text.println("No graph-to-text converter exists!");
            return text;
        }

        for (GraphToTextConverter converter : converters) {
            if (converter.canConvert(graph)) {
                text = converter.convert(graph, diagram);
                if (text == null) {
                    text = new StructuredText(graph.getName());
                    text.println("Class " + converter.getClass().getName() + " misbehaved and returned null on graph-to-text conversion!");
                }
                return text;
            }
        }

        text = new StructuredText(graph.getName());
        text.println("No appropriate graph-to-text converter found!");
        return text;
    }

    private void updateDiagram(Diagram diagram) {

        if (diagram == lastDiagram) {
            // No change => return.
            return;
        }

        lastDiagram = diagram;

        if (diagram == null) {
            showCard(NO_GRAPH);
        } else if (diagram.getGraph().getSourceGraphs() != null) {
            showCard(TWO_GRAPHS);
            if (firstTimeSlider) {
                splitPane.setDividerLocation(0.5);
            }
            firstTimeSlider = false;
            Pair<InputGraph, InputGraph> graphs = diagram.getGraph().getSourceGraphs();
            leftEditor.setStructuredText(convert(graphs.getLeft(), diagram));
            rightEditor.setStructuredText(convert(graphs.getRight(), diagram));

            // TODO: Hack to update view - remove
            SelectionCoordinator.getInstance().getHighlightedChangedEvent().fire();
        } else {
            showCard(ONE_GRAPH);
            StructuredText text = convert(diagram.getGraph(), diagram);
            singleEditor.setStructuredText(text);

            // TODO: Hack to update view - remove
            SelectionCoordinator.getInstance().getHighlightedChangedEvent().fire();
        }
    }
    
    private ChangedListener<DiagramProvider> diagramChangedListener = new ChangedListener<DiagramProvider>() {

        public void changed(DiagramProvider source) {
            updateDiagram(source.getDiagram());
        }
        
    };

    private void updateDiagramProvider(DiagramProvider provider) {

        if (provider == currentDiagramProvider) {
            return;
        }

        if (currentDiagramProvider != null) {
            currentDiagramProvider.getChangedEvent().removeListener(diagramChangedListener);
        }

        currentDiagramProvider = provider;

        if (currentDiagramProvider != null) {
            currentDiagramProvider.getChangedEvent().addListener(diagramChangedListener);
            updateDiagram(currentDiagramProvider.getDiagram());
        } else {
            updateDiagram(null);
        }
    }

    private void showCard(final String card) {
        cardLayout.show(cardLayoutPanel, card);
    }

    public void resultChanged(LookupEvent lookupEvent) {
        DiagramProvider p = Utilities.actionsGlobalContext().lookup(DiagramProvider.class);

        if (p == null) {
            p = LookupHistory.getLast(DiagramProvider.class);
        }

        updateDiagramProvider(p);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized TextTopComponent getDefault() {
        if (instance == null) {
            instance = new TextTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the TextTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized TextTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(TextTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof TextTopComponent) {
            return (TextTopComponent) win;
        }
        Logger.getLogger(TextTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {

        DiagramProvider p = LookupHistory.getLast(DiagramProvider.class);
        updateDiagramProvider(p);

        Lookup.Template tpl = new Lookup.Template(DiagramProvider.class);
        result = Utilities.actionsGlobalContext().lookup(tpl);
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
        result = null;
        updateDiagramProvider(null);
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void requestActive() {
        super.requestActive();
        cardLayoutPanel.requestFocus();
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return TextTopComponent.getDefault();
        }
    }
}
