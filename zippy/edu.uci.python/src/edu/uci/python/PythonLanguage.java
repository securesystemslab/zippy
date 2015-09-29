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

import java.io.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.debug.*;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.vm.*;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.*;
import edu.uci.python.nodes.instruments.PythonDefaultVisualizer;
import edu.uci.python.nodes.instruments.PythonStandardASTProber;
import edu.uci.python.parser.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

@TruffleLanguage.Registration(name = "Python", version = "3.3", mimeType = "application/x-python")
public class PythonLanguage extends TruffleLanguage<PythonContext> {

    private static Visualizer visualizer = new PythonDefaultVisualizer();
    private static ASTProber registeredASTProber; // non-null if prober already registered
    private DebugSupportProvider debugSupport;

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

    @Override
    protected ToolSupportProvider getToolSupport() {
        return getDebugSupport();
    }

    @Override
    protected DebugSupportProvider getDebugSupport() {
        if (debugSupport == null) {
            debugSupport = new PythonDebugProvider();
        }
        return debugSupport;
    }

    public static void printBanner(String phase) {
        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check
    }

    private final class PythonDebugProvider implements DebugSupportProvider {

        public PythonDebugProvider() {
            if (registeredASTProber == null) {
                registeredASTProber = new PythonStandardASTProber();
                // This should be registered on the TruffleVM
                Probe.registerASTProber(registeredASTProber);
            }
        }

        public Visualizer getVisualizer() {
            if (visualizer == null) {
                visualizer = new PythonDefaultVisualizer();
            }
            return visualizer;
        }

        public void enableASTProbing(ASTProber prober) {
            if (prober != null) {
                Probe.registerASTProber(prober);
            }
        }

        public Object evalInContext(Source source, Node node, MaterializedFrame mFrame) throws DebugSupportException {
            throw new DebugSupportException("evalInContext not supported in this language");
        }

        public AdvancedInstrumentRootFactory createAdvancedInstrumentRootFactory(String expr, AdvancedInstrumentResultListener resultListener) throws DebugSupportException {
            throw new DebugSupportException("createAdvancedInstrumentRootFactory not supported in this language");
        }

    }
}

