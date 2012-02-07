package at.ssw.visualizer.modelimpl.cfg;

import at.ssw.visualizer.model.cfg.State;
import at.ssw.visualizer.model.cfg.StateEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public class StateImpl implements State {
    private String kind;
    private int size;
    private String method;
    private StateEntry[] entries;

    public StateImpl(String kind, int size, String method, StateEntryImpl[] entries) {
        this.kind = kind;
        this.size = size;
        this.method = method;
        this.entries = entries;
    }

    public String getKind() {
        return kind;
    }

    public int getSize() {
        return size;
    }

    public String getMethod() {
        return method;
    }

    public List<StateEntry> getEntries() {
        return Collections.unmodifiableList(Arrays.asList(entries));
    }
}
