package at.ssw.visualizer.model.cfg;

import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public interface State {
    public String getKind();

    public int getSize();

    public String getMethod();

    public List<StateEntry> getEntries();
}
