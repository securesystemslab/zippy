package com.sun.hotspot.c1x;

import java.lang.reflect.*;

import com.sun.cri.ri.*;

public class VMEntriesNative implements VMEntries {

    @Override
    public native byte[] RiMethod_code(Method methodOop);

    @Override
    public native int RiMethod_maxStackSize(Method methodOop);

    @Override
    public native int RiMethod_maxLocals(Method methodOop);

    @Override
    public native RiType RiMethod_holder(Method methodOop);

    @Override
    public native String RiMethod_signature(Method methodOop);

    @Override
    public native String RiMethod_name(Method methodOop);

    @Override
    public native int RiMethod_accessFlags(Method methodOop);

    @Override
    public native RiType RiSignature_lookupType(String returnType, Class<?> accessingClass);

    @Override
    public native Object RiConstantPool_lookupConstant(Class<?> constantPoolOop, int cpi);

    @Override
    public native RiMethod RiConstantPool_lookupMethod(Class<?> constantPoolOop, int cpi, byte byteCode);

    @Override
    public native RiSignature RiConstantPool_lookupSignature(Class<?> constantPoolOop, int cpi);

    @Override
    public native RiType RiConstantPool_lookupType(Class<?> constantPoolOop, int cpi);

    @Override
    public native RiField RiConstantPool_lookupField(Class<?> constantPoolOop, int cpi);

    @Override
    public native String RiType_name(Class<?> klassOop);

    @Override
    public native boolean RiType_isArrayClass(Class<?> klassOop);

    @Override
    public native boolean RiType_isInstanceClass(Class<?> klassOop);

    @Override
    public native boolean RiType_isInterface(Class<?> klassOop);

    @Override
    public native void installCode(HotSpotTargetMethod targetMethod);

    @Override
    public native HotSpotVMConfig getConfiguration();
}
