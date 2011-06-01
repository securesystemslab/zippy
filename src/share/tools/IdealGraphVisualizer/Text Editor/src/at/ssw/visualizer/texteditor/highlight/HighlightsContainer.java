package at.ssw.visualizer.texteditor.highlight;

import at.ssw.visualizer.texteditor.model.Scanner;
import at.ssw.visualizer.texteditor.model.Text;
import at.ssw.visualizer.texteditor.model.TextRegion;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.TokenID;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 *
 * @author Christian Wimmer
 * @author Alexander Reder
 */
public class HighlightsContainer extends AbstractHighlightsContainer {

    private static final String HIGHLIGHT_COLORING = "at-ssw-visualizer-highlight";
    
    protected final JTextComponent component;
    protected final Document document;
    protected final AttributeSet highlightColoring;
    protected final Scanner scanner;

    protected TextRegion[] curRegions = null;

    private final CaretListener caretListener = new CaretListener() {

        public void caretUpdate(CaretEvent event) {
            TextRegion[] newRegions = findRegions();
            if (newRegions != curRegions) {
                curRegions = newRegions;
                fireHighlightsChange(0, document.getLength());
            }
        }

    };
    
    protected HighlightsContainer(JTextComponent component, Document document) {
        this.document = document;
        this.component = component;
        component.addCaretListener(WeakListeners.create(CaretListener.class, caretListener, component));
        
        // Load the coloring.
        Text t = (Text) document.getProperty(Text.class);
        MimePath mimePath = MimePath.parse(t.getMimeType());
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        AttributeSet highlight = fcs.getFontColors(HIGHLIGHT_COLORING);
        highlightColoring = highlight != null ? highlight : SimpleAttributeSet.EMPTY;

        scanner = t.getScanner();
        scanner.setText(document);
        curRegions = findRegions();
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return new RegionSequence();
    }

    protected TextRegion[] findRegions() {
        Text text = (Text) document.getProperty(Text.class);
        Caret caret = component.getCaret();
        if (text == null || caret == null) {
            return null;
        }
        scanner.findTokenBegin(caret.getDot());
        TokenID token = scanner.nextToken();
        if (token.getNumericID() < 0) {
            return null;
        }

        return text.getHighlighting(scanner.getTokenString());
    }

    protected class RegionSequence implements HighlightsSequence {

        private int idx = -1;

        public boolean moveNext() {
            idx++;
            return curRegions != null && idx < curRegions.length;
        }

        public int getStartOffset() {
            return curRegions[idx].getStart();
        }

        public int getEndOffset() {
            return curRegions[idx].getEnd();
        }

        public AttributeSet getAttributes() {
            return highlightColoring;
        }
    }

    public static final class HighlightsLayerFactory implements org.netbeans.spi.editor.highlighting.HighlightsLayerFactory {

        public org.netbeans.spi.editor.highlighting.HighlightsLayer[] createLayers(Context context) {
            Text t = (Text) context.getDocument().getProperty(Text.class);
            if(t == null) {
                return new org.netbeans.spi.editor.highlighting.HighlightsLayer[0];
            }
            return new org.netbeans.spi.editor.highlighting.HighlightsLayer[]{org.netbeans.spi.editor.highlighting.HighlightsLayer.create("at-ssw-visualizer-highlighting", ZOrder.SHOW_OFF_RACK, true, new HighlightsContainer(context.getComponent(), context.getDocument()))};
        }

    }
}
