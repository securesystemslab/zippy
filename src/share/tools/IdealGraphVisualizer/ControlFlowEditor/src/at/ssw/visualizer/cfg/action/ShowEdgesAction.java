package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.graph.CfgScene;
import at.ssw.visualizer.cfg.graph.EdgeWidget;
import at.ssw.visualizer.cfg.model.CfgEdge;
import at.ssw.visualizer.cfg.model.CfgNode;
import org.openide.util.HelpCtx;

/**
 * Shows all edges connected to the selected node.
 *
 * @author Bernhard Stiftner
 * @author Rumpfhuber Stefan
 */
public class ShowEdgesAction extends AbstractCfgEditorAction {

    public void performAction() {
        CfgScene tc = getEditor();
        if (tc != null) {
            tc.setSelectedEdgesVisibility(true);
        }
    }

    public String getName() {
        return "Show edges";
    }


    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/showedges.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
