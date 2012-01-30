package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.CfgEditorContext;
import at.ssw.visualizer.cfg.editor.CfgEditorTopComponent;
import at.ssw.visualizer.cfg.graph.CfgScene;
import org.openide.util.HelpCtx;


public class HierarchicalNodeLayoutAction extends AbstractCfgEditorAction {

    @Override
    public void performAction() {
        CfgEditorTopComponent tc = getEditor();
        if (tc != null) {
            CfgScene scene = tc.getCfgScene();
            scene.setSceneLayout(CfgEditorContext.LAYOUT_HIERARCHICALNODELAYOUT);
            scene.applyLayout();                                 
        }
    }
   
    public String getName() {
        return "Hierarchical Node Layout";
    }


    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/arrangehier.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
