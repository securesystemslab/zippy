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
        /**
         * Don't insert the child here, because child node will be replaced with the wrapper node.
         * If child node is inserted here, it's parent (which will be wrapper's parent after
         * replacement) will be lost. Instead, wrapper is created, and the child is replaced with
         * its wrapper, and then wrapper's child is adopted by calling adoptChildren() in
         * {@link ProfilerTranslator}.
         */
        this.child = child;
        /**
         * context.getProbe will either generate a probe for this source section, or return the
         * existing probe for this section. There can be only one probe for the same source section.
         */
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
