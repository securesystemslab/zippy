package at.ssw.visualizer.texteditor.tooltip;

import at.ssw.visualizer.texteditor.model.Scanner;
import at.ssw.visualizer.texteditor.model.Text;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.editor.NbEditorKit;

/**
 *
 * @author Christian Wimmer
 * @author Alexander Reder
 */
public class ToolTipAction extends NbEditorKit.NbBuildToolTipAction {
    public ToolTipAction() {
        putValue(NAME, ExtKit.buildToolTipAction);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (!showTooltip(target)) {
            super.actionPerformed(evt, target);
        }
    }
    
    private boolean showTooltip(JTextComponent target) {
        BaseDocument document = Utilities.getDocument(target);
        Text text = (Text) document.getProperty(Text.class);
        if (text == null) {
            return false;
        }

        EditorUI ui = Utilities.getEditorUI(target);  
        ToolTipSupport tts = ui.getToolTipSupport();
        int offset = target.viewToModel(tts.getLastMouseEvent().getPoint());

        String toolTipText = text.getRegionHover(offset);

        if (toolTipText == null) {
            Scanner scanner = text.getScanner();
            scanner.setText(document);
            scanner.findTokenBegin(offset);
            TokenID token = scanner.nextToken();
            if (token.getNumericID() < 0) {
                return false;
            }

            toolTipText = text.getStringHover(scanner.getTokenString());
            if (toolTipText == null) {
                return false;
            }
        }

        StyledToolTip tooltip = new StyledToolTip(toolTipText, target.getUI().getEditorKit(target));

        tts.setToolTip(tooltip, PopupManager.ViewPortBounds, PopupManager.Largest, 0, 0);
        return true;
    }
}
