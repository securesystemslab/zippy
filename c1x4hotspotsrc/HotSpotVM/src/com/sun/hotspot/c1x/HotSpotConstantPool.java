package com.sun.hotspot.c1x;

import com.sun.cri.ci.CiConstant;
import com.sun.cri.ri.RiConstantPool;
import com.sun.cri.ri.RiField;
import com.sun.cri.ri.RiMethod;
import com.sun.cri.ri.RiSignature;
import com.sun.cri.ri.RiType;

public class HotSpotConstantPool implements RiConstantPool {

    private final Object constantPoolOop;

    public HotSpotConstantPool(Object o) {
        this.constantPoolOop = o;
    }

    @Override
    public CiConstant encoding() {
        // TODO: Check if this is correct.
        return CiConstant.forObject(constantPoolOop);
    }

    @Override
    public Object lookupConstant(int cpi) {
        return Compiler.getVMEntries().RiConstantPool_lookupConstant(constantPoolOop, cpi);
    }

    @Override
    public RiMethod lookupMethod(int cpi, int byteCode) {
        return Compiler.getVMEntries().RiConstantPool_lookupMethod(constantPoolOop, cpi, (byte) byteCode);
    }

    @Override
    public RiSignature lookupSignature(int cpi) {
        return Compiler.getVMEntries().RiConstantPool_lookupSignature(constantPoolOop, cpi);
    }

    @Override
    public RiType lookupType(int cpi, int opcode) {
        return Compiler.getVMEntries().RiConstantPool_lookupType(constantPoolOop, cpi);
    }

    @Override
    public RiField lookupField(int cpi, int opcode) {
        return Compiler.getVMEntries().RiConstantPool_lookupField(constantPoolOop, cpi);
    }

}
