package at.ssw.visualizer.model.cfg;

import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public interface BasicBlock {
    public String getName();

    public int getFromBci();

    public int getToBci();

    public List<? extends BasicBlock> getPredecessors();

    public List<? extends BasicBlock> getSuccessors();

    public List<? extends BasicBlock> getXhandlers();

    public List<String> getFlags();

    public BasicBlock getDominator();

    public int getLoopIndex();

    public int getLoopDepth();

    public boolean hasState();

    public List<State> getStates();

    public boolean hasHir();

    public List<IRInstruction> getHirInstructions();

    public boolean hasLir();

    public List<IRInstruction> getLirOperations();
}
