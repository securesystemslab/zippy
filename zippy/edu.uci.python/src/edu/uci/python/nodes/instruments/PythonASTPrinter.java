package edu.uci.python.nodes.instruments;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.oracle.truffle.api.instrument.InstrumentationNode;
import com.oracle.truffle.api.instrument.impl.DefaultASTPrinter;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeClass;
import com.oracle.truffle.api.nodes.NodeFieldAccessor;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.NodeFieldAccessor.NodeFieldKind;

public class PythonASTPrinter extends DefaultASTPrinter {

    public PythonASTPrinter() {
    }

    @Override
    protected void printTree(PrintWriter p, Node node, int maxDepth, Node markNode, int level) {
        if (node == null) {
            p.print("null");
            return;
        }

        p.print(nodeName(node));

        p.print("(");

        if (node instanceof InstrumentationNode) {
            p.print(instrumentInfo((InstrumentationNode) node));
        }

        p.print(sourceInfo(node));

        p.print(NodeUtil.printSyntaxTags(node));

        ArrayList<NodeFieldAccessor> childFields = new ArrayList<>();

        for (NodeFieldAccessor field : NodeClass.get(node.getClass()).getFields()) {
            if (field.getKind() == NodeFieldKind.CHILD || field.getKind() == NodeFieldKind.CHILDREN) {
                childFields.add(field);
            } else if (field.getKind() == NodeFieldKind.DATA) {
                // p.print(sep);
                // sep = ", ";
                //
                // final String fieldName = field.getName();
                // switch (fieldName) {
                //
                // }
                // p.print(fieldName);
                // p.print(" = ");
                // p.print(field.loadValue(node));
            }
        }
        p.print(")");

        if (level <= maxDepth) {

            if (childFields.size() != 0) {
                p.print(" {");
                for (NodeFieldAccessor field : childFields) {

                    Object value = field.loadValue(node);
                    if (value == null) {
                        printNewLine(p, level);
                        p.print(field.getName());
                        p.print(" = null ");
                    } else if (field.getKind() == NodeFieldKind.CHILD) {
                        printChild(p, maxDepth, markNode, level, field, value);
                    } else if (field.getKind() == NodeFieldKind.CHILDREN) {
                        printChildren(p, maxDepth, markNode, level, field, value);
                    } else {
                        printNewLine(p, level);
                        p.print(field.getName());
                    }
                }
                printNewLine(p, level - 1);
                p.print("}");
            }
        }
    }
}
