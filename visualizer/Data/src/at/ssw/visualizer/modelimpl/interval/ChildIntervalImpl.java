package at.ssw.visualizer.modelimpl.interval;

import at.ssw.visualizer.model.interval.ChildInterval;
import at.ssw.visualizer.model.interval.Interval;
import at.ssw.visualizer.model.interval.Range;
import at.ssw.visualizer.model.interval.UsePosition;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public class ChildIntervalImpl implements ChildInterval, Comparable<ChildIntervalImpl> {
    private Interval parent;
    private String regNum;
    private String type;
    private String operand;
    private String spillState;
    private ChildInterval registerHint;
    private Range[] ranges;
    private UsePosition[] usePositions;

    public void setValues(String regNum, String type, String operand, String spillState, ChildInterval registerHint, Range[] ranges, UsePosition[] usePositions) {
        this.regNum = regNum;
        this.type = type;
        this.operand = operand;
        this.spillState = spillState;
        this.registerHint = registerHint;
        this.ranges = ranges;
        this.usePositions = usePositions;
    }
    
    public Interval getParent() {
        return parent;
    }

    protected void setParent(IntervalImpl parent) {
        this.parent = parent;
    }

    public String getRegNum() {
        return regNum;
    }

    public String getType() {
        return type;
    }

    public String getOperand() {
        return operand;
    }

    public String getSpillState() {
        return spillState;
    }

    public ChildInterval getRegisterHint() {
        return registerHint;
    }

    public List<Range> getRanges() {
        return Collections.unmodifiableList(Arrays.asList(ranges));
    }

    public List<UsePosition> getUsePositions() {
        return Collections.unmodifiableList(Arrays.asList(usePositions));
    }


    public int getFrom() {
        return ranges[0].getFrom();
    }

    public int getTo() {
        return ranges[ranges.length - 1].getTo();
    }


    public int compareTo(ChildIntervalImpl other) {
        return getFrom() - other.getFrom();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(regNum);
        result.append(": ");
        result.append(getType());
        result.append(", ");
        result.append(getOperand());
        result.append(", ");
        if (registerHint != null) {
            result.append(registerHint.getRegNum());
        } else {
            result.append("null");
        }

        result.append("  ");
        for (int i = 0; i < ranges.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(ranges[i]);
        }

        result.append("  ");
        for (int i = 0; i < usePositions.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(usePositions[i]);
        }

        return result.toString();
    }
}
