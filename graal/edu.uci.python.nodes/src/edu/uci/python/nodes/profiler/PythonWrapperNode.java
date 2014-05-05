package edu.uci.python.nodes.profiler;

import java.util.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.instrument.*;
//import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;

public class PythonWrapperNode extends PNode implements Wrapper {

    @Child private PNode child;
    private final Probe probe;

    public PythonWrapperNode(PythonContext context, PNode child) {
        this.child = child;
        // this.child = insert(child);
        // context.getInstrumentation().getProbe will generate a probe
        this.probe = context.getInstrumentation().getProbe(child.getSourceSection(), null);
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

    public boolean isTaggedAs(PhylumTag tag) {
        return probe.isTaggedAs(tag);
    }

    public Set<PhylumTag> getPhylumTags() {
        return probe.getPhylumTags();
    }

    public Node getChild() {
        return child;
    }

    public Probe getProbe() {
        return probe;
    }

}
