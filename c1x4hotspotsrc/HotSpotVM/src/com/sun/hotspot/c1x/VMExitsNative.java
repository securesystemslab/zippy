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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import com.sun.c1x.*;
import com.sun.c1x.debug.*;
import com.sun.cri.ci.*;
import com.sun.cri.ri.*;
import com.sun.hotspot.c1x.logging.*;
import com.sun.hotspot.c1x.server.*;

/**
 * Exits from the HotSpot VM into Java code.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class VMExitsNative implements VMExits, Remote {

    public static final boolean LogCompiledMethods = false;
    public static boolean compileMethods = true;

    private final Compiler compiler;

    public final HotSpotTypePrimitive TypeBoolean;
    public final HotSpotTypePrimitive TypeChar;
    public final HotSpotTypePrimitive TypeFloat;
    public final HotSpotTypePrimitive TypeDouble;
    public final HotSpotTypePrimitive TypeByte;
    public final HotSpotTypePrimitive TypeShort;
    public final HotSpotTypePrimitive TypeInt;
    public final HotSpotTypePrimitive TypeLong;
    public final HotSpotTypePrimitive TypeVoid;

    public VMExitsNative(Compiler compiler) {
        this.compiler = compiler;

        TypeBoolean = new HotSpotTypePrimitive(compiler, CiKind.Boolean);
        TypeChar = new HotSpotTypePrimitive(compiler, CiKind.Char);
        TypeFloat = new HotSpotTypePrimitive(compiler, CiKind.Float);
        TypeDouble = new HotSpotTypePrimitive(compiler, CiKind.Double);
        TypeByte = new HotSpotTypePrimitive(compiler, CiKind.Byte);
        TypeShort = new HotSpotTypePrimitive(compiler, CiKind.Short);
        TypeInt = new HotSpotTypePrimitive(compiler, CiKind.Int);
        TypeLong = new HotSpotTypePrimitive(compiler, CiKind.Long);
        TypeVoid = new HotSpotTypePrimitive(compiler, CiKind.Void);
    }

    /**
     * Default option configuration for C1X.
     */
    static {
        C1XOptions.setOptimizationLevel(3);
        C1XOptions.OptInlineExcept = false;
        C1XOptions.OptInlineSynchronized = false;
        C1XOptions.DetailedAsserts = false;
        C1XOptions.CommentedAssembly = false;
        C1XOptions.MethodEndBreakpointGuards = 2;
    }

    @Override
    public boolean setOption(String option) {
        if (option.length() == 0) {
            return false;
        }

        Object value = null;
        String fieldName = null;
        String valueString = null;

        char first = option.charAt(0);
        if (first == '+' || first == '-') {
            fieldName = option.substring(1);
            value = (first == '+');
        } else {
            int index = option.indexOf('=');
            if (index == -1) {
                return false;
            }
            fieldName = option.substring(0, index);
            valueString = option.substring(index + 1);
        }

        Field f;
        try {
            f = C1XOptions.class.getField(fieldName);

            if (value == null) {
                if (f.getType() == Float.TYPE) {
                    value = Float.parseFloat(valueString);
                } else if (f.getType() == Double.TYPE) {
                    value = Double.parseDouble(valueString);
                } else if (f.getType() == Integer.TYPE) {
                    value = Integer.parseInt(valueString);
                } else if (f.getType() == Boolean.TYPE) {
                    value = Boolean.parseBoolean(valueString);
                } else if (f.getType() == String.class) {
                    value = valueString;
                }
            }
            if (value != null) {
                f.set(null, value);
                Logger.info("Set option " + fieldName + " to " + value);
            } else {
                Logger.info("Wrong value \"" + valueString + "\" for option " + fieldName);
                return false;
            }
        } catch (SecurityException e) {
            Logger.info("Security exception when setting option " + option);
            return false;
        } catch (NoSuchFieldException e) {
            Logger.info("Could not find option " + fieldName);
            return false;
        } catch (IllegalArgumentException e) {
            Logger.info("Illegal value for option " + option);
            return false;
        } catch (IllegalAccessException e) {
            Logger.info("Illegal access exception when setting option " + option);
            return false;
        }

        return true;
    }

    private static Set<String> compiledMethods = new HashSet<String>();

    @Override
    public void compileMethod(long methodVmId, String name, int entryBCI) throws Throwable {

        if (!compileMethods) {
            return;
        }

        try {
            HotSpotMethodResolved riMethod = new HotSpotMethodResolved(compiler, methodVmId, name);
            CiResult result = compiler.getCompiler().compileMethod(riMethod, -1, null, null);
            if (LogCompiledMethods) {
                String qualifiedName = CiUtil.toJavaName(riMethod.holder()) + "::" + riMethod.name();
                compiledMethods.add(qualifiedName);
            }

            if (result.bailout() != null) {
                StringWriter out = new StringWriter();
                result.bailout().printStackTrace(new PrintWriter(out));
                Throwable cause = result.bailout().getCause();
                TTY.println("Bailout:\n" + out.toString());
                if (cause != null) {
                    Logger.info("Trace for cause: ");
                    for (StackTraceElement e : cause.getStackTrace()) {
                        String current = e.getClassName() + "::" + e.getMethodName();
                        String type = "";
                        if (compiledMethods.contains(current)) {
                            type = "compiled";
                        }
                        Logger.info(String.format("%-10s %3d %s", type, e.getLineNumber(), current));
                    }
                }
                System.out.println("BAILOUT:" + result.bailout().getMessage());
                String s = result.bailout().getMessage();
                if (cause != null) {
                    s = cause.getMessage();
                }
                compiler.getVMEntries().recordBailout(s);
            } else {
                HotSpotTargetMethod.installMethod(compiler, riMethod, result.targetMethod());
            }
        } catch (Throwable t) {
            StringWriter out = new StringWriter();
            t.printStackTrace(new PrintWriter(out));
            TTY.println("Compilation interrupted:\n" + out.toString());
            throw t;
        }
    }

    @Override
    public RiMethod createRiMethodResolved(long vmId, String name) {
        return new HotSpotMethodResolved(compiler, vmId, name);
    }

    @Override
    public RiMethod createRiMethodUnresolved(String name, String signature, RiType holder) {
        return new HotSpotMethodUnresolved(compiler, name, signature, holder);
    }

    @Override
    public RiSignature createRiSignature(String signature) {
        return new HotSpotSignature(compiler, signature);
    }

    @Override
    public RiField createRiField(RiType holder, String name, RiType type, int offset) {
        if (offset != -1) {
            HotSpotTypeResolved resolved = (HotSpotTypeResolved) holder;
            return resolved.createRiField(name, type, offset);
        }
        return new HotSpotField(compiler, holder, name, type, offset);
    }

    @Override
    public RiType createRiType(long vmId, String name) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public RiType createRiTypePrimitive(int basicType) {
        switch (basicType) {
            case 4:
                return TypeBoolean;
            case 5:
                return TypeChar;
            case 6:
                return TypeFloat;
            case 7:
                return TypeDouble;
            case 8:
                return TypeByte;
            case 9:
                return TypeShort;
            case 10:
                return TypeInt;
            case 11:
                return TypeLong;
            case 14:
                return TypeVoid;
            default:
                throw new IllegalArgumentException("Unknown basic type: " + basicType);
        }
    }

    @Override
    public RiType createRiTypeUnresolved(String name) {
        return new HotSpotTypeUnresolved(compiler, name);
    }

    @Override
    public RiConstantPool createRiConstantPool(long vmId) {
        return new HotSpotConstantPool(compiler, vmId);
    }

    @Override
    public CiConstant createCiConstant(CiKind kind, long value) {
        if (kind == CiKind.Long) {
            return CiConstant.forLong(value);
        } else if (kind == CiKind.Int) {
            return CiConstant.forInt((int) value);
        } else if (kind == CiKind.Short) {
            return CiConstant.forShort((short) value);
        } else if (kind == CiKind.Char) {
            return CiConstant.forChar((char) value);
        } else if (kind == CiKind.Byte) {
            return CiConstant.forByte((byte) value);
        } else if (kind == CiKind.Boolean) {
            return (value == 0) ? CiConstant.FALSE : CiConstant.TRUE;
        } else {
            throw new IllegalArgumentException();
        }
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
    public CiConstant createCiConstantObject(Object object) {
        return CiConstant.forObject(object);
    }
}
