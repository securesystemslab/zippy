package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.graph.CfgScene;
import org.openide.util.HelpCtx;


public class ZoominAction extends AbstractCfgEditorAction {
       
    @Override
    public void performAction() {
        CfgScene tc = getEditor();
        if (tc != null) {            
            CfgScene scene = tc;    
            scene.animateZoom(1.1);
        }
    }
    
    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/zoomin.gif";
    }

    @Override
    public String getName() {
        return "Zoomin";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
