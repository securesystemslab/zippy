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
import com.sun.cri.ri.*;

/**
 * Implementation of RiType for resolved non-primitive HotSpot classes.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class HotSpotTypeResolved implements HotSpotType {

    private final long vmId;

    private final String name;

    // cached values
    private Boolean isArrayClass;
    private Boolean isInstanceClass;
    private Boolean isInterface;

    public HotSpotTypeResolved(long vmId, String name) {
        this.vmId = vmId;
        this.name = name;
    }

    @Override
    public int accessFlags() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RiType arrayOf() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RiType componentType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RiType exactType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CiConstant getEncoding(Representation r) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CiKind getRepresentationKind(Representation r) {
        return CiKind.Object;
    }

    @Override
    public boolean hasFinalizableSubclass() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasFinalizer() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasSubclass() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isArrayClass() {
        if (isArrayClass == null)
            isArrayClass = Compiler.getVMEntries().RiType_isArrayClass(vmId);
        return isArrayClass;
    }

    @Override
    public boolean isInitialized() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInstance(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInstanceClass() {
        if (isInstanceClass == null)
            isInstanceClass = Compiler.getVMEntries().RiType_isInstanceClass(vmId);
        return isInstanceClass;
    }

    @Override
    public boolean isInterface() {
        if (isInterface == null)
            isInterface = Compiler.getVMEntries().RiType_isInterface(vmId);
        return isInterface;
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public boolean isSubtypeOf(RiType other) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Class<?> javaClass() {
        throw new RuntimeException("javaClass not implemented");
    }

    @Override
    public CiKind kind() {
        return CiKind.Object;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public RiMethod resolveMethodImpl(RiMethod method) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return "HotSpotType<" + name + ">";
    }

    public RiConstantPool constantPool() {
        return Compiler.getVMEntries().RiType_constantPool(vmId);
    }

    public long getVmId() {
        return vmId;
    }

}
