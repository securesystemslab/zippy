package edu.uci.python.parser;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.profiler.*;
import edu.uci.python.nodes.subscript.*;
import edu.uci.python.runtime.*;

public class ProfilerTranslator implements NodeVisitor {

    private final PythonNodeProber astProber;

    public ProfilerTranslator(PythonContext context) {
        this.astProber = new PythonNodeProber(context);
    }

    public void translate(PythonParseResult parseResult, RootNode root) {
        root.accept(this);

        for (RootNode functionRoot : parseResult.getFunctionRoots()) {
            functionRoot.accept(this);
        }
    }

    @Override
    public boolean visit(Node node) {
        if (node instanceof ListLiteralNode) {
            createWrapperNode((PNode) node);
        } else if (node instanceof TupleLiteralNode) {
            createWrapperNode((PNode) node);
        } else if (node instanceof SetLiteralNode) {
            createWrapperNode((PNode) node);
        } else if (node instanceof DictLiteralNode) {
            createWrapperNode((PNode) node);
        } else if (node instanceof SubscriptLoadIndexNode) {
            createWrapperNode((PNode) node);
        } else if (node instanceof SubscriptStoreIndexNode) {
            createWrapperNode((PNode) node);
        }
        return true;
    }

    private PythonWrapperNode createWrapperNode(PNode node) {
        PythonWrapperNode wrapperNode = astProber.probeAsStatement(node);
        node.replace(wrapperNode);
        wrapperNode.adoptChildren();
        return wrapperNode;
    }
}
