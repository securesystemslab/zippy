package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.editor.CfgEditorTopComponent;
import at.ssw.visualizer.cfg.graph.CfgScene;
import org.openide.util.HelpCtx;

public class ZoomoutAction extends AbstractCfgEditorAction {

    @Override
    public void performAction() {
        CfgEditorTopComponent tc = getEditor();
        if (tc != null) {   
            CfgScene scene = tc.getCfgScene();     
            scene.animateZoom(0.9);
        }
    }
    
    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/zoomout.gif";
    }
       
    @Override
    public String getName() {
        return "Zoomout";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
