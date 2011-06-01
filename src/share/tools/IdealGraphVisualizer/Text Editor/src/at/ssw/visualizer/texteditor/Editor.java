package at.ssw.visualizer.texteditor;

import at.ssw.visualizer.core.selection.Selection;
import at.ssw.visualizer.core.selection.SelectionManager;
import at.ssw.visualizer.core.selection.SelectionProvider;
import at.ssw.visualizer.texteditor.model.BlockRegion;
import at.ssw.visualizer.texteditor.model.Text;
import com.sun.hotspot.igv.data.InputBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.text.CloneableEditor;
import org.openide.windows.TopComponent;

/**
 * Abstract template class of a <code> Editor </code> class of the Visualizer.
 * 
 * Must be initialized with a custom <code> EditorSupport </code> class and the 
 * method <code> creatClonedObject </code> must be overwritten by 
 * the <code> Editor </code> implementation.
 * 
 * @author Alexander Reder
 */
public abstract class Editor extends CloneableEditor implements SelectionProvider {
    
    protected Selection selection;
    private boolean selectionUpdating;
    private InputBlock[] curBlocks;
    private boolean initialized;
    
    protected Editor(EditorSupport support) {
        super(support);
        selection = new Selection();
        selection.put(support.getControlFlowGraph());        
        selection.addChangeListener(selectionListener);
    }
    
    public Selection getSelection() {
        return selection;
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        if (!initialized) {
            getEditorPane().addCaretListener(caretListener);
            initialized = true;
        }
    }
    
    @Override
    protected void componentActivated() {
        super.componentActivated();
        SelectionManager.getDefault().setSelection(selection);
    }
    
    @Override
    protected void componentClosed() {
        super.componentClosed();
        SelectionManager.getDefault().removeSelection(selection);
    }
        
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private ChangeListener selectionListener = new ChangeListener() {
        public void stateChanged(ChangeEvent event) {
            if (selectionUpdating) {
                return;
            }
            selectionUpdating = true;
            
            Text text = (Text) getEditorPane().getDocument().getProperty(Text.class);
            InputBlock[] newBlocks = selection.get(InputBlock[].class);
            
            if (newBlocks != null && newBlocks.length > 0 && !Arrays.equals(curBlocks, newBlocks)) {
                BlockRegion r = text.getBlocks().get(newBlocks[0]);
                int startOffset = r.getNameStart();
                int endOffset = r.getNameEnd();
                
                if (newBlocks.length > 1) {
                    for (InputBlock b : newBlocks) {
                        r = text.getBlocks().get(b);
                        startOffset = Math.min(startOffset, r.getStart());
                        endOffset = Math.max(endOffset, r.getEnd());
                    }
                }
                
                getEditorPane().select(startOffset, endOffset);
            }
            curBlocks = newBlocks;
            selectionUpdating = false;
        }
    };
    
    
    private CaretListener caretListener = new CaretListener() {
        public void caretUpdate(CaretEvent event) {
            if (selectionUpdating) {
                return;
            }
            selectionUpdating = true;
            
            Text text = (Text) getEditorPane().getDocument().getProperty(Text.class);
            List<InputBlock> newBlocks = new ArrayList<InputBlock>();
            int startOffset = Math.min(event.getDot(), event.getMark());
            int endOffset = Math.max(event.getDot(), event.getMark());
            
            for (BlockRegion region : text.getBlocks().values()) {
                if (region.getStart() <= endOffset && region.getEnd() > startOffset) {
                    newBlocks.add(region.getBlock());
                }
            }
            
            curBlocks = newBlocks.toArray(new InputBlock[newBlocks.size()]);
            selection.put(curBlocks);
            selectionUpdating = false;
        }
    };
    
}
