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

import java.io.*;

import com.sun.cri.ci.*;
import com.sun.cri.ri.*;
import com.sun.hotspot.c1x.logging.*;

/**
 * Exits from the HotSpot VM into Java code.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class VMExitsNative implements VMExits {

    @Override
    public void compileMethod(long methodVmId, String name, int entry_bci) {
        try {
            Logger.info("compiling " + name + " (0x" + Long.toHexString(methodVmId) + ")");
            Compiler compiler = Compiler.getInstance();
            HotSpotMethodResolved riMethod = new HotSpotMethodResolved(methodVmId, name);
            CiResult result = compiler.getCompiler().compileMethod(riMethod, null);

            if (result.bailout() != null) {
                StringWriter out = new StringWriter();
                result.bailout().printStackTrace(new PrintWriter(out));
                Logger.info("Bailout:\n" + out.toString());
            } else {
                Logger.log("Compilation result: " + result.targetMethod());
                HotSpotTargetMethod.installMethod(riMethod, result.targetMethod());
            }
        } catch (Throwable t) {
            StringWriter out = new StringWriter();
            t.printStackTrace(new PrintWriter(out));
            Logger.info("Compilation interrupted:\n" + out.toString());
        }
    }

    @Override
    public RiMethod createRiMethodResolved(long vmId, String name) {
        return new HotSpotMethodResolved(vmId, name);
    }

    @Override
    public RiMethod createRiMethodUnresolved(String name, String signature, RiType holder) {
        return new HotSpotMethodUnresolved(name, signature, holder);
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
    public RiType createRiType(long vmId, String name) {
        throw new RuntimeException("not implemented");
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
    public RiType createRiTypeUnresolved(String name, long accessingClassVmId) {
        return new HotSpotTypeUnresolved(name, accessingClassVmId);
    }

    @Override
    public RiConstantPool createRiConstantPool(long vmId) {
        return new HotSpotConstantPool(vmId);
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
    public CiConstant createCiConstantObject(long vmId) {
        return CiConstant.forObject(vmId);
    }
}
