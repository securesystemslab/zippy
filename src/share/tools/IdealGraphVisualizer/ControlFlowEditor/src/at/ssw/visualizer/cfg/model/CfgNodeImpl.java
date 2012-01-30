package at.ssw.visualizer.cfg.model;

import at.ssw.visualizer.model.cfg.BasicBlock;
import java.awt.Color;



public class CfgNodeImpl implements CfgNode {
    private int nodeIndex;
    private BasicBlock basicBlock;
    private int level;   
    private int loopDepth=0;
    private int loopIndex=0; 
    private boolean osr=false;
    private CfgNodeImpl dominator=null;
    private CfgEdgeImpl[] inputEdges = new CfgEdgeImpl[0];
    private CfgEdgeImpl[] outputEdges = new CfgEdgeImpl[0];
    private String description;
    private Color customColor=null;
    private boolean loopHeader;
       
    public CfgNodeImpl(BasicBlock bb, int nodeIndex, String description) {
        this.basicBlock = bb;
        this.nodeIndex = nodeIndex;  
        this.description = description;
        
        if (bb.getPredecessors().size() == 1) {
            BasicBlock pred = bb.getPredecessors().get(0);
            boolean isStd = pred.getPredecessors().size() == 0;
            if (isStd) {
                for (String s : bb.getFlags()) {
                    if (s.equals("osr")) {
                        osr = true;
                        break;
                    }
                }
            }
        }   
    }

    public int getNodeIndex() {
        return nodeIndex;
    }
    
    
    public void setDominator(CfgNodeImpl dominator) {
        this.dominator = dominator;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLoopDepth(int loopDepth) {
        this.loopDepth = loopDepth;
    }

    public void setLoopHeader(boolean loopHeader) {
        this.loopHeader = loopHeader;
    }

    public void setLoopIndex(int loopIndex) {
        this.loopIndex = loopIndex;
    }

    public void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }
       
    public boolean isRoot() {
        return nodeIndex==0;
    }

    public boolean isLoopHeader() {
        return loopHeader;
    }

    public boolean isLoopMember() {
        return loopIndex > 0;
    }

    public int getLevel() {
        return level;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }
    
    public int getLoopDepth() {
        return loopDepth;
    }

    public int getLoopIndex() {
        return loopIndex;
    }
   
    public boolean isOSR() {
        return osr;
    }
    
    @Override
    public String toString(){
        return basicBlock.getName();
    }

    public void setInputEdges(CfgEdgeImpl[] inputEdges){
        this.inputEdges = inputEdges;
    }
    
    
    public void setOutputEdges(CfgEdgeImpl[] outputEdges){
        this.outputEdges = outputEdges;
    }
     
    public CfgEdge[] getInputEdges() {
        return this.inputEdges;
    }

    public CfgEdge[] getOutputEdges() {
        return outputEdges;
    }

    public String getDescription() {
        return description;
    }

    public void setColor(Color color) {
        this.customColor=color;
    }

   
}
