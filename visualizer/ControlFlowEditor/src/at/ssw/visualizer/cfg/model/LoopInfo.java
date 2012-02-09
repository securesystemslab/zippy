package at.ssw.visualizer.cfg.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class LoopInfo {  
    private CfgNode header;//the target node of the backedge
    private int loopIndex; //index of the min cycleSet
    private int loopDepth; //nested depth >=1 
    private LoopInfo parent=null;  
    private Set<CfgNode> members;
    private List<CfgEdge> backEdges = new ArrayList<>();//dfs backEdge

    protected void setLoopDepth(int depth) {
        this.loopDepth=depth;
    }

    protected void setLoopIndex(int loopIndex) {
        this.loopIndex = loopIndex;
    }

    public int getLoopDepth() {
        return loopDepth;
    }
            
    public Set<CfgNode> getMembers() {
        return members;
    }

    protected void setMembers(Set<CfgNode> members) {
        this.members = members;
    }
        
    public int getLoopIndex() {
        return loopIndex;
    }

    protected void setParent(LoopInfo parent) {
        this.parent = parent;
    }
    
    public LoopInfo getParent(){
        return parent;
    }
    
    public List<CfgEdge> getBackEdges() {
        return backEdges;
    }

    public CfgNode getHeader() {
        return header;
    }

    protected void setHeader(CfgNode header) {
        this.header = header;
    }

    @Override
    public String toString(){
        return "Loop(" + header.toString()+ ")-->" + members.toString();
    }

    
  
}
