package org.python.ast.nodes.statements;

import org.python.ast.datatypes.PFunction;
import org.python.ast.nodes.FunctionRootNode;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.*;

public class FunctionDefNode extends StatementNode {

    private final FrameSlot slot;

    private final String name;

    @Child
    private final ParametersNode parameters;

    private final CallTarget callTarget;

    @Child
    private final RootNode funcRoot;

    public FunctionDefNode(FrameSlot slot, String name, ParametersNode parameters, CallTarget callTarget, RootNode funcRoot) {
        this.slot = slot;
        this.name = name;
        this.parameters = adoptChild(parameters);
        this.callTarget = callTarget;
        this.funcRoot = adoptChild(funcRoot);
    }

    public String getName() {
        return name;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        parameters.evaluateDefaults(frame);
        PFunction fn = new PFunction(name, parameters, callTarget);
        frame.setObject(slot, fn);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + name + ")" + funcRoot;
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;

        parameters.visualize(level);
        ((FunctionRootNode) funcRoot).visualize(level);
    }

}
