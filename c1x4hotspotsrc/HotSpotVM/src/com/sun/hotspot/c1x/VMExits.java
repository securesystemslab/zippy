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
 * Exits from the HotSpot VM into Java code.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public interface VMExits {

    public abstract void compileMethod(long methodVmId, String name, int entry_bci);

    public abstract RiMethod createRiMethodResolved(long vmId, String name);

    public abstract RiMethod createRiMethodUnresolved(String name, String signature, RiType holder);

    public abstract RiSignature createRiSignature(String signature);

    public abstract RiField createRiField(RiType holder, String name, RiType type, int offset);

    public abstract RiType createRiType(long vmId, String name);

    public abstract RiType createRiTypePrimitive(int basicType);

    public abstract RiType createRiTypeUnresolved(String name, long accessingClassVmId);

    public abstract RiConstantPool createRiConstantPool(long vmId);

    public abstract CiConstant createCiConstantInt(int value);

    public abstract CiConstant createCiConstantLong(long value);

    public abstract CiConstant createCiConstantFloat(float value);

    public abstract CiConstant createCiConstantDouble(double value);

    public abstract CiConstant createCiConstantObject(long vmId);

}