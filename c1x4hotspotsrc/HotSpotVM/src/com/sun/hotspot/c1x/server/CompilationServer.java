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

import com.sun.cri.ci.*;
import com.sun.hotspot.c1x.*;
import com.sun.hotspot.c1x.Compiler;
import com.sun.hotspot.c1x.InvocationSocket.DelegateCallback;
import com.sun.hotspot.c1x.logging.*;

/**
 * Server side of the client/server compilation model.
 *
 * @author Lukas Stadler
 */
public class CompilationServer {

    private Registry registry;
    private ServerSocket serverSocket;
    private Socket socket;

    private ReplacingOutputStream output;
    private ReplacingInputStream input;


    public static void main(String[] args) throws Exception {
        new CompilationServer().run();
    }

    public static class Container implements Serializable {

        public final Class<?> clazz;
        public final Object[] values;

        public Container(Class<?> clazz, Object... values) {
            this.clazz = clazz;
            this.values = values;
        }
    }

    /**
     * Replaces certain cir objects that cannot easily be made Serializable.
     */
    public static class ReplacingOutputStream extends ObjectOutputStream {

        public ReplacingOutputStream(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws IOException {
            Class<? extends Object> clazz = obj.getClass();
            if (clazz == CiConstant.class) {
                CiConstant o = (CiConstant) obj;
                return new Container(clazz, o.kind, o.boxedValue());
            } else if (obj == CiValue.IllegalValue) {
                return new Container(CiValue.class);
            } else if (obj instanceof Compiler) {
                return new Container(Compiler.class);
            }
            return obj;
        }
    }

    /**
     * Replaces certain cir objects that cannot easily be made Serializable.
     */
    public static class ReplacingInputStream extends ObjectInputStream {
        private Compiler compiler;

        public ReplacingInputStream(InputStream in) throws IOException {
            super(in);
            enableResolveObject(true);
        }

        public void setCompiler(Compiler compiler) {
            this.compiler = compiler;
        }

        @Override
        protected Object resolveObject(Object obj) throws IOException {
            if (obj instanceof Container) {
                Container c = (Container) obj;
                if (c.clazz == CiConstant.class) {
                    return CiConstant.forBoxed((CiKind) c.values[0], c.values[1]);
                } else if (c.clazz == CiValue.class) {
                    return CiValue.IllegalValue;
                } else if (c.clazz == Compiler.class) {
                    assert compiler != null;
                    return compiler;
                }
                throw new RuntimeException("unexpected container class: " + c.clazz);
            }
            return obj;
        }
    }

    private void run() throws IOException, ClassNotFoundException {
        serverSocket = ServerSocketFactory.getDefault().createServerSocket(1199);
        do {
            try {
                Logger.log("Compilation server ready, waiting for client to connect...");
                socket = serverSocket.accept();
                Logger.log("Connected to " + socket.getRemoteSocketAddress());

                output = new ReplacingOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                // required, because creating an ObjectOutputStream writes a header, but doesn't flush the stream
                output.flush();
                input = new ReplacingInputStream(new BufferedInputStream(socket.getInputStream()));

                final InvocationSocket invocation = new InvocationSocket(output, input);
                invocation.setDelegateCallback( new DelegateCallback() {
                    public Object getDelegate() {
                        VMEntries entries = (VMEntries) Proxy.newProxyInstance(VMEntries.class.getClassLoader(), new Class<?>[] {VMEntries.class}, invocation);
                        Compiler compiler = Compiler.initializeServer(entries);
                        input.setCompiler(compiler);
                        return compiler.getVMExits();
                    }
                });

                invocation.waitForResult();
            } catch (IOException e) {
                e.printStackTrace();
                socket.close();
            }
        } while (false);
    }
}
