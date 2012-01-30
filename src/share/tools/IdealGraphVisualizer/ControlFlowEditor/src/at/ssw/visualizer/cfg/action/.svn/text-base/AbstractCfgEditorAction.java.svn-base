package at.ssw.visualizer.cfg.action;


import at.ssw.visualizer.cfg.graph.CfgEventListener;
import at.ssw.visualizer.cfg.editor.CfgEditorTopComponent;
import at.ssw.visualizer.cfg.graph.CfgScene;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 * The common superclass of all concrete actions related to the CFG visualizer.
 *
 * @author Bernhard Stiftner
 * @author Rumpfhuber Stefan
 */
public abstract class AbstractCfgEditorAction extends CallableSystemAction implements CfgEventListener , PropertyChangeListener {

    CfgEditorTopComponent topComponent = null;
    
    public AbstractCfgEditorAction() {        
        TopComponent.getRegistry().addPropertyChangeListener(this);
        setEnabled(false);

    }
       
    protected CfgEditorTopComponent getEditor() {
        return topComponent;
    }
    
    protected void setEditor(CfgEditorTopComponent newTopComponent) {    
        CfgEditorTopComponent oldTopComponent = getEditor();
        if(newTopComponent != oldTopComponent){
            if(oldTopComponent != null) {
                oldTopComponent.getCfgScene().removeCfgEventListener(this);
            }
            this.topComponent = newTopComponent;         
            if (newTopComponent != null) {
                newTopComponent.getCfgScene().addCfgEventListener(this);
                selectionChanged(newTopComponent.getCfgScene());
            }
            this.setEnabled(newTopComponent!=null);
        }
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
     
    
    public void propertyChange(PropertyChangeEvent e) {  
        if ( e.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED)) {
            if(e.getNewValue() instanceof CfgEditorTopComponent){  
                CfgEditorTopComponent tc = (CfgEditorTopComponent)e.getNewValue();
                setEditor(tc);
                selectionChanged(tc.getCfgScene());
            } 
        }
    }
    
    
    public void selectionChanged(CfgScene scene) {   
    }

}
