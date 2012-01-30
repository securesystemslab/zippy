package at.ssw.visualizer.model.interval;

import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public interface Interval {
    public IntervalList getParent();

    public List<ChildInterval> getChildren();

    public String getRegNum();

    public int getFrom();

    public int getTo();
}
