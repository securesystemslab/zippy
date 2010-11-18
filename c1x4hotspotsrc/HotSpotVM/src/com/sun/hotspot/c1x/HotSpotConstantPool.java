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

import com.sun.cri.ri.*;

/**
 * Implementation of RiConstantPool for HotSpot.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class HotSpotConstantPool implements RiConstantPool, CompilerObject {

    private final long vmId;

    public HotSpotConstantPool(long vmId) {
        this.vmId = vmId;
    }

    @Override
    public Object lookupConstant(int cpi) {
        return Compiler.getVMEntries().RiConstantPool_lookupConstant(vmId, cpi);
    }

    @Override
    public RiMethod lookupMethod(int cpi, int byteCode) {
        return Compiler.getVMEntries().RiConstantPool_lookupMethod(vmId, cpi, (byte) byteCode);
    }

    @Override
    public RiSignature lookupSignature(int cpi) {
        return Compiler.getVMEntries().RiConstantPool_lookupSignature(vmId, cpi);
    }

    @Override
    public RiType lookupType(int cpi, int opcode) {
        return Compiler.getVMEntries().RiConstantPool_lookupType(vmId, cpi);
    }

    @Override
    public RiField lookupField(int cpi, int opcode) {
        return Compiler.getVMEntries().RiConstantPool_lookupField(vmId, cpi, (byte) opcode);
    }

}
