package org.python.ast.nodes.statements;

import java.util.*;

import com.oracle.truffle.api.frame.*;

public class ParametersNode extends StatementNode {

    public static ParametersNode EMPTY_PARAMS = new ParametersNode(null);

    protected List<String> parameterNames;

    public ParametersNode(List<String> paramNames) {
        this.parameterNames = paramNames;
    }

    public int indexOf(String name) {
        return parameterNames.indexOf(name);
    }

    public int size() {
        return parameterNames.size();
    }

    public void evaluateDefaults(VirtualFrame frame) {}

    @Override
    public void executeVoid(VirtualFrame frame) {}

}
