package at.ssw.visualizer.model.cfg;

import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public interface BasicBlock {
    public ControlFlowGraph getParent();

    public String getName();

    public int getFromBci();

    public int getToBci();

    public List<BasicBlock> getPredecessors();

    public List<BasicBlock> getSuccessors();

    public List<BasicBlock> getXhandlers();

    public List<String> getFlags();

    public BasicBlock getDominator();

    public int getLoopIndex();

    public int getLoopDepth();

    public int getFirstLirId();

    public int getLastLirId();

    public boolean hasState();

    public List<State> getStates();

    public boolean hasHir();

    public List<IRInstruction> getHirInstructions();

    public boolean hasLir();

    public List<IRInstruction> getLirOperations();
}
