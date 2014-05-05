package edu.uci.python.parser;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.profiler.*;
import edu.uci.python.nodes.subscript.*;
import edu.uci.python.runtime.*;

public class ProfilerTranslator implements NodeVisitor {

    private final PythonNodeProber astProber;
    private final PythonContext context;

    public ProfilerTranslator(PythonContext context) {
        this.context = context;
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
// SubscriptLoadIndexNode subscritLoadIndexNode = (SubscriptLoadIndexNode) node;
// WrapperNode wrapperNode = new WrapperNode(subscritLoadIndexNode);
// subscritLoadIndexNode.replace(wrapperNode);
        } else if (node instanceof SubscriptStoreIndexNode) {
// SubscriptStoreIndexNode subscriptStoreIndexNode = (SubscriptStoreIndexNode) node;
// WrapperNode wrapperNode = new WrapperNode(subscriptStoreIndexNode);
// subscriptStoreIndexNode.replace(wrapperNode);
        }
        return true;
    }

    private void createWrapperNode(PNode node) {
        PNode wrapperNode = astProber.probeAsStatement(node);
        node.replace(wrapperNode);
        wrapperNode.adoptChildren();
    }
}
