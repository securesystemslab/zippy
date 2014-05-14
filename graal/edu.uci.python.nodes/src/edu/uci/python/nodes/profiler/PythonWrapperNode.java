package edu.uci.python.nodes.profiler;

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.instrument.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem
 */

public class PythonWrapperNode extends PNode implements Wrapper {

    @Child private PNode child;
    private final Probe probe;

    public PythonWrapperNode(PythonContext context, PNode child) {
        this.child = child;
        // this.child = insert(child);
        // context.getInstrumentation().getProbe will generate a probe
        // this.probe = context.getInstrumentation().getProbe(child.getSourceSection(), null);
        this.probe = context.getProbe(child.getSourceSection());
    }

    @Override
    public Object execute(VirtualFrame frame) {
        probe.notifyEnter(child, frame);

        Object result;

        try {
            result = child.execute(frame);
            probe.notifyLeave(child, frame, result);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.notifyLeaveExceptional(child, frame, e);
            throw (e);
        }

        return result;
    }

    public PNode getChild() {
        return child;
    }

    public Probe getProbe() {
        return probe;
    }

    @SlowPath
    public boolean isTaggedAs(PhylumTag tag) {
        return probe.isTaggedAs(tag);
    }

    @SlowPath
    public Iterable<PhylumTag> getPhylumTags() {
        return probe.getPhylumTags();
    }

    @SlowPath
    public void tagAs(PhylumTag tag) {
        probe.tagAs(tag);
    }

}
