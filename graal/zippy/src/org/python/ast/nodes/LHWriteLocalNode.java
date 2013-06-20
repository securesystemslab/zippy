package org.python.ast.nodes;

import org.python.ast.nodes.expressions.FrameSlotNode;
import org.python.ast.nodes.expressions.WriteLocalNode;
import org.python.ast.nodes.expressions.WriteLocalNodeFactory;
import org.python.core.truffle.*;

import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.codegen.SpecializationListener;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class LHWriteLocalNode extends FrameSlotNode {

    private Object value;

    public LHWriteLocalNode(FrameSlot slot) {
        super(slot);
    }

    public LHWriteLocalNode(LHWriteLocalNode specialized) {
        this(specialized.slot);
    }

    public void doLeftHandSide(VirtualFrame frame, Object value) {
        this.value = value;
        onSpecialize(frame);
        frame.setObject(slot, value);
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        return null;
    }

    @SpecializationListener
    protected void onSpecialize(VirtualFrame frame) {
        Class<?> target = PythonTypes.getRelaxedTyped(value);
        slot.setType(target);
        frame.updateToLatestVersion();
    }

    @Override
    protected FrameSlotNode specialize(Class<?> clazz) {
        WriteLocalNode dummy = WriteLocalNodeFactory.create(slot, DUMMY);
        return WriteLocalNodeFactory.createSpecialized(dummy, clazz);
    }

}
