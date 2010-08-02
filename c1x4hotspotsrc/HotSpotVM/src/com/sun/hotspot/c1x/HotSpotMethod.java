package com.sun.hotspot.c1x;

import java.lang.reflect.*;

import com.sun.cri.ri.RiExceptionHandler;
import com.sun.cri.ri.RiMethod;
import com.sun.cri.ri.RiMethodProfile;
import com.sun.cri.ri.RiSignature;
import com.sun.cri.ri.RiType;

public class HotSpotMethod implements RiMethod {

    Method method;
    private byte[] code;
    private int maxLocals = -1;
    private int maxStackSize = -1;
    private RiSignature signature;

    public HotSpotMethod(Method method) {
        this.method = method;
    }

    @Override
    public int accessFlags() {
        return Compiler.getVMEntries().RiMethod_accessFlags(method);
    }

    @Override
    public boolean canBeStaticallyBound() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public byte[] code() {
        if (code == null) {
            code = Compiler.getVMEntries().RiMethod_code(method);
        }
        return code;
    }

    @Override
    public RiExceptionHandler[] exceptionHandlers() {
        // TODO: Add support for exception handlers
        return new RiExceptionHandler[0];
    }

    @Override
    public boolean hasBalancedMonitors() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RiType holder() {
        return Compiler.getVMEntries().RiMethod_holder(method);
    }

    @Override
    public boolean isClassInitializer() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isConstructor() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLeafMethod() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOverridden() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isResolved() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String jniSymbol() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object liveness(int bci) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int maxLocals() {
        if (maxLocals == -1)
            maxLocals = Compiler.getVMEntries().RiMethod_maxLocals(method);
        return maxLocals;
    }

    @Override
    public int maxStackSize() {
        if (maxStackSize == -1)
            maxStackSize = Compiler.getVMEntries().RiMethod_maxStackSize(method);
        return maxStackSize;
    }

    @Override
    public RiMethodProfile methodData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String name() {
        return Compiler.getVMEntries().RiMethod_name(method);
    }

    @Override
    public RiSignature signature() {
        if (signature == null)
            signature = new HotSpotSignature(Compiler.getVMEntries().RiMethod_signature(method));
        return signature;
    }

}
