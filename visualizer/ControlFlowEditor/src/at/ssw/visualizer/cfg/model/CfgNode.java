package at.ssw.visualizer.cfg.model;

import at.ssw.visualizer.model.cfg.BasicBlock;


public interface CfgNode {
    //testers    
    public boolean isOSR();
    public boolean isRoot();
    public boolean isLoopHeader();
    public boolean isLoopMember();
       
    //getters
    public int getLevel();
    public int getLoopDepth();
    public int getLoopIndex();
    public int getNodeIndex();
    public CfgEdge[] getInputEdges();
    public CfgEdge[] getOutputEdges();
    public BasicBlock getBasicBlock();
    public String getDescription();
   
}
