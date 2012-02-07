package at.ssw.visualizer.model.bc;

import at.ssw.visualizer.model.cfg.ControlFlowGraph;

/**
 * This class holds the bytecode of a method and provides severel methods
 * accessing the details.
 *
 * @author Alexander Reder
 * @author Christian Wimmer
 */
public interface Bytecodes {
    /**
     * Back-link to the control flow graph where the bytecodes were loaded from.
     */
    public ControlFlowGraph getControlFlowGraph();

    /**
     * Called before the first call of getBytecodes() or getEpilogue(). Can be called multiple times.
     */
    public void parseBytecodes();
    
    /**
     * The bytecodes of the method in the given bytecode range.
     *
     * @param   fromBCI starting BCI (including this bci)
     * @param   toBCI   ending BCI (not including this bci)
     * @return          string representation of the bytecodes
     */
    public String getBytecodes(int fromBCI, int toBCI);
    
    public String getEpilogue();
}
