package at.ssw.visualizer.cfg.graph;

import at.ssw.visualizer.cfg.model.LoopInfo;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Widget;

public class LoopClusterWidget extends Widget implements Comparable<LoopClusterWidget> {
    private static final int INSET = 10;//min space between node members and cluster border
    private static final int DASHSIZE = 10;  
    private Color color = Color.BLUE;
    private int loopIndex;
    private int loopDepth; 
    private CfgScene cfgscene;
    private ArrayList<NodeWidget> members = new ArrayList<NodeWidget>();
  
    public LoopClusterWidget(CfgScene scene,  int loopdepth, final int loopindex) {
        super(scene);
        this.cfgscene = scene;
        this.loopIndex = loopindex;
        this.loopDepth = loopdepth;          
        this.setBorder(BorderFactory.createDashedBorder(color, DASHSIZE, DASHSIZE/2, true));          
        this.getActions().addAction(ActionFactory.createEditAction( new EditProvider() { //double click action
            public void edit(Widget w) {
                if(w instanceof LoopClusterWidget){                 
                    for(LoopInfo info : cfgscene.getCfgEnv().getLoopMap().values()){
                        if(info.getLoopIndex() == loopindex){              
                            cfgscene.setNodeSelection(info.getMembers());                         
                            break;
                        }
                    }   
                }
            }
        }));
               
    }

    public List<NodeWidget> getMembers() {
        return members;
    }

    public int getLoopIndex() {
        return loopIndex;
    }
    
    public void addMember(NodeWidget nw) {
        assert(!this.members.contains(nw));        
        members.add(nw);       
    }

    public boolean removeMember(NodeWidget nw) {
        if(this.members.contains(nw)){
            members.remove(nw);       
            return true;
        }
        return false;
    }
  
    public void setrandomColor(){
        if(this.loopDepth == 0 ) return; 
        Random rand = new Random();
        Color randColor = Color.getHSBColor(rand.nextFloat()%360,0.1f,1.0f);      
        this.setBackground(randColor);
    }
    
    //updates the bounds of the widget,
    //and revalidates the widget if a membernode changed the scene position
    public void updateClusterBounds(){      
        Rectangle boundRect=null;
        
        for(NodeWidget nw : this.members){
            if(boundRect==null){
                boundRect = nw.convertLocalToScene(nw.getBounds());  
            } else {
                boundRect = boundRect.union(nw.convertLocalToScene(nw.getBounds()));
            }  
        }
        if(boundRect==null) return;
        for(Widget w : this.getChildren()) {
            if(w instanceof LoopClusterWidget) {            
                LoopClusterWidget lc = (LoopClusterWidget)w;
                lc.updateClusterBounds();
                boundRect = boundRect.union(w.convertLocalToScene(w.getBounds()));
            }
        }
       
        boundRect.grow(INSET, INSET);
        this.setPreferredBounds(boundRect);               
    }
    
    
    public int compareTo(LoopClusterWidget o) {
        return new Integer(this.loopDepth).compareTo(o.loopDepth);
    }
    
    
    @Override
    public String toString(){
        return "LoopCluster: [DEPTH "+this.loopDepth+ "] [INDEX "+this.loopIndex+"]";
    }
}
