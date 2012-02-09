package at.ssw.visualizer.modelimpl.interval;

import at.ssw.visualizer.model.interval.ChildInterval;
import at.ssw.visualizer.model.interval.Interval;
import at.ssw.visualizer.model.interval.IntervalList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public class IntervalImpl implements Interval {
    private IntervalList parent;
    private ChildInterval[] children;

    public IntervalImpl(ChildIntervalImpl[] children) {
        this.children = children;
        for (ChildIntervalImpl child : children) {
            child.setParent(this);
        }
    }

    public IntervalList getParent() {
        return parent;
    }

    protected void setParent(IntervalListImpl parent) {
        this.parent = parent;
    }

    public List<ChildInterval> getChildren() {
        return Collections.unmodifiableList(Arrays.asList(children));
    }

    public String getRegNum() {
        return children[0].getRegNum();
    }

    public int getFrom() {
        return children[0].getFrom();
    }

    public int getTo() {
        return children[children.length - 1].getTo();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < children.length; i++) {
            if (i > 0) {
                result.append("\n  ");
            }
            result.append(children[i]);
        }
        return result.toString();
    }
}
