/*
 * Copyright (c) 2010 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.hotspot.c1x.server;

import java.io.*;
import java.lang.reflect.Proxy;
import java.net.*;
import java.rmi.registry.*;

import javax.net.*;

import com.sun.hotspot.c1x.*;
import com.sun.hotspot.c1x.Compiler;
import com.sun.hotspot.c1x.InvocationSocket.DelegateCallback;
import com.sun.hotspot.c1x.logging.*;
import com.sun.hotspot.c1x.server.ReplacingStreams.ReplacingInputStream;
import com.sun.hotspot.c1x.server.ReplacingStreams.ReplacingOutputStream;

/**
 * Server side of the client/server compilation model.
 *
 * @author Lukas Stadler
 */
public class CompilationServer {

    private ReplacingOutputStream output;
    private ReplacingInputStream input;

    public static void main(String[] args) throws Exception {
        new CompilationServer().run();
    }

    private void run() throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(1199);
        do {
            Socket socket = null;
            ReplacingStreams streams = new ReplacingStreams();
            try {
                Logger.log("Compilation server ready, waiting for client to connect...");
                socket = serverSocket.accept();
                Logger.log("Connected to " + socket.getRemoteSocketAddress());

                output = streams.new ReplacingOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                // required, because creating an ObjectOutputStream writes a header, but doesn't flush the stream
                output.flush();
                input = streams.new ReplacingInputStream(new BufferedInputStream(socket.getInputStream()));

                final InvocationSocket invocation = new InvocationSocket(output, input);
                invocation.setDelegateCallback(new DelegateCallback() {
                    public Object getDelegate() {
                        VMEntries entries = (VMEntries) Proxy.newProxyInstance(VMEntries.class.getClassLoader(), new Class<?>[] { VMEntries.class}, invocation);
                        Compiler compiler = CompilerImpl.initializeServer(entries);
                        input.setCompiler(compiler);
                        return compiler.getVMExits();
                    }
                });

                invocation.waitForResult();
            } catch (IOException e) {
                e.printStackTrace();
                if (socket != null) {
                    socket.close();
                }
            }
        } while (false);
    }
}
