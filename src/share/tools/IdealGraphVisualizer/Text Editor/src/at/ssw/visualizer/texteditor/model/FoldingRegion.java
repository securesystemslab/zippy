package at.ssw.visualizer.texteditor.model;

import org.netbeans.api.editor.fold.FoldType;

/**
 *
 * @author Christian Wimmer
 * @author Alexander Reder
 */
public class FoldingRegion extends TextRegion {

       
    private FoldType kind;
    private boolean initallyCollapsed;


    public FoldingRegion(FoldType kind, int start, int end, boolean initiallyCollapsed) {
        super(start, end);
        this.kind = kind;
        this.initallyCollapsed = initiallyCollapsed;
    }

    
    public FoldType getKind() {
        return kind;
    }

    public boolean isInitiallyCollapsed() {
        return initallyCollapsed;
    }
        
}
