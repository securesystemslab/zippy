package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.CfgEditorContext;
import at.ssw.visualizer.cfg.graph.CfgScene;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;


@ActionID(id = "HierarchicalNodeLayout", category = "View")
@ActionRegistration(displayName = "Layout", iconBase="at/ssw/visualizer/cfg/icons/arrangehier.gif")
@ActionReference(path = "CompilationViewer/CFG/Actions", position = 160)
public class HierarchicalNodeLayoutAction implements ActionListener {
    List<CfgScene> scenes;
    
    public HierarchicalNodeLayoutAction(List<CfgScene> scenes) {
        this.scenes = scenes;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        for (CfgScene s : scenes) {
            s.setSceneLayout(CfgEditorContext.LAYOUT_HIERARCHICALNODELAYOUT);
        }
    }
}
