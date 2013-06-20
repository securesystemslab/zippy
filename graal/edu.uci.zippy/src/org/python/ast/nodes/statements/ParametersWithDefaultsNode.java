package org.python.ast.nodes.statements;

import java.util.Arrays;
import java.util.List;

import org.python.ast.datatypes.PArguments;
import org.python.ast.nodes.LeftHandSideNode;
import org.python.ast.nodes.TypedNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ParametersWithDefaultsNode extends ParametersWithNoDefaultsNode {

    @Children
    TypedNode[] defaults;

    Object[] evaluatedDefaults;

    public ParametersWithDefaultsNode(LeftHandSideNode[] parameters, TypedNode[] defaults, List<String> paramNames) {
        super(parameters, paramNames);
        this.defaults = adoptChildren(defaults);
    }

    @Override
    public void evaluateDefaults(VirtualFrame frame) {
        Object[] evaluated = new Object[defaults.length];

        int index = 0;
        for (int i = 0; i < defaults.length; i++) {
            evaluated[index++] = defaults[i].executeGeneric(frame);
        }

        evaluatedDefaults = evaluated;
    }

    /**
     * invoked when CallTarget is called, applies runtime arguments to the newly
     * created VirtualFrame.
     */
    @Override
    public void executeVoid(VirtualFrame frame) {
        PArguments args = (PArguments) frame.getArguments();
        Object[] values = args.getArgumentsArray();

        // update defaults
        int offset = parameters.length - evaluatedDefaults.length;
        for (int i = 0; i < evaluatedDefaults.length; i++) {
            parameters[offset].doLeftHandSide(frame, evaluatedDefaults[i]);
            offset++;
        }

        // update parameters
        int valLen = values.length;
        int paramLen = parameters.length;
        int size = paramLen > valLen ? valLen : paramLen;
        for (int i = 0; i < size; i++) {
            Object val = values[i];

            if (val != null) {
                parameters[i].doLeftHandSide(frame, val);
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + Arrays.toString(parameters) + ", " + Arrays.toString(defaults) + ")";
    }

}
