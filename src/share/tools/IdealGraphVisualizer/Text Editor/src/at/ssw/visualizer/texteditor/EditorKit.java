package at.ssw.visualizer.texteditor;

import at.ssw.visualizer.texteditor.tooltip.ToolTipAction;
import javax.swing.Action;
import javax.swing.text.TextAction;
import org.netbeans.modules.editor.NbEditorKit;

/**
 * Abstract template class of a <code> EditorKit </code>class of the Visualizer.
 * 
 * The <code> scanner </code> field must be initialized with the
 * custom <code> Scanner </code> implementation and the method <code> 
 * getContentType </code> must be overwritten and return the mime type 
 * for the editor.
 * 
 * @author Alexander Reder
 */
public abstract class EditorKit extends NbEditorKit {

    @Override
    protected Action[] getCustomActions() {
        Action[] prev = super.getCustomActions();
        Action[] added = new Action[]{new ToolTipAction()};
        if (prev != null) {
            return TextAction.augmentList(prev, added);
        } else {
           return added;
        }
    }
    
}
