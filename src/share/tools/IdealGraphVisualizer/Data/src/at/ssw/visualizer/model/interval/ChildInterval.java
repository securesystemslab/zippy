package at.ssw.visualizer.model.interval;

import java.util.List;

/**
 *
 * @author Christian Wimmer
 */
public interface ChildInterval {
    public Interval getParent();

    public String getRegNum();

    public String getType();

    public String getOperand();

    public String getSpillState();

    public ChildInterval getRegisterHint();

    public List<Range> getRanges();

    public List<UsePosition> getUsePositions();

    public int getFrom();

    public int getTo();
}
