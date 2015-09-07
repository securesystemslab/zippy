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
import java.nio.file.Path;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.debug.*;
import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.vm.*;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.*;
import edu.uci.python.parser.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

@TruffleLanguage.Registration(name = "Python", version = "3.3", mimeType = "application/x-python")
public class PythonLanguage extends TruffleLanguage<PythonContext> {

    public static final PythonLanguage INSTANCE = new PythonLanguage();

    // TODO: myq re-do this class

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
        // TODO: Add Python Debugger Support
        return null;
    }

    public static void printBanner(String phase) {
        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check
    }

    public static void run(TruffleVM context, Path path, String[] args) throws IOException {
        TruffleVM vm = TruffleVM.newVM().build();
        assert vm.getLanguages().containsKey("application/x-python");

        Source src = Source.fromFileName(path.toString());
        PyString pypath = new PyString(System.getProperty("user.dir"));
        Py.getSystemState().path.insert(0, pypath);

        CallTarget result = null;
        result = (CallTarget) context.eval(src.withMimeType("application/x-python")).get();

        result.call(PArguments.empty());

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
    protected PythonContext createContext(com.oracle.truffle.api.TruffleLanguage.Env env) {
        return new PythonContext(new PythonOptions(), new PythonDefaultBuiltinsLookup(), new PythonParserImpl());
    }

}
