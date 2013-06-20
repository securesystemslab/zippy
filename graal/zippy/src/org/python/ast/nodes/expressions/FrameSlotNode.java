package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeListener;

/**
 * Copied from com.oracle.truffle.sl.nodes.FrameSlotNode
 * 
 * @author zwei
 * 
 */
public abstract class FrameSlotNode extends TypedNode implements FrameSlotTypeListener {

    protected final FrameSlot slot;

    public FrameSlotNode(FrameSlot slot) {
        this.slot = slot;
        slot.registerOneShotTypeListener(this);
    }

    // use with caution
    public FrameSlot getSlot() {
        return slot;
    }

    @Override
    public void typeChanged(FrameSlot changedSlot, Class<?> oldType) {
        if (getParent() != null) {
            replace(specialize(changedSlot.getType()));
        }
    }

    protected abstract FrameSlotNode specialize(Class<?> clazz);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + slot + ")";
    }

}
