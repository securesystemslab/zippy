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
import com.sun.hotspot.igv.data.Properties;
import com.sun.hotspot.igv.texteditor.*;
import com.sun.hotspot.igv.data.InputGraph;
import com.sun.hotspot.igv.data.Pair;
import com.sun.hotspot.igv.data.Property;
import com.sun.hotspot.igv.graph.Diagram;
import com.sun.hotspot.igv.graph.services.DiagramProvider;
import com.sun.hotspot.igv.graphtotext.services.GraphToTextConverter;
import com.sun.hotspot.igv.selectioncoordinator.SelectionCoordinator;
import com.sun.hotspot.igv.structuredtext.MultiElement;
import com.sun.hotspot.igv.structuredtext.StructuredText;
import com.sun.hotspot.igv.util.LookupHistory;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author Thomas Wuerthinger
 * @author Peter Hofer
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
    private JComboBox sourceCombo;
    private boolean firstTimeSplitter = true;
    private JPanel textDiffPanel;

    private static final String TWO_GRAPHS_TEXT_DIFF = "twoGraphsTextDiff";
    private static final String TWO_GRAPHS = "twoGraphs";
    private static final String ONE_GRAPH = "oneGraph";
    private static final String NO_GRAPH = "noGraph";

    private static final String GRAPH_TEXT_REPRESENTATION = "< Graph Text Representation >";

    private DiagramProvider currentDiagramProvider;

    private TextTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TextTopComponent.class, "CTL_TextTopComponent"));
        setToolTipText(NbBundle.getMessage(TextTopComponent.class, "HINT_TextTopComponent"));

        setLayout(new BorderLayout());

        // Selector for displayed data
        JToolBar sourceSelectBar = new JToolBar();
        sourceSelectBar.setLayout(new BorderLayout());
        sourceSelectBar.setFloatable(false);
        sourceSelectBar.add(new JLabel("Show: "), BorderLayout.WEST);
        sourceCombo = new JComboBox();
        sourceCombo.addItem(GRAPH_TEXT_REPRESENTATION);
        sourceCombo.addItemListener(sourceSelectionListener);
        sourceSelectBar.add(sourceCombo, BorderLayout.CENTER);
        add(sourceSelectBar, BorderLayout.NORTH);

        // Card layout for three different views.
        cardLayout = new CardLayout();
        cardLayoutPanel = new JPanel(cardLayout);
        add(cardLayoutPanel, BorderLayout.CENTER);

        // No graph selected.
        JLabel noGraphLabel = new JLabel("No graph open.", JLabel.CENTER);
        noGraphLabel.setOpaque(true);
        noGraphLabel.setBackground(Color.WHITE);
        cardLayoutPanel.add(noGraphLabel, NO_GRAPH);

        // Single graph selected.
        singleEditor = new TextEditor();
        cardLayoutPanel.add(singleEditor.getComponent(), ONE_GRAPH);

        // Graph difference => show split pane with two graphs.
        splitPane = new JSplitPane();
        leftEditor = new TextEditor();
        rightEditor = new TextEditor();
        // Work around a problem with JSplitPane and the NetBeans editor:
        // setDividerLocation() doesn't work when the split pane has not been
        // layouted and painted yet. JSplitPane then initially uses a tiny width
        // for the left editor component, which causes the editor to calculate
        // invalid offsets and constantly throw exceptions, particularly on
        // mouse events. Thus, defer adding the two components and setting the
        // divider's location.
        splitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (firstTimeSplitter && splitPane.getWidth() > 0) {
                    splitPane.setLeftComponent(leftEditor.getComponent());
                    splitPane.setRightComponent(rightEditor.getComponent());
                    splitPane.setDividerLocation(0.5);
                    firstTimeSplitter = false;
                }
            }
        });
        cardLayoutPanel.add(splitPane, TWO_GRAPHS);
        
        // Text difference => NetBeans diff view
        // Diff component is created and added on demand
        textDiffPanel = new JPanel(new BorderLayout());
        cardLayoutPanel.add(textDiffPanel, TWO_GRAPHS_TEXT_DIFF);
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

    private StructuredText createStructuredPlainText(String name, String text) {
        StructuredText structured = new StructuredText(name);
        MultiElement multi = new MultiElement();
        multi.print(text);
        structured.addChild(multi);
        return structured;
    }

    private ItemListener sourceSelectionListener = new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() == GRAPH_TEXT_REPRESENTATION) {
                    displayDiagram(lastDiagram);
                } else {
                    displayGroupProperty(lastDiagram, (String) e.getItem());
                }
            }
        }
    };

    private void setDiagram(Diagram diagram) {
        if (diagram == lastDiagram) {
            // No change => return.
            return;
        }
        lastDiagram = diagram;

        // Rebuild combobox choices
        Object selection = sourceCombo.getSelectedItem();
        sourceCombo.removeAllItems();
        // NOTE: addItem() makes the first inserted item the selected item,
        //       so use insertItemAt() instead
        sourceCombo.insertItemAt(GRAPH_TEXT_REPRESENTATION, 0);
        if (diagram != null) {
            if (diagram.getGraph().getSourceGraphs() != null) {
                // Diff graph with source graphs with possibly different groups:
                // show properties from both graphs
                Pair<InputGraph, InputGraph> sourceGraphs = diagram.getGraph().getSourceGraphs();
                Properties props = new Properties(sourceGraphs.getLeft().getGroup().getProperties());
                if (sourceGraphs.getLeft().getGroup() != sourceGraphs.getRight().getGroup()) {
                    props.add(sourceGraphs.getRight().getGroup().getProperties());
                }
                for (Property p : props) {
                    sourceCombo.addItem(p.getName());
                }
            } else {
                // Single graph
                for (Property p : diagram.getGraph().getGroup().getProperties()) {
                    sourceCombo.addItem(p.getName());
                }
            }
        }
        // NOTE: The following triggers a display update.
        sourceCombo.setSelectedItem(selection);
        if (sourceCombo.getSelectedItem() == null) {
            // previously selected property doesn't exist in new graph's group:
            // default to show graph representation
            sourceCombo.setSelectedItem(GRAPH_TEXT_REPRESENTATION);
        }
    }

    private void displayGroupProperty(Diagram diagram, String property) {
        if (diagram == null) {
            showCard(NO_GRAPH);
        } else if (diagram.getGraph().getSourceGraphs() != null) {
            showCard(TWO_GRAPHS_TEXT_DIFF);
            textDiffPanel.removeAll();
            try {
                Pair<InputGraph, InputGraph> sourceGraphs = diagram.getGraph().getSourceGraphs();

                String ltext = sourceGraphs.getLeft().getGroup().getProperties().get(property);
                if (ltext == null) {
                    ltext = "";
                }
                StreamSource leftsrc = StreamSource.createSource("left", sourceGraphs.getLeft().getName(), "text/plain", new StringReader(ltext));

                String rtext = sourceGraphs.getRight().getGroup().getProperties().get(property);
                if (rtext == null) {
                    rtext = "";
                }
                StreamSource rightsrc = StreamSource.createSource("right", sourceGraphs.getRight().getName(), "text/plain", new StringReader(rtext));

                DiffView view = Diff.getDefault().createDiff(leftsrc, rightsrc);
                textDiffPanel.add(view.getComponent(), BorderLayout.CENTER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            textDiffPanel.revalidate(); // required when card was visible before
        } else {
            showCard(ONE_GRAPH);
            String text = diagram.getGraph().getGroup().getProperties().get(property);
            singleEditor.setStructuredText(createStructuredPlainText(diagram.getGraph().getName(), text));
        }
    }

    private void displayDiagram(Diagram diagram) {
        if (diagram == null) {
            showCard(NO_GRAPH);
        } /* This side-by-side view of the source graphs for diff graphs doesn't
           * work properly because nodes that exist only in graph B (the 'new'
           * graph) are in most cases assigned different ids.

            else if (diagram.getGraph().getSourceGraphs() != null) {
            showCard(TWO_GRAPHS);
            Pair<InputGraph, InputGraph> graphs = diagram.getGraph().getSourceGraphs();
            leftEditor.setStructuredText(convert(graphs.getLeft(), diagram));
            rightEditor.setStructuredText(convert(graphs.getRight(), diagram));

            // TODO: Hack to update view - remove
            SelectionCoordinator.getInstance().getHighlightedChangedEvent().fire();
        } */
        else {
            showCard(ONE_GRAPH);
            StructuredText text = convert(diagram.getGraph(), diagram);
            singleEditor.setStructuredText(text);

            // TODO: Hack to update view - remove
            SelectionCoordinator.getInstance().getHighlightedChangedEvent().fire();
        }
    }

    private ChangedListener<DiagramProvider> diagramChangedListener = new ChangedListener<DiagramProvider>() {

        public void changed(DiagramProvider source) {
            setDiagram(source.getDiagram());
        }
        
    };

    private void setDiagramProvider(DiagramProvider provider) {
        if (provider == currentDiagramProvider) {
            return;
        }

        if (currentDiagramProvider != null) {
            currentDiagramProvider.getChangedEvent().removeListener(diagramChangedListener);
        }

        currentDiagramProvider = provider;

        if (currentDiagramProvider != null) {
            currentDiagramProvider.getChangedEvent().addListener(diagramChangedListener);
            setDiagram(currentDiagramProvider.getDiagram());
        } else {
            setDiagram(null);
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

        setDiagramProvider(p);
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
        setDiagramProvider(p);

        Lookup.Template<DiagramProvider> tpl = new Lookup.Template<DiagramProvider>(DiagramProvider.class);
        result = Utilities.actionsGlobalContext().lookup(tpl);
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
        result = null;
        setDiagramProvider(null);
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
