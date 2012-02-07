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
package com.oracle.graal.visualizer.sharedactions;

import com.sun.hotspot.igv.svg.BatikSVG;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.JFileChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbPreferences;

@ActionID(id = "com.oracle.graal.visualizer.sharedactions.ExportSVGAction", category = "File")
@ActionRegistration(displayName = "Export", iconBase = "com/oracle/graal/visualizer/sharedactions/images/export.png")
@ActionReference(path = "Menu/File", position = 600)
public class ExportSVGAction implements ActionListener {

    private static final String PREFERENCE_DIR = "dir";
    private ExportSVGCookie exportCookie;

    public ExportSVGAction(ExportSVGCookie exportCookie) {
        this.exportCookie = exportCookie;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Graphics2D svgGenerator = BatikSVG.createGraphicsObject();
        if (svgGenerator == null) {
            NotifyDescriptor message = new NotifyDescriptor.Message("For export to SVG files the Batik SVG Toolkit must be intalled.", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(message);
            return;
        }
        
        File f = selectFile();
        if (f != null) {
            exportCookie.paint(svgGenerator);
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

    private File selectFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "SVG files (*.svg)";
            }
        });
        fc.setCurrentDirectory(new File(NbPreferences.forModule(ExportSVGAction.class).get(PREFERENCE_DIR, "~")));


        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".svg");
            }

            File dir = file;
            if (!dir.isDirectory()) {
                dir = dir.getParentFile();
            }

            NbPreferences.forModule(ExportSVGAction.class).put(PREFERENCE_DIR, dir.getAbsolutePath());
            return file;
        }

        return null;
    }
}
