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
package com.oracle.graal.visualizer.editor;

import com.sun.hotspot.igv.graph.Figure;
import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.Lookups;

class SplitCompilationViewer implements CompilationViewer {

    private JSplitPane splitPane;
    private Component firstPanel;
    private Component secondPanel;
    private static final String DIVIDER_LOCATION = "dividerLocation";
    private final PropertyChangeListener splitChanged = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent changeEvent) {
            String propertyName = changeEvent.getPropertyName();
            if (propertyName.equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                setLastDividerLocation((Integer) changeEvent.getNewValue());
            }
        }
    };

    private static void setLastDividerLocation(int pos) {
        NbPreferences.forModule(SplitCompilationViewer.class).put(DIVIDER_LOCATION, Integer.toString(pos));
    }

    private static int getLastDividerLocation() {
        try {
            return Integer.parseInt(NbPreferences.forModule(SplitCompilationViewer.class).get(DIVIDER_LOCATION, "400"));
        } catch (NumberFormatException e) {
            return 400;
        }
    }

    public SplitCompilationViewer(CompilationViewer firstViewer, CompilationViewer secondViewer) {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        firstPanel = createComponent(firstViewer);
        secondPanel = createComponent(secondViewer);
        splitPane.add(firstPanel);
        splitPane.add(secondPanel);
        splitPane.addPropertyChangeListener(splitChanged);
        splitPane.setDividerLocation(getLastDividerLocation());
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed();
    }

    @Override
    public Component getComponent() {
        return splitPane;
    }

    private Component createComponent(CompilationViewer viewer) {
        return viewer.getComponent();
    }
}
