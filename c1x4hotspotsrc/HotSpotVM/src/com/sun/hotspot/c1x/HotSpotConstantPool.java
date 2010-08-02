package com.sun.hotspot.c1x;

import com.sun.cri.ci.CiConstant;
import com.sun.cri.ri.RiConstantPool;
import com.sun.cri.ri.RiField;
import com.sun.cri.ri.RiMethod;
import com.sun.cri.ri.RiSignature;
import com.sun.cri.ri.RiType;

public class HotSpotConstantPool implements RiConstantPool {

    private final Class<?> constantPoolHolder;

    public HotSpotConstantPool(Class<?> o) {
        this.constantPoolHolder = o;
    }

    @Override
    public CiConstant encoding() {
        // TODO: Check if this is correct.
        return CiConstant.forObject(constantPoolHolder);
    }

    @Override
    public Object lookupConstant(int cpi) {
        return Compiler.getVMEntries().RiConstantPool_lookupConstant(constantPoolHolder, cpi);
    }

    @Override
    public RiMethod lookupMethod(int cpi, int byteCode) {
        return Compiler.getVMEntries().RiConstantPool_lookupMethod(constantPoolHolder, cpi, (byte) byteCode);
    }

    @Override
    public RiSignature lookupSignature(int cpi) {
        return Compiler.getVMEntries().RiConstantPool_lookupSignature(constantPoolHolder, cpi);
    }

    @Override
    public RiType lookupType(int cpi, int opcode) {
        return Compiler.getVMEntries().RiConstantPool_lookupType(constantPoolHolder, cpi);
    }

    @Override
    public RiField lookupField(int cpi, int opcode) {
        return Compiler.getVMEntries().RiConstantPool_lookupField(constantPoolHolder, cpi);
    }

}
