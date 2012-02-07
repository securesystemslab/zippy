package at.ssw.visualizer.modelimpl.cfg;

import at.ssw.visualizer.model.cfg.StateEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public class StateEntryImpl implements StateEntry {
    private int index;
    private String name;
    private String[] phiOperands;
    private String operand;

    public StateEntryImpl(int index, String name, String[] phiOperands, String operand) {
        this.index = index;
        this.name = name;
        this.phiOperands = phiOperands;
        this.operand = operand;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean hasPhiOperands() {
        return phiOperands != null;
    }

    public List<String> getPhiOperands() {
        return Collections.unmodifiableList(Arrays.asList(phiOperands));
    }

    public String getOperand() {
        return operand;
    }
}
