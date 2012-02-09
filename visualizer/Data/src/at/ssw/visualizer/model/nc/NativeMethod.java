package at.ssw.visualizer.model.nc;

import at.ssw.visualizer.model.cfg.ControlFlowGraph;

/**
 *
 * @author Alexander Reder
 */
public interface NativeMethod {

    public ControlFlowGraph getControlFlowGraph();
    
    public String getMethodText();
    
}
