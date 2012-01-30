package at.ssw.visualizer.modelimpl.interval;

import at.ssw.visualizer.model.cfg.BasicBlock;
import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import at.ssw.visualizer.model.interval.Interval;
import at.ssw.visualizer.model.interval.IntervalList;
import com.sun.hotspot.igv.data.AbstractFolderElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public class IntervalListImpl extends AbstractFolderElement implements IntervalList {
    private Interval[] intervals;
    private ControlFlowGraph controlFlowGraph;
    private int numLIROperations;

    public IntervalListImpl(String shortName, String name, IntervalImpl[] intervals, ControlFlowGraph controlFlowGraph) {
        super(shortName, name);
        this.intervals = intervals;
        this.controlFlowGraph = controlFlowGraph;

        for (IntervalImpl interval : intervals) {
            interval.setParent(this);
            numLIROperations = Math.max(numLIROperations, interval.getTo());
        }
        for (BasicBlock basicBlock : controlFlowGraph.getBasicBlocks()) {
            numLIROperations = Math.max(numLIROperations, basicBlock.getLastLirId() + 2);
        }
    }


    public List<Interval> getIntervals() {
        return Collections.unmodifiableList(Arrays.asList(intervals));
    }

    public ControlFlowGraph getControlFlowGraph() {
        return controlFlowGraph;
    }

    public int getNumLIROperations() {
        return numLIROperations;
    }


    @Override
    public String toString() {
        return "    Intervals \"" + getName() + "\": " + intervals.length + " intervals\n";
    }
}
