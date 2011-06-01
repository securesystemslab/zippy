package at.ssw.visualizer.core.selection;

import javax.swing.event.ChangeListener;

/**
 *
 * @author Christian Wimmer
 */
public class SelectionManager {
    private static final SelectionManager SINGLETON = new SelectionManager();

    public static SelectionManager getDefault() {
        return SINGLETON;
    }


    /** Default selection returned when no TopComponent is active.
     * It is also used to maintain listeners added to the selection manager. */
    private final Selection emptySelection;
    private Selection curSelection;

    private SelectionManager() {
        emptySelection = new Selection();
        curSelection = emptySelection;
    }

    public Selection getCurSelection() {
        return curSelection;
    }

    public void setSelection(Selection sel) {
        if (curSelection != sel) {
            curSelection = sel;
            fireChangeEvent();
        }
    }

    public void removeSelection(Selection sel) {
        if (curSelection == sel) {
            curSelection = emptySelection;
            fireChangeEvent();
        }
    }

    protected void fireChangeEvent() {
        emptySelection.fireChangeEvent();
    }

    public void addChangeListener(ChangeListener listener) {
        emptySelection.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        emptySelection.removeChangeListener(listener);
    }
}
