package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.editor.CfgEditorSupport;
import at.ssw.visualizer.cfg.editor.CfgEditorTopComponent;
import at.ssw.visualizer.cfg.icons.Icons;
import at.ssw.visualizer.core.focus.Focus;
import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/**
 * Shows the CFG visualizer for the currently selected compilation.
 *
 * @author Bernhard Stiftner
 * @author Christian Wimmer
 */
public final class ShowCFGEditorAction extends CookieAction {
         
    protected void performAction(Node[] activatedNodes) {       
        ControlFlowGraph cfg = activatedNodes[0].getLookup().lookup(ControlFlowGraph.class);
        if (!Focus.findEditor(CfgEditorTopComponent.class, cfg)) {
            CfgEditorSupport editor = new CfgEditorSupport(cfg);
            editor.open();
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes)) {
            return false;
        }
        ControlFlowGraph cfg = activatedNodes[0].getLookup().lookup(ControlFlowGraph.class);
        return cfg.getBasicBlocks().size() > 0;
    }

    public String getName() {
        return "Open Control Flow Graph";
    }

    @Override
    protected String iconResource() {
        return Icons.CFG;
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    protected Class[] cookieClasses() {
        return new Class[]{ControlFlowGraph.class};
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
