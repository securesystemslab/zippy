package at.ssw.visualizer.texteditor.model;

import at.ssw.visualizer.model.Compilation;
import at.ssw.visualizer.model.cfg.BasicBlock;
import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import java.util.Map;

/**
 * @author Christian Wimmer
 * @author Alexander Reder
 */
public class Text {

    private Compilation compilation;
    private ControlFlowGraph cfg;

    private String text;
    private FoldingRegion[] foldings;
    private Map<String, TextRegion> hyperlinks;
    private Map<String, String> stringHovers;
    private Map<TextRegion, String> regionHovers;
    private Map<String, TextRegion[]> highlighting;
    private Map<BasicBlock, BlockRegion> blocks;
    private Scanner scanner;
    private String mimeType;

    
    public Text(ControlFlowGraph cfg, String text, FoldingRegion[] foldings, Map<String, TextRegion> hyperlinks, Map<String, String> stringHovers, Map<TextRegion, String> regionHovers, Map<String, TextRegion[]> highlighting, Map<BasicBlock, BlockRegion> blocks, Scanner scanner, String mimeType) {
        this.compilation = cfg.getCompilation();
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


    public Compilation getCompilation() {
        return compilation;
    }

    public ControlFlowGraph getCfg() {
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

    public Map<BasicBlock, BlockRegion> getBlocks() {
        return blocks;
    }
    
    public Scanner getScanner() {
        return scanner;
    }
    
    public String getMimeType() {
        return mimeType;
    }
}
