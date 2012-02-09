package at.ssw.visualizer.modelimpl.interval;

import at.ssw.visualizer.model.interval.UsePosition;

/**
 *
 * @author Christian Wimmer
 */
public class UsePositionImpl implements UsePosition, Comparable<UsePositionImpl> {
    private int position;
    private char kind;

    public UsePositionImpl(int position, char kind) {
        this.position = position;
        this.kind = kind;
    }


    public char getKind() {
        return kind;
    }

    public int getPosition() {
        return position;
    }


    public int compareTo(UsePositionImpl other) {
        return getPosition() - other.getPosition();
    }

    @Override
    public String toString() {
        return position + "(" + kind + ")";
    }
}
