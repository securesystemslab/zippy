package org.python.ast.nodes.statements;

import java.util.*;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.*;

import com.oracle.truffle.api.frame.*;

public class ParametersOfSizeOneNode extends ParametersNode {

    @Children
    protected LeftHandSideNode parameter;

    public ParametersOfSizeOneNode(List<String> paramNames, LeftHandSideNode parameter) {
        super(paramNames);
        this.parameter = adoptChild(parameter);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        PArguments args = (PArguments) frame.getArguments();
        Object[] values = args.getArgumentsArray();
        parameter.doLeftHandSide(frame, values[0]);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + parameter + ")";
    }

}
