package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.CfgEditorContext;
import at.ssw.visualizer.cfg.graph.CfgScene;
import org.openide.util.HelpCtx;


public class UseDirectLineRouterAction extends AbstractRouterAction {
 
    @Override
    protected void setLinkRouter(CfgScene editor) {
        editor.setRouter(CfgEditorContext.ROUTING_DIRECTLINES);
    }
    
    @Override
    public String getName() {
        return "User Direct Router";
    }
    
    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/fanrouter.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
