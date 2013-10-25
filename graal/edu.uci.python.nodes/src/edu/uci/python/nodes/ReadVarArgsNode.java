package edu.uci.python.nodes;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.datatypes.*;

/**
 * @author Gulfem
 */

public class ReadVarArgsNode extends ReadArgumentNode {

    public ReadVarArgsNode(int paramIndex) {
        super(paramIndex);
    }

    @Override
    public final Object[] execute(VirtualFrame frame) {
        return executeObjectArray(frame);
    }

    @Override
    public final Object[] executeObjectArray(VirtualFrame frame) {
        PArguments arguments = frame.getArguments(PArguments.class);
        int index = getIndex();
        if (index >= arguments.getLength()) {
            return PArguments.EMPTY_ARGUMENTS_ARRAY;
        } else {
            Object[] varArgs = new Object[arguments.getLength() - index];
            for (int i = 0; i < varArgs.length; i++) {
                varArgs[i] = arguments.getArgument(i + index);
            }
            return varArgs;
        }
    }
}
