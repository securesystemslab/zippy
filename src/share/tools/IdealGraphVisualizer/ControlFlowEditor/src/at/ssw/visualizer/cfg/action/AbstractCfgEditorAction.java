package at.ssw.visualizer.cfg.action;


import at.ssw.visualizer.cfg.graph.CfgScene;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;

/**
 * The common superclass of all concrete actions related to the CFG visualizer.
 *
 * @author Bernhard Stiftner
 * @author Rumpfhuber Stefan
 */
public abstract class AbstractCfgEditorAction extends CallableSystemAction {

    CfgScene topComponent = null;
    
    public AbstractCfgEditorAction() {        
        setEnabled(false);

    }
       
    protected CfgScene getEditor() {
        return Utilities.actionsGlobalContext().lookup(CfgScene.class);
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        return new JMenuItem(this);
    }

   
    @Override
    public JComponent getToolbarPresenter() {
        JButton b = new JButton(this);
        if (getIcon() != null) {
            b.setText(null);
            b.setToolTipText(getName());
        }
        return b;
    }

   
    @Override
    public JMenuItem getPopupPresenter() {       
        return new JMenuItem(this);
    }
    
   
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
