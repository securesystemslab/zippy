/*
 * Copyright (c) 2009-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product that is
 * described in this document. In particular, and without limitation, these intellectual property rights may include one
 * or more of the U.S. patents listed at http://www.sun.com/patents and one or more additional patents or pending patent
 * applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun Microsystems, Inc. standard
 * license agreement and applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or registered
 * trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks are used under license and
 * are trademarks or registered trademarks of SPARC International, Inc. in the U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open Company, Ltd.
 */
package com.sun.hotspot.c1x;

import java.lang.reflect.Proxy;
import java.net.*;

import com.sun.c1x.*;
import com.sun.c1x.target.amd64.*;
import com.sun.cri.ci.*;
import com.sun.cri.ri.*;
import com.sun.cri.xir.*;
import com.sun.hotspot.c1x.logging.*;
import com.sun.hotspot.c1x.server.CompilationServer.ReplacingInputStream;
import com.sun.hotspot.c1x.server.CompilationServer.ReplacingOutputStream;

/**
 * Singleton class holding the instance of the C1XCompiler.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class Compiler {

    private static Compiler theInstance;

    public static Compiler getInstance() {
        if (theInstance == null) {
            theInstance = new Compiler();
        }
        return theInstance;
    }

    private static VMEntries vmEntries;

    public static VMExits initializeServer(VMEntries entries) {
        if (Logger.ENABLED) {
            vmEntries = LoggingProxy.getProxy(VMEntries.class, entries);
            vmExits = LoggingProxy.getProxy(VMExits.class, new VMExitsNative());
        } else {
            vmEntries = entries;
            vmExits = new VMExitsNative();
        }
        return vmExits;
    }

    private static VMEntries initializeClient(VMExits exits) {
        vmEntries = new VMEntriesNative();
        vmExits = exits;
        return vmEntries;
    }

    public static VMEntries getVMEntries() {
        if (vmEntries == null) {
            try {
                if (Logger.ENABLED) {
                    vmEntries = LoggingProxy.getProxy(VMEntries.class, new VMEntriesNative());
                } else {
                    vmEntries = new VMEntriesNative();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return vmEntries;
    }

    private static VMExits vmExits;

    public static VMExits getVMExits() {
        if (vmExits == null) {
            String remote = System.getProperty("c1x.remote");
            assert theInstance == null;
            assert vmEntries == null;
            try {
                if (remote != null) {
                    System.out.println("C1X compiler started in client/server mode, connection to server " + remote);
                    Socket socket = new Socket(remote, 1199);
                    ReplacingOutputStream output = new ReplacingOutputStream(socket.getOutputStream());
                    ReplacingInputStream input = new ReplacingInputStream(socket.getInputStream());

                    InvocationSocket invocation = new InvocationSocket(output, input);
                    VMExits exits = (VMExits) Proxy.newProxyInstance(VMExits.class.getClassLoader(), new Class<?>[] { VMExits.class}, invocation);
                    VMEntries entries = Compiler.initializeClient(exits);
                    invocation.setDelegate(entries);
                } else {
                    if (Logger.ENABLED) {
                        vmExits = LoggingProxy.getProxy(VMExits.class, new VMExitsNative());
                    } else {
                        vmExits = new VMExitsNative();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return vmExits;
    }

    private final CiCompiler compiler;
    private final HotSpotVMConfig config;
    private final HotSpotRuntime runtime;
    private final RiRegisterConfig registerConfig;
    private final CiTarget target;
    private final RiXirGenerator generator;

    private Compiler() {
        config = getVMEntries().getConfiguration();
        config.check();

        runtime = new HotSpotRuntime(config);
        final int wordSize = 8;
        final int stackFrameAlignment = 16;
        registerConfig = new HotSpotRegisterConfig(config);
        target = new HotSpotTarget(new AMD64(), registerConfig, true, wordSize, wordSize, wordSize, stackFrameAlignment, config.vmPageSize, wordSize, wordSize, config.codeEntryAlignment, true);

        if (Logger.ENABLED) {
            generator = LoggingProxy.getProxy(RiXirGenerator.class, new HotSpotXirGenerator(config, target, registerConfig));
        } else {
            generator = new HotSpotXirGenerator(config, target, registerConfig);
        }
        compiler = new C1XCompiler(runtime, target, generator);

        C1XOptions.setOptimizationLevel(3);
        C1XOptions.UseDeopt = false;
        C1XOptions.IRChecking = Logger.ENABLED;
        C1XOptions.TraceBytecodeParserLevel = 0;
        // C1XOptions.TraceBytecodeParserLevel = Logger.ENABLED ? 4 : 0;
        C1XOptions.PrintCFGToFile = false;
        C1XOptions.PrintAssembly = false;// true;
        C1XOptions.PrintCompilation = Logger.ENABLED;
        C1XOptions.GenAssertionCode = Logger.ENABLED;
        C1XOptions.DetailedAsserts = Logger.ENABLED;

        C1XOptions.GenSpecialDivChecks = true;
        C1XOptions.AlignCallsForPatching = true;
        C1XOptions.NullCheckUniquePc = true;
        C1XOptions.invokeinterfaceTemplatePos = true;
    }

    public CiCompiler getCompiler() {
        return compiler;
    }

    public HotSpotVMConfig getConfig() {
        return config;
    }

    public HotSpotRuntime getRuntime() {
        return runtime;
    }

    public RiRegisterConfig getRegisterConfig() {
        return registerConfig;
    }

    public CiTarget getTarget() {
        return target;
    }

    public RiXirGenerator getGenerator() {
        return generator;
    }

}
