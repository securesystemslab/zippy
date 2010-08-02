package com.sun.hotspot.c1x;

import java.lang.reflect.*;

import com.sun.cri.ci.*;
import com.sun.cri.ri.*;

public interface VMExits {

    public abstract void compileMethod(Method method, int entry_bci);

    public abstract RiMethod createRiMethod(Method methodOop);

    public abstract RiSignature createRiSignature(String signature);

    public abstract RiField createRiField(RiType holder, String name, RiType type, int offset);

    public abstract RiType createRiType(Class<?> klassOop);

    public abstract RiType createRiTypePrimitive(int basicType);

    public abstract RiType createRiTypeUnresolved(String name, Class<?> accessingKlassOop);

    public abstract RiConstantPool createRiConstantPool(Class<?> constantPoolOop);

    public abstract CiConstant createCiConstantInt(int value);

    public abstract CiConstant createCiConstantLong(long value);

    public abstract CiConstant createCiConstantFloat(float value);

    public abstract CiConstant createCiConstantDouble(double value);

    public abstract CiConstant createCiConstantObject(Object value);

}