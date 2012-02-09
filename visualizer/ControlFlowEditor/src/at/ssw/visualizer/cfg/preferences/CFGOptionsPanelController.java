package at.ssw.visualizer.cfg.preferences;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Controller for the settings page displayed in the options dialog.
 *
 * @author Bernhard Stiftner
 */
public class CFGOptionsPanelController extends OptionsPanelController {

    CFGOptionsPanel optionsPanel;

    
    public void update() {
        getOptionsPanel().update();       
    }

    public void applyChanges() {
        getOptionsPanel().applyChanges();
    }

    public void cancel() {
        getOptionsPanel().cancel();
    }

    public boolean isValid() {
        return getOptionsPanel().isDataValid();
    }

    public boolean isChanged() {
        return getOptionsPanel().isChanged();
    }

    public JComponent getComponent(Lookup masterLookup) {
        return getOptionsPanel();
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getOptionsPanel().addPropertyChangeListener(l);
    }

    //todo: investigate - who removes the changelistener ?
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getOptionsPanel().removePropertyChangeListener(l);
    }

    private CFGOptionsPanel getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = new CFGOptionsPanel();
        }
        return optionsPanel;
    }

}
