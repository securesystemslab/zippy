package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import org.python.ast.datatypes.*;

import com.oracle.truffle.api.codegen.Generic;
import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class ReadLocalNode extends FrameSlotNode {

    public ReadLocalNode(FrameSlot slot) {
        super(slot);
    }

    public ReadLocalNode(ReadLocalNode specialized) {
        this(specialized.slot);
    }

    @Specialization
    public int doInteger(VirtualFrame frame) {
        return frame.getInt(slot);
    }

    @Specialization
    public BigInteger doBigInteger(VirtualFrame frame) {
        return (BigInteger) frame.getObject(slot);
    }

    @Specialization
    public double doDouble(VirtualFrame frame) {
        return frame.getDouble(slot);
    }
    
    @Specialization
    public PComplex doComplex(VirtualFrame frame) {
        return (PComplex) frame.getObject(slot);
    }

    @Specialization
    public boolean doBoolean(VirtualFrame frame) {
        return frame.getBoolean(slot);
    }

    @Specialization
    public String doString(VirtualFrame frame) {
        return (String) frame.getObject(slot);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame) {
        return frame.getObject(slot);
    }

    @Override
    protected FrameSlotNode specialize(Class<?> clazz) {
        return ReadLocalNodeFactory.createSpecialized(this, clazz);
    }
    
    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }

        System.out.println(this);
    }

}
