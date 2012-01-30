package at.ssw.visualizer.modelimpl.cfg;

import at.ssw.visualizer.model.bc.Bytecodes;
import at.ssw.visualizer.model.cfg.BasicBlock;
import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import at.ssw.visualizer.model.nc.NativeMethod;
import com.sun.hotspot.igv.data.AbstractFolderElement;
import com.sun.hotspot.igv.data.FolderElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Wimmer
 */
public class ControlFlowGraphImpl extends AbstractFolderElement implements ControlFlowGraph {
    private BasicBlock[] basicBlocks;
    private Map<String, BasicBlock> blockNames;
    private Bytecodes bytecodes;
    private NativeMethod nativeMethod;
    private boolean hasState;
    private boolean hasHir;
    private boolean hasLir;

    public ControlFlowGraphImpl(String shortName, String name, BasicBlockImpl[] basicBlocks) {
        super(shortName, name);
        this.basicBlocks = basicBlocks;

        blockNames = new HashMap<String, BasicBlock>(basicBlocks.length);
        nativeMethod = null;
        for (BasicBlockImpl block : basicBlocks) {
            block.setParent(this);
            blockNames.put(block.getName(), block);
            hasState |= block.hasState();
            hasHir |= block.hasHir();
            hasLir |= block.hasLir();
        }
    }

    public List<BasicBlock> getBasicBlocks() {
        return Collections.unmodifiableList(Arrays.asList(basicBlocks));
    }

    public BasicBlock getBasicBlockByName(String name) {
        return blockNames.get(name);
    }

    public Bytecodes getBytecodes() {
        return bytecodes;
    }

    public void setBytecodes(Bytecodes bytecodes) {
        this.bytecodes = bytecodes;
    }

    public NativeMethod getNativeMethod() {
        return nativeMethod;
    }

    public void setNativeMethod(NativeMethod nativeMethod) {
        this.nativeMethod = nativeMethod;
    }

    public boolean hasState() {
        return hasState;
    }

    public boolean hasHir() {
        return hasHir;
    }

    public boolean hasLir() {
        return hasLir;
    }

    @Override
    public String toString() {
        return "    CFG \"" + getName() + "\": " + basicBlocks.length + " blocks\n";
    }
}
