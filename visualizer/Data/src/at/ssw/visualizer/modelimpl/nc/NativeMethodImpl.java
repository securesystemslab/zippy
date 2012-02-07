package at.ssw.visualizer.modelimpl.nc;

import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import at.ssw.visualizer.model.nc.NativeMethod;

/**
 *
 * @author Alexander Reder
 */
public class NativeMethodImpl implements NativeMethod {

    private ControlFlowGraph controlFlowGraph;
    private String methodText;
    
    public NativeMethodImpl(ControlFlowGraph controlFlowGraph, String methodText) {
        this.controlFlowGraph = controlFlowGraph;
        this.methodText = methodText;
    }

    public ControlFlowGraph getControlFlowGraph() {
        return controlFlowGraph;
    }
    
    public String getMethodText() {
        return methodText;
    }

}
