package at.ssw.visualizer.modelimpl.interval;

import at.ssw.visualizer.model.interval.Range;

/**
 *
 * @author Christian Wimmer
 */
public class RangeImpl implements Range, Comparable<RangeImpl> {
    private int from;
    private int to;

    public RangeImpl(int from, int to) {
        this.from = from;
        this.to = to;
    }


    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }


    public int compareTo(RangeImpl other) {
        return getFrom() - other.getFrom();
    }

    @Override
    public String toString() {
        return "[" + from + ", " + to + "]";
    }
}
