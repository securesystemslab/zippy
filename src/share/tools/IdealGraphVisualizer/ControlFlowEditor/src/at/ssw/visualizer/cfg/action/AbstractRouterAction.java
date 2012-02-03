package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.graph.CfgScene;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import org.openide.util.actions.Presenter;

/**
 * Common superclass for all actions which set the link router.
 *
 * @author Bernhard Stiftner
 * @author Rumpfhuber Stefan
 */
public abstract class AbstractRouterAction extends AbstractCfgEditorAction implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar {
       
    public void performAction() {
        CfgScene tc = getEditor();
        if (tc != null) {  
            setLinkRouter(tc);
        }
    }

    protected abstract void setLinkRouter(CfgScene editor);
    
    @Override
    public JMenuItem getMenuPresenter() {
        JMenuItem presenter = new MenuPresenter();
        presenter.setToolTipText(getName());
        return presenter;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }

    @Override
    public JComponent getToolbarPresenter() {
        ToolbarPresenter presenter = new ToolbarPresenter();    
        presenter.setToolTipText(getName());
        return presenter;
    }
      
    class MenuPresenter extends JRadioButtonMenuItem {

        public MenuPresenter() {
            super(AbstractRouterAction.this);
            setIcon(null);
        }      
    }

    class ToolbarPresenter extends JToggleButton {

        public ToolbarPresenter() {
            super(AbstractRouterAction.this);
            setText(null);  
        }
    } 
}
