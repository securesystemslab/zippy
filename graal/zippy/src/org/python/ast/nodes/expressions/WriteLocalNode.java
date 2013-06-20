package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;
import org.python.core.truffle.*;

import com.oracle.truffle.api.codegen.Generic;
import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.codegen.SpecializationListener;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class WriteLocalNode extends FrameSlotNode {

    @Child
    protected TypedNode rightNode;

    public WriteLocalNode(FrameSlot slot, TypedNode right) {
        super(slot);
        this.rightNode = adoptChild(right);
    }

    public WriteLocalNode(WriteLocalNode specialized) {
        this(specialized.slot, specialized.rightNode);
        copyNext(specialized);
    }

    public void patchValue(TypedNode right) {
        rightNode = adoptChild(right);
    }

    /*
     * As a LeftHandSideNode
     */
    public void doLeftHandSide(VirtualFrame frame, int value) {
        write(frame, value);
    }

    public void doLeftHandSide(VirtualFrame frame, BigInteger value) {
        write(frame, value);
    }

    public void doLeftHandSide(VirtualFrame frame, double value) {
        write(frame, value);
    }

    public void doLeftHandSide(VirtualFrame frame, String value) {
        write(frame, value);
    }

    @Override
    public void doLeftHandSide(VirtualFrame frame, Object value) {
        writeGeneric(frame, value);
    }

    /*
     * As a right hand side expression
     */
    @Specialization
    public int write(VirtualFrame frame, int right) {
        frame.setInt(slot, right);
        return right;
    }

    @Specialization
    public BigInteger write(VirtualFrame frame, BigInteger right) {
        frame.setObject(slot, right);
        return right;
    }

    @Specialization
    public double write(VirtualFrame frame, double right) {
        frame.setDouble(slot, right);
        return right;
    }

    @Specialization
    public PComplex write(VirtualFrame frame, PComplex right) {
        //frame.setObject(slot, right);
        frame.setObject(slot, new PComplex(right));
        return right;
    }

    @Specialization
    public boolean write(VirtualFrame frame, boolean right) {
        frame.setBoolean(slot, right);
        return right;
    }

    @Specialization
    public String write(VirtualFrame frame, String right) {
        frame.setObject(slot, right);
        return right;
    }

    @Generic(useSpecializations = false)
    public Object writeGeneric(VirtualFrame frame, Object right) {
        frame.setObject(slot, right);
        return right;
    }

    @SpecializationListener
    protected void onSpecialize(VirtualFrame frame, Object value) {
        /*
         * This is a dirty hack. a write local in fasta is initialized to None.
         */
        if (value == null) {
            return;
        }

        Class<?> target = PythonTypes.getRelaxedTyped(value);
        slot.setType(target);
        frame.updateToLatestVersion();
    }

    @Override
    protected FrameSlotNode specialize(Class<?> clazz) {
        return WriteLocalNodeFactory.createSpecialized(this, clazz);
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        rightNode.visualize(level);
    }

}
