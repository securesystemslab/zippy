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
import com.sun.cri.ci.*;
import com.sun.cri.ri.*;
import com.sun.hotspot.c1x.logging.*;

/**
 * Exits from the HotSpot VM into Java code.
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class VMExitsNative implements VMExits {

    public static final boolean LogCompiledMethods = false;
    public static boolean compileMethods = true;

    /**
     * Default option configuration for C1X.
     */
    static {
        C1XOptions.setOptimizationLevel(3);
        C1XOptions.OptInlineExcept = false;
        C1XOptions.OptInlineSynchronized = false;
        C1XOptions.UseDeopt = false;
        C1XOptions.IRChecking = false;
        C1XOptions.DetailedAsserts = false;
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
            Compiler compiler = Compiler.getInstance();
            HotSpotMethodResolved riMethod = new HotSpotMethodResolved(methodVmId, name);
            CiResult result = compiler.getCompiler().compileMethod(riMethod, -1, null);
            if (LogCompiledMethods) {
                String qualifiedName = CiUtil.toJavaName(riMethod.holder()) + "::" + riMethod.name();
                compiledMethods.add(qualifiedName);
            }

            if (result.bailout() != null) {
                StringWriter out = new StringWriter();
                result.bailout().printStackTrace(new PrintWriter(out));
                Throwable cause = result.bailout().getCause();
                Logger.info("Bailout:\n" + out.toString());
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
                Compiler.getVMEntries().recordBailout(result.bailout().getMessage());
            } else {
                HotSpotTargetMethod.installMethod(riMethod, result.targetMethod());
            }
        } catch (Throwable t) {
            StringWriter out = new StringWriter();
            t.printStackTrace(new PrintWriter(out));
            Logger.info("Compilation interrupted:\n" + out.toString());
            throw t;
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
    public CiConstant createCiConstantObject(Object object) {
        return CiConstant.forObject(object);
    }
}
