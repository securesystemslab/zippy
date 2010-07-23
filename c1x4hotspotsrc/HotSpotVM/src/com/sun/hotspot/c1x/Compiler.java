/*
 * Copyright (c) 2009 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.c1x.*;
import com.sun.c1x.target.amd64.*;
import com.sun.cri.ci.*;
import com.sun.cri.ri.*;
import com.sun.cri.xir.*;

/**
 *
 * @author Thomas Wuerthinger
 *
 *         Singleton class holding the instance of the C1XCompiler.
 *
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

    public static VMEntries getVMEntries() {
        if (vmEntries == null) {
            System.out.println("getVMEntries");
            try {
                //vmEntries = LoggingProxy.getProxy(VMEntries.class, new VMEntriesNative());
                vmEntries = new VMEntriesNative();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return vmEntries;
    }

    private static VMExits vmExits;

    public static VMExits getVMExits() {
        if (vmExits == null) {
            System.out.println("getVMExits");
            try {
                //vmExits = LoggingProxy.getProxy(VMExits.class, new VMExitsNative());
                vmExits = new VMExitsNative();
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
        final int stackFrameAlignment = 8;
        registerConfig = new HotSpotRegisterConfig(config);
        target = new CiTarget(new AMD64(), registerConfig, true, wordSize, wordSize, wordSize, stackFrameAlignment, config.vmPageSize, wordSize, wordSize, config.codeEntryAlignment, true);
        generator = new HotSpotXirGenerator(config, registerConfig);
        compiler = new C1XCompiler(runtime, target, generator);

        C1XOptions.setOptimizationLevel(3);
        C1XOptions.TraceBytecodeParserLevel = 4;
        C1XOptions.PrintCFGToFile = false;
        C1XOptions.PrintAssembly = false;// true;
        C1XOptions.PrintCompilation = true;
        C1XOptions.GenAssertionCode = true;
        C1XOptions.DetailedAsserts = true;
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
