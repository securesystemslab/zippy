package org.python.ast.nodes.statements;

import java.util.*;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.*;

import com.oracle.truffle.api.frame.*;

public class ParametersOfSizeTwoNode extends ParametersNode {

    @Children
    protected LeftHandSideNode param0;

    @Children
    protected LeftHandSideNode param1;

    public ParametersOfSizeTwoNode(List<String> paramNames, LeftHandSideNode param0, LeftHandSideNode param1) {
        super(paramNames);
        this.param0 = adoptChild(param0);
        this.param1 = adoptChild(param1);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        PArguments args = (PArguments) frame.getArguments();
        Object[] values = args.getArgumentsArray();
        param0.doLeftHandSide(frame, values[0]);
        param1.doLeftHandSide(frame, values[1]);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + param0 + ", " + param1 + ")";
    }

}
