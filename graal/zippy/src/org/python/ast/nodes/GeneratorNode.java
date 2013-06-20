package org.python.ast.nodes;

import org.python.ast.nodes.statements.*;
import org.python.ast.utils.*;

import com.oracle.truffle.api.frame.*;

public class GeneratorNode extends FunctionRootNode {

    public GeneratorNode(ParametersNode parameters, StatementNode body, String name) {
        super(parameters, body);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        parameters.executeVoid(frame);

        try {
            body.executeVoid(frame);
        } catch (ExplicitReturnException ere) {
            return ere.getValue();
        } catch (ImplicitReturnException ire) {

        }

        return null;
    }

}
