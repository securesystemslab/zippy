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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

public class Server {

    private Runnable serverRunnable;
    private ServerSocket serverSocket;
    
    private Server() {}
    

    public static Server start(final ServerCallback callback, int port) {

        final Server server = new Server();
        
        try {
            server.serverSocket = new java.net.ServerSocket(port);
        } catch (IOException ex) {
            NotifyDescriptor message = new NotifyDescriptor.Message("Could not create server. Listening for incoming data is disabled.", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(message);
            return null;
        }

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Socket clientSocket = server.serverSocket.accept();
                        if (server.serverRunnable != this) {
                            clientSocket.close();
                            return;
                        }
                        RequestProcessor.getDefault().post(new Client(clientSocket, callback), 0, Thread.MAX_PRIORITY);
                    } catch (IOException ex) {
                        server.serverSocket = null;
                        NotifyDescriptor message = new NotifyDescriptor.Message("Error during listening for incoming connections. Listening for incoming data is disabled.", NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(message);
                        return;
                    }
                }
            }
        };

        server.serverRunnable = runnable;
        RequestProcessor.getDefault().post(runnable, 0, Thread.MAX_PRIORITY);
        return server;
    }

    void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException ex) {
        }
        
        serverSocket = null;
    }
}
