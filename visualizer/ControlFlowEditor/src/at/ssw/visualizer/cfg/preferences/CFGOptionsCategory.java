package at.ssw.visualizer.cfg.preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.ImageUtilities;

/**
 * Descriptor for the settings page displayed in the options dialog.
 *
 * @author Bernhard Stiftner
 */
public class CFGOptionsCategory extends OptionsCategory {
    
    public OptionsPanelController create() {
        return new CFGOptionsPanelController();
    }

    public String getCategoryName() {
        return "Control Flow Graph";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage("at/ssw/visualizer/cfg/icons/cfg32.gif"));
    }

    public String getTitle() {
        return "CFG Visualizer";
    }
}
