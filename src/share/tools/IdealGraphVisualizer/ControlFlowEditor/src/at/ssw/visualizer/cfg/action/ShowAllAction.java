package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.editor.CfgEditorTopComponent;
import at.ssw.visualizer.cfg.graph.CfgScene;
import org.openide.util.HelpCtx;


/**
 * Adjusts the Zoom factor of the Scene to the bounds of Scroll panel 
 * to get a clean view on the whole graph.
 *
 */
public class ShowAllAction extends AbstractCfgEditorAction {
    
    @Override
    public void performAction() {    
        CfgEditorTopComponent tc = getEditor();
        if (tc != null) {
            CfgScene scene = tc.getCfgScene();  
            scene.zoomScene();        

        }        
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public String getName() {
        return "Fit Scene to Window";
    }
    
    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/autosize.gif";    
    }
          
}   
