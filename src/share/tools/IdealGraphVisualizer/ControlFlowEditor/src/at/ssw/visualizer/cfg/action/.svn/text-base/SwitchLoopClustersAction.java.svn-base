package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.editor.CfgEditorTopComponent;
import at.ssw.visualizer.cfg.graph.CfgScene;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import org.openide.util.HelpCtx;
import org.openide.util.actions.Presenter;

public class SwitchLoopClustersAction extends AbstractCfgEditorAction implements Presenter.Toolbar {

    @Override
    public void performAction() {
        CfgEditorTopComponent tc = getEditor();
        if (tc != null) {  
            CfgScene scene = tc.getCfgScene();
            boolean visible = scene.isLoopClusterVisible();
            scene.setLoopWidgets(!visible);
        }
    }

    @Override
    public String getName() {
        return "Enable/Disable Loop Clusters";
    }

    @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/cluster.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    
    @Override
    public JComponent getToolbarPresenter() {
        return new ToolbarPresenter();       
    }
    
    class ToolbarPresenter extends JToggleButton {     
        private static final String TOOLTIP_ENABLE = "Enable LoopClusters";
        private static final String TOOLTIP_DISABLE = "Disable LoopClusters";
        
        public ToolbarPresenter() {      
            super(SwitchLoopClustersAction.this);
            setText(null);    
            this.setToolTipText(TOOLTIP_DISABLE);
            this.setSelected(true);
            
            this.addItemListener(new ItemListener(){
                public void itemStateChanged(ItemEvent e) {
                    if(isSelected()){
                        setToolTipText(TOOLTIP_DISABLE);
                    } else {
                        setToolTipText(TOOLTIP_ENABLE);
                    }
                }
            });      
        }
    }

}
