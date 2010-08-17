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

import com.sun.cri.ci.*;

/**
 * Used to communicate configuration details, runtime offsets, etc. to c1x upon compileMethod.
 *
 * @author Lukas Stadler
 */
public class HotSpotVMConfig implements CompilerObject {

    // os information, register layout, code generation, ...
    public boolean windowsOs;
    public int codeEntryAlignment;

    // offsets, ...
    public int vmPageSize;
    public int stackShadowPages;
    public int hubOffset;
    public int arrayLengthOffset;
    public int[] arrayOffsets;
    public int arrayClassElementOffset;

    // runtime stubs
    public long instanceofStub;
    public long debugStub;
    public long resolveStaticCallStub;

    public void check() {
        assert vmPageSize >= 16;
        assert codeEntryAlignment > 0;
        assert stackShadowPages > 0;
    }

    public int getArrayOffset(CiKind kind) {
        return arrayOffsets[getKindNumber(kind)];
    }

    private int getKindNumber(CiKind kind) {
        if (kind == CiKind.Boolean)
            return 0;
        else if (kind == CiKind.Byte)
            return 1;
        else if (kind == CiKind.Short)
            return 2;
        else if (kind == CiKind.Char)
            return 3;
        else if (kind == CiKind.Int)
            return 4;
        else if (kind == CiKind.Float)
            return 5;
        else if (kind == CiKind.Long)
            return 6;
        else if (kind == CiKind.Double)
            return 7;
        else if (kind == CiKind.Object)
            return 8;
        else
            throw new RuntimeException(kind + " is not a Java kind");
    }

}
