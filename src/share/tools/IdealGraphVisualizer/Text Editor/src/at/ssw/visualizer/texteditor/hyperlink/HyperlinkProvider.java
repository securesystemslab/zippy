package at.ssw.visualizer.texteditor.hyperlink;

import at.ssw.visualizer.texteditor.model.Scanner;
import at.ssw.visualizer.texteditor.model.Text;
import at.ssw.visualizer.texteditor.model.TextRegion;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.Utilities;

/**
 * 
 * @author Bernhard Stiftner
 * @author Christian Wimmer
 * @author Alexander Reder
 */
public class HyperlinkProvider implements org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider {
    
    protected Scanner scanner = null;
    
    protected TextRegion findTarget(Document doc, int offset) {
        Text text = (Text) doc.getProperty(Text.class);
        if (text == null) {
            return null;
        }
        
        scanner = text.getScanner();
        scanner.setText(doc);
        scanner.findTokenBegin(offset);
        TokenID token = scanner.nextToken();
        if (token.getNumericID() < 0) {
            return null;
        }
        
        return text.getHyperlinkTarget(scanner.getTokenString());
    }

    public boolean isHyperlinkPoint(Document doc, int offset) {
        return findTarget(doc, offset) != null;
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (findTarget(doc, offset) != null) {
            return new int[]{scanner.getTokenOffset(), scanner.getTokenOffset() + scanner.getTokenLength()};
        }
        return null;
    }

    public void performClickAction(Document doc, int offset) {
        TextRegion target = findTarget(doc, offset);
        if (target != null) {
            JTextComponent editor = Utilities.getFocusedComponent();
            editor.select(target.getStart(), target.getEnd());
        }
    }

}
