package org.python.util;

import org.python.antlr.BaseParser;
import org.python.core.CompileMode;
import org.python.core.Options;
import org.python.core.Py;
import org.python.core.PyStringMap;
import org.python.core.truffle.ASTInterpreter;

import com.oracle.truffle.api.nodes.RootNode;

public class ZippyInteractiveConsole extends InteractiveConsole {
	
    public void execfile(java.io.InputStream s, String name, boolean timeRuntime) {
        setSystemState();

        long start = Options.startTimer(Options.timeRun);

        if (Options.interpretAST) {

            // Truffle
            ASTInterpreter.init((PyStringMap) getLocals(), Options.debug);
            RootNode root = Py.parseToAST(s, name, CompileMode.exec, cflags);
            BaseParser.visualizeAST(root, "Before Specialization");

            ASTInterpreter.interpret(root, false);

            if (Options.specialize) {
                BaseParser.visualizeAST(root, "After Specialization");
            }
        } else {
            Py.runCode(Py.compile_flags(s, name, CompileMode.exec, cflags), null, getLocals());
        }

        Options.stopTimer(Options.timeRun, start, Options.TIMER.RUN);
        Options.printTimes();
        Py.flushLine();
    }

}
