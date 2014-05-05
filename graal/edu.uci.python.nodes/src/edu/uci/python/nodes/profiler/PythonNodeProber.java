package edu.uci.python.nodes.profiler;

import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;

public class PythonNodeProber implements ASTNodeProber {

    private final PythonContext context;

    public PythonNodeProber(PythonContext context) {
        this.context = context;
    }

    public Node probeAs(Node astNode, PhylumTag tag, Object... args) {
        return astNode;
    }

    public PNode probeAsStatement(PNode node) {
        PythonWrapperNode wrapper;
        if (node instanceof PythonWrapperNode) {
            wrapper = (PythonWrapperNode) node;
        } else {
            wrapper = new PythonWrapperNode(context, node);
            wrapper.getProbe().tagAs(PhylumTag.STATEMENT);
            wrapper.clearSourceSection();
            wrapper.assignSourceSection(node.getSourceSection());
        }

        wrapper.getProbe().addInstrument(new ProfilerInstrument());
        return wrapper;
    }
}
