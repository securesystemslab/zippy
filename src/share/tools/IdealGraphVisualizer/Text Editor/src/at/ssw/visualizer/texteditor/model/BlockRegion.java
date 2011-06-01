package at.ssw.visualizer.texteditor.model;

import at.ssw.visualizer.model.cfg.BasicBlock;

/**
 *
 * @author Christian Wimmer
 */
public class BlockRegion extends TextRegion {

    private BasicBlock block;

    private int nameStart;
    private int nameEnd;

    public BlockRegion(BasicBlock block, int start, int end, int nameStart, int nameEnd) {
        super(start, end);
        this.block = block;
        this.nameStart = nameStart;
        this.nameEnd = nameEnd;
    }


    public BasicBlock getBlock() {
        return block;
    }

    public int getNameStart() {
        return nameStart;
    }

    public int getNameEnd() {
        return nameEnd;
    }
}
