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

package com.oracle.graal.visualizer.logviewer.scene;

import com.oracle.graal.visualizer.logviewer.model.LogParser;
import java.awt.Component;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ImportLogErrorDialog {
    
    public static void showDialog(Component parent, List<LogParser.ParseError> errors) {
        JTextArea txaErrors = new JTextArea();
        
        for (LogParser.ParseError error : errors) {
            txaErrors.append("Error at line " + error.getLineNumber());
            txaErrors.append(": " + error.getMessage());
            txaErrors.append("\n");
            txaErrors.append("Log line: " + error.getLine());
            txaErrors.append("\n");
        }
        
        JScrollPane scpErrors = new JScrollPane(txaErrors);
        
        JOptionPane.showMessageDialog(parent, scpErrors, "Parse errors", JOptionPane.ERROR_MESSAGE);
    }
}
