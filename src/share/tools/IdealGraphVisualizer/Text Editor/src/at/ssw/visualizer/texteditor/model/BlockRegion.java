package at.ssw.visualizer.texteditor.model;

import com.sun.hotspot.igv.data.InputBlock;

/**
 *
 * @author Christian Wimmer
 */
public class BlockRegion extends TextRegion {

    private InputBlock block;

    private int nameStart;
    private int nameEnd;

    public BlockRegion(InputBlock block, int start, int end, int nameStart, int nameEnd) {
        super(start, end);
        this.block = block;
        this.nameStart = nameStart;
        this.nameEnd = nameEnd;
    }


    public InputBlock getBlock() {
        return block;
    }

    public int getNameStart() {
        return nameStart;
    }

    public int getNameEnd() {
        return nameEnd;
    }
}
