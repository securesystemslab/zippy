package edu.uci.python.nodes.profiler;

import java.util.*;

import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem
 */

public class PythonNodeProber implements ASTNodeProber {

    private final PythonContext context;

    private static Map<PythonWrapperNode, ProfilerInstrument> wrapperToInstruments = new HashMap<>();

    public PythonNodeProber(PythonContext context) {
        this.context = context;
    }

    public Node probeAs(Node astNode, PhylumTag tag, Object... args) {
        return astNode;
    }

    public PythonWrapperNode probeAsStatement(PNode node) {
        PythonWrapperNode wrapper;
        if (node instanceof PythonWrapperNode) {
            wrapper = (PythonWrapperNode) node;
        } else {
            wrapper = new PythonWrapperNode(context, node);
            wrapper.getProbe().tagAs(StandardTag.STATEMENT);
            wrapper.assignSourceSection(node.getSourceSection());
        }

        ProfilerInstrument profilerInstrument = new ProfilerInstrument();
        wrapper.getProbe().addInstrument(profilerInstrument);
        wrapperToInstruments.put(wrapper, profilerInstrument);
        return wrapper;
    }

    public static Map<PythonWrapperNode, ProfilerInstrument> getWrapperToInstruments() {
        return wrapperToInstruments;
    }
}
