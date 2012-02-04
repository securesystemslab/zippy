/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 */
package com.oracle.graal.visualizer.outline.server;

import com.sun.hotspot.igv.data.GraphDocument;
import com.sun.hotspot.igv.data.Group;
import com.sun.hotspot.igv.settings.Settings;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.net.InetAddress;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ServerPanel extends JPanel {

    private final static int BORDER_SIZE = 2;
    JLabel label;
    private Server server;
    private int port = -1;
    private int numberOfConnections;
    private final GraphDocument document;
    private final PreferenceChangeListener preferenceChanged = new PreferenceChangeListener() {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            updateServer();
        }
    };
    private ServerCallback callback = new ServerCallback() {

        @Override
        public void connectionOpened(InetAddress inetAddress) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    numberOfConnections++;
                    updateLabel();
                }
            });
        }

        @Override
        public void connectionClosed() {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    numberOfConnections--;
                    updateLabel();
                }
            });
        }

        @Override
        public void started(Group g) {
            document.addElement(g);
        }
    };

    private void updateLabel() {
        if (numberOfConnections == 0) {
            label.setText(String.format("Listening on %d", port));
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
        } else {
            label.setText(String.format("%d connections", numberOfConnections));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
    }

    private void updateServer() {
        int curPort = Integer.parseInt(Settings.get().get(Settings.PORT, Settings.PORT_DEFAULT));
        if (curPort != port) {
            port = curPort;
            if (server != null) {
                server.shutdown();
            }
            server = Server.start(callback, port);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateLabel();
                }
            });
        }
    }

    public ServerPanel(GraphDocument document) {

        this.document = document;
        Settings.get().addPreferenceChangeListener(preferenceChanged);
        label = new JLabel();
        label.setBorder(BorderFactory.createEmptyBorder(0, BORDER_SIZE, 0, BORDER_SIZE));
        this.setLayout(new BorderLayout());
        this.add(label, BorderLayout.WEST);
        updateServer();
    }

    public Component getToolbarPresenter() {
        return label;
    }
}
