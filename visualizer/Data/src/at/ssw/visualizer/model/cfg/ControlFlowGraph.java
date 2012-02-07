package at.ssw.visualizer.model.cfg;

import at.ssw.visualizer.model.bc.Bytecodes;
import at.ssw.visualizer.model.nc.NativeMethod;
import com.sun.hotspot.igv.data.FolderElement;
import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public interface ControlFlowGraph extends FolderElement {
    public List<BasicBlock> getBasicBlocks();

    public BasicBlock getBasicBlockByName(String name);

    public Bytecodes getBytecodes();

    public NativeMethod getNativeMethod();

    public boolean hasState();

    public boolean hasHir();

    public boolean hasLir();
}
