package at.ssw.visualizer.texteditor.model;

import com.sun.hotspot.igv.data.InputBlock;
import com.sun.hotspot.igv.data.InputGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Alexander Reder
 */
public abstract class TextBuilder {

    protected StringBuilder text;
    protected Scanner scanner;
    protected List<FoldingRegion> foldingRegions;
    protected Map<String, TextRegion> hyperlinks;
    protected Map<String, String> stringHovers;
    protected Map<TextRegion, String> regionHovers;
    protected Map<String, TextRegion[]> highlighting;
    protected Map<InputBlock, BlockRegion> blocks;
    protected Set<String> hoverKeys;
    protected Map<String, String> hoverDefinitions;
    protected Map<String, List<String>> hoverReferences;
    
    public TextBuilder() {
        text = new StringBuilder(4 * 1024);
        foldingRegions = new ArrayList<FoldingRegion>();
        hyperlinks = new HashMap<String, TextRegion>();
        stringHovers = new HashMap<String, String>();
        regionHovers = new HashMap<TextRegion, String>();
        highlighting = new HashMap<String, TextRegion[]>();
        blocks = new HashMap<InputBlock, BlockRegion>();
        hoverKeys = new HashSet<String>();
        hoverDefinitions = new HashMap<String, String>();
        hoverReferences = new HashMap<String, List<String>>();
    }
    
    public abstract Text buildDocument(InputGraph cfg);
    
    protected abstract void buildHighlighting();
    
    protected Text buildText(InputGraph cfg, String mimeType) {
        buildHovers();
        buildHighlighting();
        return new Text(cfg, text.toString(), foldingRegions.toArray(new FoldingRegion[foldingRegions.size()]), hyperlinks, stringHovers, regionHovers, highlighting, blocks, scanner, mimeType);
    }
    
    protected void appendBlockDetails(InputBlock block) {
        text.append(blockDetails(block));
    }
        
    protected String blockDetails(InputBlock block) {
        StringBuilder sb = new StringBuilder();
        sb.append(block.getName());
        hoverKeys.add(block.getName());
        // TODO: predecessors, successors, BCI, ...
        return sb.toString();
    }
    
    protected void appendBlockList(StringBuilder sb, String prefix, List<InputBlock> blocks) {
        for (InputBlock block : blocks) {
            sb.append(prefix);
            prefix = ",";
            sb.append(block.getName());
        }
    }
    
    private void appendList(StringBuilder sb, String prefix, List<String> values) {
        for (String value : values) {
            sb.append(prefix).append(value);
            prefix = ",";
        }
    }
    
    protected void buildHovers() {
        StringBuilder sb;
        for(String key : hoverKeys) {
            sb = new StringBuilder();
            if(hoverDefinitions.containsKey(key) && hoverReferences.containsKey(key)) {
                sb.append("Definition;\n");
                sb.append(hoverDefinitions.get(key));
                sb.append("\n");
            }
            if(hoverReferences.containsKey(key)) {
                sb.append("References:\n");
                for(String ref : hoverReferences.get(key)) {
                    sb.append(ref);
                    sb.append("\n");
                }
            }
            if(sb.length() > 0) {
                stringHovers.put(key, sb.toString().substring(0, sb.length() - 1));
            }            
        }
    }
    
}
