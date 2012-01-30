package at.ssw.visualizer.cfg.preferences;

import java.awt.Font;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Bernhard Stiftner
 */
public class FontChooserDialog {

    /*
     * Displays a font selection dialog.
     * @return The selected font, or the initial font if the user chose
     * to bail out
     */
    public static Font show(Font initialFont) {
        PropertyEditor pe = PropertyEditorManager.findEditor(Font.class);
        if (pe == null) {
            throw new RuntimeException("Could not find font editor component.");
        }
        pe.setValue(initialFont);
        DialogDescriptor dd = new DialogDescriptor(
                pe.getCustomEditor(),
                "Choose Font");
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            Font f = (Font)pe.getValue();
            return f;
        }
        return initialFont;
    }

}
