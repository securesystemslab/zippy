package org.python.ast.nodes.expressions;

import com.oracle.truffle.api.codegen.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class ReadEnvironmentNode extends FrameSlotNode {

    final int level;

    public ReadEnvironmentNode(FrameSlot slot, int level) {
        super(slot);
        this.level = level;
    }

    public ReadEnvironmentNode(ReadEnvironmentNode specialized) {
        this(specialized.slot, specialized.level);
    }

    @Specialization
    public Object doGeneric(VirtualFrame frame) {
        VirtualFrame parent = getParentFrame(frame, level);
        return parent.getObject(slot);
    }

    VirtualFrame getParentFrame(VirtualFrame frame, int level) {
        if (level == 0) {
            return frame;
        } else {
            return getParentFrame((VirtualFrame) frame.getCaller().unpack(), level - 1);
        }
    }

    @Override
    protected FrameSlotNode specialize(Class<?> clazz) {
        return this;
    }

}
