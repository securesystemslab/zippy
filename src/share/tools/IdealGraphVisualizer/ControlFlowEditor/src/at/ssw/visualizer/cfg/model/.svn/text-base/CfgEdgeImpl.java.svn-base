package at.ssw.visualizer.cfg.model;

public class CfgEdgeImpl implements CfgEdge {   
    private CfgNodeImpl sourceNode;
    private CfgNodeImpl targetNode;
    private boolean symmetric;
    private boolean backedge;

    public CfgEdgeImpl(CfgNodeImpl sourceNode, CfgNodeImpl targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }
   
    public void setSymmetric(boolean symmetric){
        this.symmetric = symmetric;
    }
    
    public void setBackEdge(boolean isBackedge){
        this.backedge = isBackedge;
    }
        
    public CfgNode getSourceNode() {
        return sourceNode;
    }

    public CfgNode getTargetNode() {
        return targetNode;
    }

    public boolean isBackEdge() {
        return this.backedge;
    }

    public boolean isSymmetric() {
        return symmetric;
    }

    public boolean isReflexive() {
        return sourceNode==targetNode;
    }
    
    public boolean isXhandler() {
        return sourceNode.getBasicBlock().getXhandlers().contains(targetNode.getBasicBlock());
    }

    @Override
    public String toString(){
        return this.sourceNode.getBasicBlock().getName() + "->" + targetNode.getBasicBlock().getName();
    }  
}
