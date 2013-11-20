package edu.uci.python.builtins;

import java.util.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.builtins.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.nodes.*;

/**
 * @author Gulfem
 */

public abstract class PythonBuiltins extends PythonBuiltinsContainer {

    protected abstract List<? extends com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories();

    @Override
    public void initialize() {
        List<com.oracle.truffle.api.dsl.NodeFactory<PythonBuiltinNode>> factories = (List<com.oracle.truffle.api.dsl.NodeFactory<PythonBuiltinNode>>) getNodeFactories();
        if (factories == null) {
            throw new IllegalArgumentException("No factories found. Override getFactories() to resolve this.");
        }

        for (com.oracle.truffle.api.dsl.NodeFactory<PythonBuiltinNode> factory : factories) {
            Builtin builtin = factory.getNodeClass().getAnnotation(Builtin.class);
            PNode[] argsKeywords = createArgumentsList(builtin);
            PythonBuiltinNode builtinNode = factory.createNode(builtin.name(), argsKeywords);

            BuiltinFunctionRootNode rootNode = new BuiltinFunctionRootNode(builtinNode);
            CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);

            if (builtin.isClass()) {
                PBuiltinClass builtinClass;

                if (builtin.hasFixedNumOfArguments()) {
                    builtinClass = new PBuiltinClass(builtin.name(), builtin.fixedNumOfArguments(), builtin.fixedNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(),
                                    builtin.takesVariableArguments(), callTarget);
                } else {
                    builtinClass = new PBuiltinClass(builtin.name(), builtin.minNumOfArguments(), builtin.maxNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(),
                                    builtin.takesVariableArguments(), callTarget);
                }

                setBuiltinClass(builtin.name(), builtinClass);
            } else {
                Arity arity;
                PBuiltinFunction function;

                if (builtin.hasFixedNumOfArguments()) {
                    arity = new Arity(builtin.name(), builtin.fixedNumOfArguments(), builtin.fixedNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(),
                                    builtin.takesVariableArguments(), builtin.keywordNames());

                    function = new PBuiltinFunction(builtin.name(), arity, callTarget);
                } else {
                    arity = new Arity(builtin.name(), builtin.minNumOfArguments(), builtin.maxNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(),
                                    builtin.takesVariableArguments(), builtin.keywordNames());
                    function = new PBuiltinFunction(builtin.name(), arity, callTarget);
                }

                setBuiltinFunction(function.getName(), function);
            }

        }
    }

    private static PNode[] createArgumentsList(Builtin builtin) {
        ArrayList<PNode> args = new ArrayList<>();
        int totalNumOfArgsKeywords = 0;

        if (builtin.hasFixedNumOfArguments()) {
            totalNumOfArgsKeywords = builtin.fixedNumOfArguments();
        } else if (builtin.takesVariableArguments()) {
            totalNumOfArgsKeywords = builtin.minNumOfArguments();
            if (builtin.name().equals("max") || builtin.name().equals("min")) {
                totalNumOfArgsKeywords = totalNumOfArgsKeywords + 1;
            }
        } else {
            totalNumOfArgsKeywords = builtin.maxNumOfArguments();
        }

        for (int i = 0; i < totalNumOfArgsKeywords; i++) {
            args.add(new ReadArgumentNode(i));
        }

        if (builtin.takesVariableArguments()) {
            args.add(new ReadVarArgsNode(args.size()));
        }

        if (builtin.takesKeywordArguments()) {
            if (builtin.takesVariableKeywords()) {
                args.add(new ReadVarKeywordsNode(builtin.keywordNames()));
            } else {
                args.add(new ReadKeywordNode(builtin.keywordNames()[0]));
            }
        }

        PNode[] argsKeywords = args.toArray(new PNode[args.size()]);
        return argsKeywords;
    }
}
