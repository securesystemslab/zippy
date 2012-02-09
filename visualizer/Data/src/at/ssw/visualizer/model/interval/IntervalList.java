package at.ssw.visualizer.model.interval;

import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public interface IntervalList {
    public List<Interval> getIntervals();

    public ControlFlowGraph getControlFlowGraph();

    public int getNumLIROperations();
}
