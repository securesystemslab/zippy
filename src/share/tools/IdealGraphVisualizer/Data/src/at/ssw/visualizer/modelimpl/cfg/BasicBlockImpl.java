package at.ssw.visualizer.modelimpl.cfg;

import at.ssw.visualizer.model.cfg.BasicBlock;
import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import at.ssw.visualizer.model.cfg.IRInstruction;
import at.ssw.visualizer.model.cfg.State;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public class BasicBlockImpl implements BasicBlock {

    private ControlFlowGraph parent;
    private String name;
    private int fromBci;
    private int toBci;
    private BasicBlock[] predecessors;
    private BasicBlock[] successors;
    private BasicBlock[] xhandlers;
    private String[] flags;
    private BasicBlock dominator;
    private int loopIndex;
    private int loopDepth;
    private int firstLirId;
    private int lastLirId;
    private State[] states;
    private IRInstruction[] hirInstructions;
    private IRInstruction[] lirOperations;

    public void setValues(String name, int fromBci, int toBci, BasicBlock[] predecessors, BasicBlock[] successors, BasicBlock[] xhandlers, String[] flags, BasicBlock dominator, int loopIndex, int loopDepth, int firstLirId, int lastLirId, State[] states, IRInstruction[] hirInstructions, IRInstruction[] lirOperations) {
        this.name = name;
        this.fromBci = fromBci;
        this.toBci = toBci;

        this.predecessors = predecessors;
        this.successors = successors;
        this.xhandlers = xhandlers;

        this.flags = flags;
        this.dominator = dominator;
        this.loopIndex = loopIndex;
        this.loopDepth = loopDepth;
        this.firstLirId = firstLirId;
        this.lastLirId = lastLirId;

        this.states = states;
        this.hirInstructions = hirInstructions;
        this.lirOperations = lirOperations;
    }

    public ControlFlowGraph getParent() {
        return parent;
    }

    protected void setParent(ControlFlowGraph parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public int getFromBci() {
        return fromBci;
    }

    public int getToBci() {
        return toBci;
    }

    public void setToBci(int toBci) {
        this.toBci = toBci;
    }

    public List<BasicBlock> getPredecessors() {
        return Collections.unmodifiableList(Arrays.asList(predecessors));
    }

    public List<BasicBlock> getSuccessors() {
        return Collections.unmodifiableList(Arrays.asList(successors));
    }

    public List<BasicBlock> getXhandlers() {
        return Collections.unmodifiableList(Arrays.asList(xhandlers));
    }

    public List<String> getFlags() {
        return Collections.unmodifiableList(Arrays.asList(flags));
    }

    public BasicBlock getDominator() {
        return dominator;
    }

    public int getLoopIndex() {
        return loopIndex;
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    public int getFirstLirId() {
        return firstLirId;
    }

    public int getLastLirId() {
        return lastLirId;
    }

    public boolean hasState() {
        return states != null;
    }

    public List<State> getStates() {
        return Collections.unmodifiableList(Arrays.asList(states));
    }

    public boolean hasHir() {
        return hirInstructions != null;
    }

    public List<IRInstruction> getHirInstructions() {
        return Collections.unmodifiableList(Arrays.asList(hirInstructions));
    }

    public boolean hasLir() {
        return lirOperations != null;
    }

    public List<IRInstruction> getLirOperations() {
        return Collections.unmodifiableList(Arrays.asList(lirOperations));
    }

    @Override
    public String toString() {
        return name;
    }
}
