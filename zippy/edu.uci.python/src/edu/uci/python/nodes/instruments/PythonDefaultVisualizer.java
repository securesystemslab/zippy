package edu.uci.python.nodes.instruments;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.instrument.ASTPrinter;
import com.oracle.truffle.api.instrument.impl.DefaultVisualizer;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;

import edu.uci.python.nodes.call.CallDispatchNode;
import edu.uci.python.nodes.function.BuiltinFunctionRootNode;
import edu.uci.python.nodes.function.FunctionRootNode;
import edu.uci.python.runtime.datatype.PNone;

public class PythonDefaultVisualizer extends DefaultVisualizer {

    private final PythonASTPrinter astPrinter;

    public PythonDefaultVisualizer() {
        this.astPrinter = new PythonASTPrinter();
    }

    @Override
    public ASTPrinter getASTPrinter() {
        return astPrinter;
    }

    @Override
    public String displayMethodName(Node node) {

        if (node == null) {
            return null;
        }
        RootNode root = node.getRootNode();
        if (root instanceof FunctionRootNode) {
            FunctionRootNode functionRootNode = (FunctionRootNode) root;
            return functionRootNode.getFunctionName();
        } else if (root instanceof BuiltinFunctionRootNode) {
            BuiltinFunctionRootNode bfunctionRootNode = (BuiltinFunctionRootNode) root;
            return bfunctionRootNode.getFunctionName();
        }

        return "unknown";
    }

    @Override
    public String displayCallTargetName(CallTarget callTarget) {
        if (callTarget instanceof RootCallTarget) {
            final RootCallTarget rootCallTarget = (RootCallTarget) callTarget;
            CallDispatchNode callnode = (CallDispatchNode) rootCallTarget.getRootNode().getParent();
            return callnode.toString();
        }
        return callTarget.toString();
    }

    @Override
    public String displayValue(Object value, int trim) {
        if (value == null || value == PNone.NONE) {
            return "None";
        }
        return trim(value.toString(), trim);
    }

    @Override
    public String displayIdentifier(FrameSlot slot) {

        final Object id = slot.getIdentifier();
        return id.toString();
    }
}
