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

import com.sun.cri.ci.*;
import com.sun.cri.ri.*;

/**
 * Entries into the HotSpot VM from Java code.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public interface VMEntries {

    public byte[] RiMethod_code(long vmId);

    public int RiMethod_maxStackSize(long vmId);

    public int RiMethod_maxLocals(long vmId);

    public RiType RiMethod_holder(long vmId);

    public String RiMethod_signature(long vmId);

    public int RiMethod_accessFlags(long vmId);

    public RiType RiSignature_lookupType(String returnType, long accessingClassVmId);

    public CiConstant RiConstantPool_lookupConstant(long vmId, int cpi);

    public RiMethod RiConstantPool_lookupMethod(long vmId, int cpi, byte byteCode);

    public RiSignature RiConstantPool_lookupSignature(long vmId, int cpi);

    public RiType RiConstantPool_lookupType(long vmId, int cpi);

    public RiField RiConstantPool_lookupField(long vmId, int cpi);

    public boolean RiType_isArrayClass(long vmId);

    public boolean RiType_isInstanceClass(long vmId);

    public boolean RiType_isInterface(long vmId);

    public RiConstantPool RiType_constantPool(long vmId);

    public void installMethod(HotSpotTargetMethod targetMethod);

    public long installStub(HotSpotTargetMethod targetMethod);

    public HotSpotVMConfig getConfiguration();

}