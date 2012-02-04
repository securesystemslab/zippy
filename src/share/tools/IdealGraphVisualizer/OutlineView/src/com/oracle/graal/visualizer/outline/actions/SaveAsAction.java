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

package com.oracle.graal.visualizer.outline.actions;

import com.oracle.graal.visualizer.outline.OutlineTopComponent;
import com.sun.hotspot.igv.data.Folder;
import com.sun.hotspot.igv.data.FolderElement;
import com.sun.hotspot.igv.data.GraphDocument;
import com.sun.hotspot.igv.data.serialization.Printer;
import com.sun.hotspot.igv.settings.Settings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(id = "com.oracle.graal.visualizer.outline.actions.SaveAsAction", category = "File")
@ActionRegistration(displayName = "Save as...", iconBase="com/oracle/graal/visualizer/outline/images/save.png")
@ActionReferences(value = {
    @ActionReference(path = "Menu/File", position = 200),
    @ActionReference(path = OutlineTopComponent.NODE_ACTIONS_FOLDER)})
public final class SaveAsAction implements ActionListener {

    private final List<FolderElement> elements;
    
    public SaveAsAction(List<FolderElement> elements) {
        this.elements = elements;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        GraphDocument doc = new GraphDocument();
        outer: for (FolderElement element : elements) {
            Folder cur = element.getParent();
            while (cur instanceof FolderElement) {
                FolderElement curElement = (FolderElement) cur;
                if (elements.contains(curElement)) {
                    continue outer;
                }
                cur = curElement.getParent();
            }
            
            Folder previousParent = element.getParent();
            doc.addElement(element);
            element.setParent(previousParent);
        }

        save(doc);
    }

    public static void save(GraphDocument doc) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(ImportAction.getFileFilter());
        fc.setCurrentDirectory(new File(Settings.get().get(Settings.DIRECTORY, Settings.DIRECTORY_DEFAULT)));

        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".xml");
            }

            File dir = file;
            if (!dir.isDirectory()) {
                dir = dir.getParentFile();
            }
            Settings.get().put(Settings.DIRECTORY, dir.getAbsolutePath());
            try {
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
                    Printer p = new Printer();
                    p.export(writer, doc);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error writing file " + file.getAbsolutePath());
            }
        }
    }
}
