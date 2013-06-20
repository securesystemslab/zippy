package org.python.core.truffle;

import org.python.antlr.base.mod;
import org.python.ast.datatypes.PArguments;
import org.python.ast.nodes.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

public class ASTInterpreter {

    public static boolean debug;

    public static void init(PyStringMap globals, boolean debug) {
        GlobalScope.create(globals);
        ASTInterpreter.debug = debug;
    }

    public static void interpret(RootNode rootNode, boolean log) {
        CallTarget module;

        if (Options.specialize) {
            ModuleNode root = (ModuleNode) rootNode;
            module = Truffle.getRuntime().createCallTarget(root, root.getFrameDescriptor());
        } else {
            mod root = (mod) rootNode;
            module = Truffle.getRuntime().createCallTarget(root, root.getFrameDescriptor());
        }

        Arguments arguments = new PArguments();

        long start = System.nanoTime();
        module.call(null, arguments);
        long end = System.nanoTime();

        if (log) {
            System.out.printf("== iteration %d: %.3f ms\n", (0), (end - start) / 1000000.0);
        }
    }

    public static void trace(String message) {
        if (!ASTInterpreter.debug) {
            return;
        }

        System.out.println(message);
    }

}
