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

import java.lang.reflect.*;

import com.sun.cri.ci.*;
import com.sun.cri.ri.*;

/**
 * Exits from the HotSpot VM into Java code.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class VMExitsNative implements VMExits {

    @Override
    public void compileMethod(Method method, int entry_bci) {
        try {
            Compiler compiler = Compiler.getInstance();
            HotSpotMethod riMethod = new HotSpotMethod(method);
            CiResult result = compiler.getCompiler().compileMethod(riMethod, null);

            if (result.bailout() != null) {
                System.out.println("Bailout:");
                result.bailout().printStackTrace();
            } else {
                Logger.log("Compilation result: " + result.targetMethod());
                HotSpotTargetMethod.installCode(compiler.getConfig(), riMethod, result.targetMethod());
            }
        } catch (Throwable t) {
            System.out.println("Compilation interrupted:");
            t.printStackTrace();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }

    @Override
    public RiMethod createRiMethod(Method method) {
        RiMethod m = new HotSpotMethod(method);
        return m;
    }

    @Override
    public RiSignature createRiSignature(String signature) {
        return new HotSpotSignature(signature);
    }

    @Override
    public RiField createRiField(RiType holder, String name, RiType type, int offset) {
        return new HotSpotField(holder, name, type, offset);
    }

    @Override
    public RiType createRiType(Class<?> klassOop) {
        return new HotSpotType(klassOop);
    }

    @Override
    public RiType createRiTypePrimitive(int basicType) {
        CiKind kind = null;
        switch (basicType) {
            case 4:
                kind = CiKind.Boolean;
                break;
            case 5:
                kind = CiKind.Char;
                break;
            case 6:
                kind = CiKind.Float;
                break;
            case 7:
                kind = CiKind.Double;
                break;
            case 8:
                kind = CiKind.Byte;
                break;
            case 9:
                kind = CiKind.Short;
                break;
            case 10:
                kind = CiKind.Int;
                break;
            case 11:
                kind = CiKind.Long;
                break;
            case 14:
                kind = CiKind.Void;
                break;
            default:
                throw new IllegalArgumentException("Unknown basic type: " + basicType);
        }
        return new HotSpotTypePrimitive(kind);
    }

    @Override
    public RiType createRiTypeUnresolved(String name, Class<?> accessingKlassOop) {
        return new HotSpotTypeUnresolved(name);
    }

    @Override
    public RiConstantPool createRiConstantPool(Class<?> constantPoolOop) {
        return new HotSpotConstantPool(constantPoolOop);
    }

    @Override
    public CiConstant createCiConstantInt(int value) {
        return CiConstant.forInt(value);
    }

    @Override
    public CiConstant createCiConstantLong(long value) {
        return CiConstant.forLong(value);
    }

    @Override
    public CiConstant createCiConstantFloat(float value) {
        return CiConstant.forFloat(value);
    }

    @Override
    public CiConstant createCiConstantDouble(double value) {
        return CiConstant.forDouble(value);
    }

    @Override
    public CiConstant createCiConstantObject(Object value) {
        return CiConstant.forObject(value);
    }
}
