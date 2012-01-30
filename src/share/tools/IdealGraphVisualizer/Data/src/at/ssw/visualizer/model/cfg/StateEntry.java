package at.ssw.visualizer.model.cfg;

import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public interface StateEntry {
    public int getIndex();

    public String getName();

    public boolean hasPhiOperands();

    public List<String> getPhiOperands();

    public String getOperand();
}
