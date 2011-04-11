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
package com.sun.hotspot.c1x;

import java.io.*;
import java.lang.reflect.*;

import com.sun.hotspot.c1x.logging.*;

/**
 * A java.lang.reflect proxy that communicates over a socket connection.
 *
 * Calling a method sends the method name and the parameters through the socket. Afterwards this class waits for a result.
 * While waiting for a result three types of objects can arrive through the socket: a method invocation, a method result or an exception.
 * Method invocation can thus be recursive.
 *
 * @author Lukas Stadler
 */
public class InvocationSocket {

    private final ObjectOutputStream output;
    private final ObjectInputStream input;

    public InvocationSocket(ObjectOutputStream output, ObjectInputStream input) {
        this.output = output;
        this.input = input;
    }

    private static class Invocation implements Serializable {

        public Object receiver;
        public String methodName;
        public Object[] args;

        public Invocation(Object receiver, String methodName, Object[] args) {
            this.receiver = receiver;
            this.methodName = methodName;
            this.args = args;
        }
    }

    private static class Result implements Serializable {

        public Object result;

        public Result(Object result) {
            this.result = result;
        }
    }

    public class Handler implements InvocationHandler {
        private Object receiver;

        public Handler(Object receiver) {
            this.receiver = receiver;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!method.getDeclaringClass().isInterface()) {
                return method.invoke(receiver, args);
            }
            try {
                Logger.startScope("invoking remote " + method.getName());
                output.writeObject(new Invocation(receiver, method.getName(), args));
                output.flush();
                return waitForResult();
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            } finally {
                Logger.endScope("");
            }
        }
    }

    public Object waitForResult() throws IOException, ClassNotFoundException {
        while (true) {
            Object in = input.readObject();
            if (in instanceof Result) {
                return ((Result) in).result;
            } else if (in instanceof RuntimeException) {
                throw (RuntimeException) in;
            } else if (in instanceof Throwable) {
                throw new RuntimeException((Throwable) in);
            }

            Invocation invoke = (Invocation) in;
            Method method = null;
            for (Class<?> clazz = invoke.receiver.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (invoke.methodName.equals(m.getName())) {
                        method = m;
                        break;
                    }
                }
            }
            if (method == null) {
                Exception e = new UnsupportedOperationException("unknown method " + invoke.methodName);
                e.printStackTrace();
                output.writeObject(e);
                output.flush();
            } else {
                Object result;
                try {
                    if (invoke.args == null) {
                        Logger.startScope("invoking local " + invoke.methodName);
                        result = method.invoke(invoke.receiver);
                    } else {
                        if (Logger.ENABLED) {
                            StringBuilder str = new StringBuilder();
                            str.append("invoking local " + invoke.methodName + "(");
                            for (int i = 0; i < invoke.args.length; i++) {
                                str.append(i == 0 ? "" : ", ");
                                str.append(Logger.pretty(invoke.args[i]));
                            }
                            str.append(")");
                            Logger.startScope(str.toString());
                        }
                        result = method.invoke(invoke.receiver, invoke.args);
                    }
                    result = new Result(result);
                } catch (IllegalArgumentException e) {
                    System.out.println("error while invoking " + invoke.methodName);
                    e.getCause().printStackTrace();
                    result = e.getCause();
                } catch (InvocationTargetException e) {
                    System.out.println("error while invoking " + invoke.methodName);
                    e.getCause().printStackTrace();
                    result = e.getCause();
                } catch (IllegalAccessException e) {
                    System.out.println("error while invoking " + invoke.methodName);
                    e.getCause().printStackTrace();
                    result = e.getCause();
                } finally {
                    Logger.endScope("");
                }
                output.writeObject(result);
                output.flush();
            }
        }
    }

    public void sendResult(Object obj) throws IOException {
        output.writeObject(new Result(obj));
        output.flush();
    }

}
