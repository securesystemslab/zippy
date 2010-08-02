package com.sun.hotspot.c1x;

import java.lang.reflect.*;

import com.sun.cri.ri.*;

public interface VMEntries {

    public abstract byte[] RiMethod_code(Method methodOop);

    public abstract int RiMethod_maxStackSize(Method methodOop);

    public abstract int RiMethod_maxLocals(Method methodOop);

    public abstract RiType RiMethod_holder(Method methodOop);

    public abstract String RiMethod_signature(Method methodOop);

    public abstract String RiMethod_name(Method methodOop);

    public abstract int RiMethod_accessFlags(Method methodOop);

    public abstract RiType RiSignature_lookupType(String returnType, Class<?> accessingClass);

    public abstract Object RiConstantPool_lookupConstant(Class<?> constantPoolHolder, int cpi);

    public abstract RiMethod RiConstantPool_lookupMethod(Class<?> constantPoolHolder, int cpi, byte byteCode);

    public abstract RiSignature RiConstantPool_lookupSignature(Class<?> constantPoolHolder, int cpi);

    public abstract RiType RiConstantPool_lookupType(Class<?> constantPoolHolder, int cpi);

    public abstract RiField RiConstantPool_lookupField(Class<?> constantPoolHolder, int cpi);

    public abstract String RiType_name(Class<?> klassOop);

    public abstract boolean RiType_isArrayClass(Class<?> klassOop);

    public abstract boolean RiType_isInstanceClass(Class<?> klassOop);

    public abstract boolean RiType_isInterface(Class<?> klassOop);

    public abstract void installCode(HotSpotTargetMethod targetMethod);

    public abstract HotSpotVMConfig getConfiguration();

}