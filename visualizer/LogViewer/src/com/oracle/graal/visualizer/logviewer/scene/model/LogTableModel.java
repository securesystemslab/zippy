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

package com.oracle.graal.visualizer.logviewer.scene.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class LogTableModel extends AbstractTableModel {
    private final static String[] columnNames = {"Line #", "Method", "Scope", "Node", "Log Text"};
    
    private List<TableLine> entries = new ArrayList<>();
    
    public void setLogEntries(List<TableLine> entries) {
        this.entries = entries;
        fireTableDataChanged();
    }
    
    public TableLine getTableLine(int line) {
        return entries.get(line);
    }
    
    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int i, int i1) {
        switch (i1) {
            case 0:
                return entries.get(i).getLineNr();
            case 1:
                return entries.get(i).getLogLine().getMethod().getName();
            case 2:
                return entries.get(i).getLogLine().getScope()!=null?
                           entries.get(i).getLogLine().getScope().getName():"";
            case 3:
                return entries.get(i).getLogLine().getNode()!=null?
                           entries.get(i).getLogLine().getNode().getName():"";
            case 4:
                return entries.get(i).getLogLine().getText();
        }
        return null;
    }
    
}
