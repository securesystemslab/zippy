package com.sun.hotspot.c1x;

import com.sun.cri.ci.CiConstant;
import com.sun.cri.ci.CiKind;
import com.sun.cri.ri.*;

public class HotSpotType implements RiType {

    // do not query this class object directly, use the VMEntries methods instead!
    // (otherwise this query won't be recorded correctly)
    final Class<?> klass;

    public HotSpotType(Class<?> klass) {
        this.klass = klass;
        assert klass != null;
    }

    @Override
    public int accessFlags() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RiType arrayOf() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RiType componentType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RiType exactType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CiConstant getEncoding(Representation r) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CiKind getRepresentationKind(Representation r) {
        return CiKind.Object;
    }

    @Override
    public boolean hasFinalizableSubclass() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasFinalizer() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasSubclass() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isArrayClass() {
        System.out.println("Checking for array class " + name());
        return Compiler.getVMEntries().RiType_isArrayClass(klass);
    }

    @Override
    public boolean isInitialized() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInstance(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInstanceClass() {
        return Compiler.getVMEntries().RiType_isInstanceClass(klass);
    }

    @Override
    public boolean isInterface() {
        return Compiler.getVMEntries().RiType_isInterface(klass);
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public boolean isSubtypeOf(RiType other) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Class<?> javaClass() {
        return klass;
    }

    @Override
    public CiKind kind() {
        return CiKind.Object;
    }

    @Override
    public String name() {
        return Compiler.getVMEntries().RiType_name(klass);
    }

    @Override
    public RiMethod resolveMethodImpl(RiMethod method) {
        // TODO Auto-generated method stub
        return null;
    }

    public Class<?> klass() {
        return klass;
    }

}
