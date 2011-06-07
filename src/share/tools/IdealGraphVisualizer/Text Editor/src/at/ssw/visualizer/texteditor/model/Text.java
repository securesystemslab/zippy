package at.ssw.visualizer.texteditor.model;

import com.sun.hotspot.igv.data.InputBlock;
import com.sun.hotspot.igv.data.InputGraph;
import java.util.Map;

/**
 * @author Christian Wimmer
 * @author Alexander Reder
 */
public class Text {

    private InputGraph cfg;

    private String text;
    private FoldingRegion[] foldings;
    private Map<String, TextRegion> hyperlinks;
    private Map<String, String> stringHovers;
    private Map<TextRegion, String> regionHovers;
    private Map<String, TextRegion[]> highlighting;
    private Map<InputBlock, BlockRegion> blocks;
    private Scanner scanner;
    private String mimeType;

    
    public Text(InputGraph cfg, String text, FoldingRegion[] foldings, Map<String, TextRegion> hyperlinks, Map<String, String> stringHovers, Map<TextRegion, String> regionHovers, Map<String, TextRegion[]> highlighting, Map<InputBlock, BlockRegion> blocks, Scanner scanner, String mimeType) {
        this.cfg = cfg;
        this.text = text;
        this.foldings = foldings;
        this.hyperlinks = hyperlinks;
        this.stringHovers = stringHovers;
        this.regionHovers = regionHovers;
        this.highlighting = highlighting;
        this.blocks = blocks;
        this.scanner = scanner;
        this.mimeType = mimeType;
    }

    public InputGraph getCfg() {
        return cfg;
    }

    public String getText() {
        return text;
    }

    public FoldingRegion[] getFoldings() {
        return foldings;
    }

    public TextRegion getHyperlinkTarget(String key) {
        return hyperlinks.get(key);
    }

    public String getStringHover(String key) {
        return stringHovers.get(key);
    }

    public String getRegionHover(int position) {
        for (TextRegion r : regionHovers.keySet()) {
            if (r.getStart() <= position && r.getEnd() >= position) {
                return regionHovers.get(r);
            }
        }
        return null;
    }

    public TextRegion[] getHighlighting(String key) {
        return highlighting.get(key);
    }

    public Map<InputBlock, BlockRegion> getBlocks() {
        return blocks;
    }
    
    public Scanner getScanner() {
        return scanner;
    }
    
    public String getMimeType() {
        return mimeType;
    }
}
