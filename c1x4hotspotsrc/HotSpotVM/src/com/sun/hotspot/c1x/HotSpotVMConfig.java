/*
 * Copyright (c) 2010 Sun Microsystems, Inc. All rights reserved.
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

/**
 * Used to communicate configuration details, runtime offsets, etc. to c1x upon compileMethod.
 *
 * @author Lukas Stadler
 */
public class HotSpotVMConfig {

    // os information, register layout, code generation, ...
    public boolean windowsOs;
    public int codeEntryAlignment;

    // offsets, ...
    public int vmPageSize;
    public int stackShadowPages;
    public int hubOffset;

    // runtime stubs
    public long instanceofStub;
    public long debugStub;

    public void check() {
        assert vmPageSize >= 16;
        assert codeEntryAlignment > 0;
        assert stackShadowPages > 0;
        assert instanceofStub != 0;
        assert debugStub != 0;
        System.out.println("Config::debugStub = " + Long.toHexString(debugStub));
    }

}
