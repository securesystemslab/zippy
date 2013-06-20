package org.python.core.truffle;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeListener;

public class EnvironmentFrameSlot implements FrameSlot {

    private final FrameSlot slot;

    private final int level;

    public EnvironmentFrameSlot(FrameSlot slot, int level) {
        this.slot = slot;
        this.level = level;
    }

    public static EnvironmentFrameSlot pack(FrameSlot slot, int level) {
        return new EnvironmentFrameSlot(slot, level);
    }

    public FrameSlot unpack() {
        return slot;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public Object getIdentifier() {
        return slot.getIdentifier();
    }

    @Override
    public int getIndex() {
        return slot.getIndex();
    }

    @Override
    public Class<?> getType() {
        return slot.getType();
    }

    @Override
    public void setType(Class<?> type) {
        slot.setType(type);
    }

    @Override
    public void registerOneShotTypeListener(FrameSlotTypeListener listener) {
        slot.registerOneShotTypeListener(listener);
    }

    @Override
    public String toString() {
        return slot.toString();
    }
}
