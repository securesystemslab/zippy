/*
 * Copyright (c) 2015, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python;

import java.io.IOException;
import java.io.InputStreamReader;

import org.python.core.Py;
import org.python.core.PyString;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrument.ASTProber;
import com.oracle.truffle.api.instrument.AdvancedInstrumentResultListener;
import com.oracle.truffle.api.instrument.AdvancedInstrumentRootFactory;
import com.oracle.truffle.api.instrument.Visualizer;
import com.oracle.truffle.api.instrument.WrapperNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;

import edu.uci.python.builtins.PythonDefaultBuiltinsLookup;
import edu.uci.python.nodes.ModuleNode;
import edu.uci.python.nodes.PNode;
import edu.uci.python.nodes.instruments.PythonDefaultVisualizer;
import edu.uci.python.nodes.statement.StatementNode;
import edu.uci.python.parser.PythonParserImpl;
import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.PythonParseResult;
import edu.uci.python.runtime.function.PFunction;
import edu.uci.python.runtime.standardtype.PythonModule;

@TruffleLanguage.Registration(name = "Python", version = "3.3", mimeType = "application/x-python")
public class PythonLanguage extends TruffleLanguage<PythonContext> {

    private static Visualizer visualizer = new PythonDefaultVisualizer();
    private static ASTProber registeredASTProber; // non-null if prober already registered

    public static final PythonLanguage INSTANCE = new PythonLanguage();

    @Override
    protected PythonContext createContext(Env env) {
        return new PythonContext(new PythonOptions(), new PythonDefaultBuiltinsLookup(), new PythonParserImpl());
    }

    public static void main(String[] args) throws IOException {
        PolyglotEngine vm = PolyglotEngine.buildNew().build();
        assert vm.getLanguages().containsKey("application/x-python");

        PyString path = new PyString(System.getProperty("user.dir"));
        Py.getSystemState().path.insert(0, path);

        int repeats = 1;
        if (args.length >= 2) {
            repeats = Integer.parseInt(args[1]);
        }

        Source source;
        if (args.length == 0) {
            source = Source.fromReader(new InputStreamReader(System.in), "<stdin>").withMimeType("application/x-python");
        } else {
            source = Source.fromFileName(args[0]);
        }
        Value s = vm.eval(source);
        while (repeats-- > 0) {
            s.invoke(null);
        }

// result.call(PArguments.empty());

        // TODO: fix prints
        if (PythonOptions.PrintAST) {
            printBanner("After Specialization");
// result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
// result.visualizeToNetwork();
        }

        Py.flushLine();
    }

    @Override
    protected CallTarget parse(Source code, Node node, String... argumentNames) throws IOException {
        PythonContext context = new PythonContext(new PythonOptions(), new PythonDefaultBuiltinsLookup(), new PythonParserImpl());
        PythonModule module = context.createMainModule(code.getPath());
        PythonParseResult result = context.getParser().parse(context, module, code);
        if (PythonOptions.PrintAST) {
            printBanner("Before Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        ModuleNode root = (ModuleNode) result.getModuleRoot();
        RootCallTarget moduleCallTarget = Truffle.getRuntime().createCallTarget(root);
        return moduleCallTarget;
    }

    @Override
    protected Object findExportedSymbol(PythonContext context, String globalName, boolean onlyExplicit) {
        for (PFunction f : context.getFunctionRegistry().getFunctions()) {
            if (globalName.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    @Override
    protected Object getLanguageGlobal(PythonContext context) {
        return context;
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        return object instanceof PNode;
    }

    public static void printBanner(String phase) {
        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check
    }

    @Override
    protected Visualizer getVisualizer() {
        if (visualizer == null) {
            visualizer = new PythonDefaultVisualizer();
        }
        return visualizer;
    }

    @Override
    protected boolean isInstrumentable(Node node) {
        return node instanceof StatementNode;
    }

    @Override
    protected WrapperNode createWrapperNode(Node node) {
        if (node instanceof StatementNode) {
// return new StatementWrapperNode((StatementNode) node);
        }
        return null;
    }

    @Override
    protected Object evalInContext(Source source, Node node, MaterializedFrame mFrame) throws IOException {
        throw new IllegalStateException("evalInContext not supported in this language: Python");
    }

    @Override
    protected AdvancedInstrumentRootFactory createAdvancedInstrumentRootFactory(String expr, AdvancedInstrumentResultListener resultListener) throws IOException {
        throw new IllegalStateException("createAdvancedInstrumentRootFactory not supported in this language: Python");
    }
}
