package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.CfgEditorContext;
import at.ssw.visualizer.cfg.graph.CfgScene;
import org.openide.util.HelpCtx;


public class HierarchicalCompoundLayoutAction extends AbstractCfgEditorAction {
    
      public void performAction() {
        CfgScene tc = getEditor();
        if (tc != null) {
            tc.setSceneLayout(CfgEditorContext.LAYOUT_HIERARCHICALCOMPOUNDLAYOUT);           
            tc.applyLayout();                   
        }
    }

    public String getName() {
        return "Hierarchical Compound Layout";
    }


    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/arrangeloop.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
  
   
}
