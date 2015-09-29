package edu.uci.python.nodes.instruments;

import com.oracle.truffle.api.instrument.ASTProber;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeVisitor;

public class PythonStandardASTProber implements NodeVisitor, ASTProber {

    public void probeAST(Node node) {
        node.accept(this);
    }

    public boolean visit(Node node) {
        // TODO: rebase gulfam work here
        return false;
    }

}
