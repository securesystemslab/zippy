package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.editor.CfgEditorTopComponent;
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
        CfgEditorTopComponent tc = getEditor();
        if (tc != null) {
            tc.getCfgScene().setSelectedEdgesVisibility(true);
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

    @Override  
    public void selectionChanged(CfgScene scene) {      
        for (CfgNode n : scene.getSelectedNodes()) {  
            for (CfgEdge e : scene.findNodeEdges(n, true, true) ){
                EdgeWidget ew = (EdgeWidget) scene.findWidget(e);
                if(!ew.isVisible()) {
                    setEnabled(true);
                    return;
                }
            }
        }
        setEnabled(false);
    }
}
