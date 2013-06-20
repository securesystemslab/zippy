package org.python.ast.nodes;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.statements.*;
import org.python.ast.utils.*;

import com.oracle.truffle.api.frame.*;

public class GeneratorRootNode extends FunctionRootNode {

    StatementNode resumingNode = null;

    VirtualFrame frame;

    public GeneratorRootNode(ParametersNode parameters, StatementNode body) {
        super(parameters, body);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        parameters.executeVoid(frame);
        resumingNode = body;
        this.frame = frame;
        return new PGenerator(this);
    }

    public Object next() throws ImplicitReturnException {
        StatementNode current = resumingNode;

        while (current != null) {
            try {
                current.executeVoid(frame);
                current = current.next();
            } catch (ExplicitYieldException eye) {
                resumingNode = eye.getResumingNode();
                return eye.getValue();
            }
        }

        throw new ImplicitReturnException();
    }
}
