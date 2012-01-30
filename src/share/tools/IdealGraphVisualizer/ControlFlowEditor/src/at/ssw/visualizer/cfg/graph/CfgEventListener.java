package at.ssw.visualizer.cfg.graph;

import java.util.EventListener;


public interface CfgEventListener extends EventListener {
    
    /**
     *  the node or the edge selection got changed
     */
    public void selectionChanged(CfgScene scene);
      
}
