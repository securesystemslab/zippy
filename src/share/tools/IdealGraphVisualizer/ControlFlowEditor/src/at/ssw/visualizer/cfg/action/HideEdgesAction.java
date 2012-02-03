package at.ssw.visualizer.cfg.action;


import at.ssw.visualizer.cfg.graph.CfgScene;
import at.ssw.visualizer.cfg.graph.EdgeWidget;
import at.ssw.visualizer.cfg.model.CfgEdge;
import at.ssw.visualizer.cfg.model.CfgNode;
import org.openide.util.HelpCtx;

/**
 * Hides all edges connected to the selected node.
 *
 * @author Bernhard Stiftner
 * @author Rumpfhuber Stefan
 */
public class HideEdgesAction extends AbstractCfgEditorAction {

    public void performAction() {
        CfgScene tc = getEditor();
        if (tc != null) {
             tc.setSelectedEdgesVisibility(false);
        }
    }

    public String getName() {
        return "Hide Edges";
    }

    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/hideedges.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
