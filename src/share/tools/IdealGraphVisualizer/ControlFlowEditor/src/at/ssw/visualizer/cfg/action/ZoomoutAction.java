package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.graph.CfgScene;
import org.openide.util.HelpCtx;

public class ZoomoutAction extends AbstractCfgEditorAction {

    @Override
    public void performAction() {
        CfgScene tc = getEditor();
        if (tc != null) {   
            CfgScene scene = tc;     
            scene.setZoomFactor(0.9);
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
