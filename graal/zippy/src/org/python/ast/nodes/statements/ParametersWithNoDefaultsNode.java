package org.python.ast.nodes.statements;

import java.util.Arrays;
import java.util.List;

import org.python.ast.datatypes.PArguments;
import org.python.ast.nodes.LeftHandSideNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ParametersWithNoDefaultsNode extends ParametersNode {

    @Children
    protected LeftHandSideNode[] parameters;

    public ParametersWithNoDefaultsNode(LeftHandSideNode[] arguments, List<String> paramNames) {
        super(paramNames);
        this.parameters = adoptChildren(arguments);
    }

    /**
     * invoked by FunctionRootNode after new Frame is created. It applies
     * runtime arguments to the newly created VirtualFrame.
     */
    @Override
    public void executeVoid(VirtualFrame frame) {
        PArguments args = (PArguments) frame.getArguments();
        Object[] values = args.getArgumentsArray();

        for (int i = 0; i < parameters.length; i++) {
            parameters[i].doLeftHandSide(frame, values[i]);
        }
    }
    
    /*@Override
    public <R> R accept(PNodeVisitor<R> visitor) {
        return visitor.visitParametersDefaultNode(this);
    }*/

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + Arrays.toString(parameters) + ")";
    }

}
