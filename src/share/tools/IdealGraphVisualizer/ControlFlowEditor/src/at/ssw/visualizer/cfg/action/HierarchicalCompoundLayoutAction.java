package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.CfgEditorContext;
import at.ssw.visualizer.cfg.graph.CfgScene;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;


@ActionID(id = "HierarchicalCompoundLayout", category = "View")
@ActionRegistration(displayName = "Compound Layout", iconBase="at/ssw/visualizer/cfg/icons/arrangeloop.gif")
@ActionReference(path = "CompilationViewer/CFG/Actions", position = 150)
public class HierarchicalCompoundLayoutAction implements ActionListener {
    List<CfgScene> scenes;
    
    public HierarchicalCompoundLayoutAction(List<CfgScene> scenes) {
        this.scenes = scenes;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        for (CfgScene s : scenes) {
            s.setSceneLayout(CfgEditorContext.LAYOUT_HIERARCHICALCOMPOUNDLAYOUT);
        }
    }
}
