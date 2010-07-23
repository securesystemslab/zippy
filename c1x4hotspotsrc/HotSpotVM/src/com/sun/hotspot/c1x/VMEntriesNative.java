package com.sun.hotspot.c1x;

import com.sun.cri.ri.RiConstantPool;
import com.sun.cri.ri.RiField;
import com.sun.cri.ri.RiMethod;
import com.sun.cri.ri.RiSignature;
import com.sun.cri.ri.RiType;

public class VMEntriesNative implements VMEntries {

    @Override
    public native byte[] RiMethod_code(Object methodOop);

    @Override
    public native int RiMethod_maxStackSize(Object methodOop);

    @Override
    public native int RiMethod_maxLocals(Object methodOop);

    @Override
    public native RiType RiMethod_holder(Object methodOop);

    @Override
    public native String RiMethod_signature(Object methodOop);

    @Override
    public native String RiMethod_name(Object methodOop);

    @Override
    public native RiType RiSignature_lookupType(String returnType, Object accessingClass);

    @Override
    public native String RiSignature_symbolToString(Object symbolOop);

    @Override
    public native Class< ? > RiType_javaClass(Object klassOop);

    @Override
    public native String RiType_name(Object klassOop);

    @Override
    public native Object RiConstantPool_lookupConstant(Object constantPoolOop, int cpi);

    @Override
    public native RiMethod RiConstantPool_lookupMethod(Object constantPoolOop, int cpi, byte byteCode);

    @Override
    public native RiSignature RiConstantPool_lookupSignature(Object constantPoolOop, int cpi);

    @Override
    public native RiType RiConstantPool_lookupType(Object constantPoolOop, int cpi);

    @Override
    public native RiField RiConstantPool_lookupField(Object constantPoolOop, int cpi);

    @Override
    public native RiType findRiType(Object holderKlassOop);

    @Override
    public native RiConstantPool RiRuntime_getConstantPool(Object klassOop);

    @Override
    public native boolean RiType_isArrayClass(Object klassOop);

    @Override
    public native boolean RiType_isInstanceClass(Object klassOop);

    @Override
    public native boolean RiType_isInterface(Object klassOop);

    @Override
    public native int RiMethod_accessFlags(Object methodOop);

    @Override
    public native void installCode(HotSpotTargetMethod targetMethod);

    @Override
    public native HotSpotVMConfig getConfiguration();
}
