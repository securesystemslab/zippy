/*
 * Copyright (c) 2011 Sun Microsystems, Inc.  All rights reserved.
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

import com.sun.cri.ci.*;
import com.sun.cri.ri.*;

public interface HotSpotTypeResolved extends RiType {

    public int accessFlags();

    public RiType arrayOf();

    public RiType componentType();

    public RiType uniqueConcreteSubtype();

    public RiType exactType();

    public CiConstant getEncoding(Representation r);

    public CiKind getRepresentationKind(Representation r);

    public boolean hasFinalizableSubclass();

    public boolean hasFinalizer();

    public boolean hasSubclass();

    public boolean isArrayClass();

    public boolean isInitialized();

    public boolean isInstance(Object obj);

    public boolean isInstanceClass();

    public boolean isInterface();

    public boolean isResolved();

    public boolean isSubtypeOf(RiType other);

    public Class<?> javaClass();

    public CiKind kind();

    public RiMethod resolveMethodImpl(RiMethod method);

    public String toString();

    public RiConstantPool constantPool();

    public int instanceSize();

    public RiField createRiField(String name, RiType type, int offset);

}