package edu.uci.python.nodes.interop;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.Source;

import edu.uci.python.builtins.Builtin;
import edu.uci.python.builtins.PythonBuiltins;
import edu.uci.python.nodes.function.PythonBuiltinNode;

public final class InteropNodes extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return InteropNodesFactory.getFactories();
    }

    @Builtin(name = "evalFile", hasFixedNumOfArguments = true, fixedNumOfArguments = 2)
    @GenerateNodeFactory
    public abstract static class EvalFileNode extends PythonBuiltinNode {
        @Specialization
        @TruffleBoundary
        public Object evalFile(String mimeType, String fileName) {
            try {
                final Source sourceObject = Source.newBuilder(new File(fileName)).mimeType(mimeType).build();

                return getContext().getEnv().parse(sourceObject).call();

            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Builtin(name = "importSymbol", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    @GenerateNodeFactory
    public abstract static class ExportNode extends PythonBuiltinNode {
        @Specialization
        @TruffleBoundary
        public Object importSymbol(String name) {
            Object object = getContext().getEnv().importSymbol(name);

            if (object == null) {
                throw new IllegalStateException("Uninitialized global");
            }

            return object;
        }
    }
}
