package org.python.ast.datatypes;

import com.oracle.truffle.api.Arguments;

public class PArguments extends Arguments {

    final Object[] arguments;

    public PArguments() {
        this.arguments = new Object[0];
    }

    public PArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public Object[] getArgumentsArray() {
        return arguments;
    }

}
