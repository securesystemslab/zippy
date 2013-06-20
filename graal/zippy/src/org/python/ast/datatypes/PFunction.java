package org.python.ast.datatypes;

import org.python.ast.nodes.statements.*;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.PackedFrame;

public class PFunction extends PCallable {

    private final ParametersNode parameters;

    private final CallTarget callTarget;

    public PFunction(String name, ParametersNode parameters, CallTarget callTarget) {
        super(name);
        this.parameters = parameters;
        this.callTarget = callTarget;
    }

    @Override
    public Object call(PackedFrame caller, Object[] args) {
        return callTarget.call(caller, new PArguments(args));
    }

    @Override
    public Object call(PackedFrame caller, Object[] arguments, Object[] keywords) {
        Object[] combined = new Object[parameters.size()];
        System.arraycopy(arguments, 0, combined, 0, arguments.length);

        // TODO: get rid of cast.
        for (int i = 0; i < keywords.length; i++) {
            PKeyword keyarg = (PKeyword) keywords[i];
            int keywordIdx = parameters.indexOf(keyarg.getName());
            combined[keywordIdx] = keyarg.getValue();
        }

        return callTarget.call(caller, new PArguments(combined));
    }

    /*
     * Specialized
     */
    @Override
    public Object call(PackedFrame caller, Object arg) {
        return callTarget.call(caller, new PArguments(new Object[] { arg }));
    }

    @Override
    public Object call(PackedFrame caller, Object arg0, Object arg1) {
        return callTarget.call(caller, new PArguments(new Object[] { arg0, arg1 }));
    }

}
