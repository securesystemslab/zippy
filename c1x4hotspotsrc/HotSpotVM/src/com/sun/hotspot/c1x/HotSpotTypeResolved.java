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

import java.lang.reflect.*;

import com.sun.cri.ci.*;
import com.sun.cri.ri.*;
import com.sun.hotspot.c1x.logging.*;

/**
 * Implementation of RiType for resolved non-primitive HotSpot classes.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class HotSpotTypeResolved implements HotSpotType {

    private final long vmId;
    private final long javaMirrorVmId;
    private final String name;
    private final int accessFlags;
    private final boolean hasFinalizer;
    private final boolean hasSubclass;
    private final boolean hasFinalizableSubclass;
    private final boolean isInitialized;
    private final boolean isArrayClass;
    private final boolean isInstanceClass;
    private final boolean isInterface;
    private final int instanceSize;
    private final RiType componentType;

    public HotSpotTypeResolved(long vmId, long javaMirrorVmId, String name, int accessFlags, boolean hasFinalizer, boolean hasSubclass, boolean hasFinalizableSubclass, boolean isInitialized,
                    boolean isArrayClass, boolean isInstanceClass, boolean isInterface, int instanceSize, RiType componentType) {
        this.vmId = vmId;
        this.javaMirrorVmId = javaMirrorVmId;
        this.name = name;
        this.accessFlags = accessFlags;
        this.hasFinalizer = hasFinalizer;
        this.hasSubclass = hasSubclass;
        this.hasFinalizableSubclass = hasFinalizableSubclass;
        this.isInitialized = isInitialized;
        this.isArrayClass = isArrayClass;
        this.isInstanceClass = isInstanceClass;
        this.isInterface = isInterface;
        this.instanceSize = instanceSize;
        this.componentType = componentType;
    }

    @Override
    public int accessFlags() {
        return accessFlags;
    }

    @Override
    public RiType arrayOf() {
        Logger.log("arrayOf " + name);
        return null;
        //return Compiler.getVMEntries().RiType_arrayOf(vmId);
    }

    @Override
    public RiType componentType() {
        Logger.log("componentType " + name + " isarray: " + isArrayClass);
        return Compiler.getVMEntries().RiType_componentType(vmId);
    }

    @Override
    public RiType exactType() {
        // TODO is this correct? what's this exactType good for?
        if (Modifier.isFinal(accessFlags)) {
            return this;
        }
        return null;
    }

    @Override
    public CiConstant getEncoding(Representation r) {
        switch (r) {
            case JavaClass:
                return CiConstant.forObject((Long) javaMirrorVmId);
            case ObjectHub:
                return CiConstant.forObject(this);
            case StaticFields:
                return CiConstant.forObject(this);
            case TypeInfo:
                return CiConstant.forObject(this);
            default:
                return null;
        }
    }

    @Override
    public CiKind getRepresentationKind(Representation r) {
        return CiKind.Object;
    }

    @Override
    public boolean hasFinalizableSubclass() {
        return hasFinalizableSubclass;
    }

    @Override
    public boolean hasFinalizer() {
        return hasFinalizer;
    }

    @Override
    public boolean hasSubclass() {
        return hasSubclass;
    }

    @Override
    public boolean isArrayClass() {
        return isArrayClass;
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public boolean isInstance(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInstanceClass() {
        return isInstanceClass;
    }

    @Override
    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public boolean isSubtypeOf(RiType other) {
        if (other instanceof HotSpotTypeResolved) {
            return Compiler.getVMEntries().RiType_isSubtypeOf(vmId, other);
        }
        // No resolved type is a subtype of an unresolved type.
        return false;
    }

    @Override
    public Class<?> javaClass() {
        return null;
    }

    @Override
    public CiKind kind() {
        return CiKind.Object;
    }

    @Override
    public String name() {
        return "L" + name + ";";
    }

    @Override
    public String simpleName() {
        return name;
    }

    @Override
    public RiMethod resolveMethodImpl(RiMethod method) {
        assert method instanceof HotSpotMethod;
        return Compiler.getVMEntries().RiType_resolveMethodImpl(vmId, method.name(), method.signature().asString());
    }

    @Override
    public String toString() {
        return "HotSpotType<" + name + ", resolved>";
    }

    public RiConstantPool constantPool() {
        return Compiler.getVMEntries().RiType_constantPool(vmId);
    }

    public long getVmId() {
        return vmId;
    }

    public int instanceSize() {
        return instanceSize;
    }

}
