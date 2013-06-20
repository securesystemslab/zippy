package org.python.ast.nodes.statements;

import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.frame.*;

public class PrintNode extends StatementNode {

    @Child
    TypedNode[] values;

    private final boolean nl;

    private StringBuilder out = null;

    public PrintNode(TypedNode[] values, boolean nl) {
        this.values = adoptChildren(values);
        this.nl = nl;
    }

    public void setOutStream(StringBuilder out) {
        this.out = out;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            TypedNode e = values[i];
            sb.append(e.executeGeneric(frame) + " ");
        }

        if (nl) {
            sb.append(System.getProperty("line.separator"));
        }

        System.out.print(sb.toString());

        if (out != null) {
            out.append(sb.toString());
        }
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
        for (TypedNode val : values) {
            val.visualize(level);
        }
    }

}
