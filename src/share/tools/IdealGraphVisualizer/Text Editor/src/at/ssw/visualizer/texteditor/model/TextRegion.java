package at.ssw.visualizer.texteditor.model;

/**
 *
 * @author Christian Wimmer
 * @author Alexander Reder
 */
public class TextRegion {
    
    private int start;
    private int end;

    public TextRegion(int start, int end) {
        this.start = start;
        this.end = end;
    }

    
    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

}
