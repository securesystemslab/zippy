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

import org.python.core.Py;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;

import edu.uci.python.builtins.PythonDefaultBuiltinsLookup;
import edu.uci.python.nodes.ModuleNode;
import edu.uci.python.nodes.PNode;
import edu.uci.python.parser.PythonParserImpl;
import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.PythonParseResult;
import edu.uci.python.runtime.object.PythonObjectAllocationInstrumentor;
import edu.uci.python.runtime.standardtype.PythonModule;

@TruffleLanguage.Registration(name = PythonLanguage.LANGUAGE_ID, version = "3.3", mimeType = PythonLanguage.MIME_TYPE, interactive = false)
@ProvidedTags({StandardTags.CallTag.class, StandardTags.StatementTag.class, StandardTags.RootTag.class, DebuggerTags.AlwaysHalt.class})
public final class PythonLanguage extends TruffleLanguage<PythonContext> {

    public static final String LANGUAGE_ID = "python";
    public static final String MIME_TYPE = "application/x-python";
    public static final String EXTENSION = ".py";

    public static PythonLanguage INSTANCE;

    private PythonParseResult parseResult;

    public PythonLanguage() {
        INSTANCE = this;
        this.parseResult = null;
    }

    @Override
    protected PythonContext createContext(Env env) {
        PythonOptions opts = new PythonOptions();
        opts.setStandardOut(env.out());
        opts.setStandardErr(env.err());
        return new PythonContext(env, opts, new PythonDefaultBuiltinsLookup(), new PythonParserImpl());
    }

    @Override
    protected CallTarget parse(ParsingRequest request) throws Exception {
        PythonContext context = this.getContextReference().get();
        PythonModule module = context.createMainModule(request.getSource().getPath());
        parseResult = context.getParser().parse(context, module, request.getSource());

        if (PythonOptions.PrintAST) {
            System.out.println("============= " + "Before Specialization" + " ============= ");
            parseResult.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            parseResult.visualizeToNetwork();
        }

        ModuleNode root = (ModuleNode) parseResult.getModuleRoot();
        return Truffle.getRuntime().createCallTarget(root);
    }

    @Override
    protected void disposeContext(PythonContext context) {
        if (parseResult == null)
            return;

        if (PythonOptions.PrintAST) {
            System.out.println("============= " + "After Specialization" + " ============= ");
            parseResult.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            parseResult.visualizeToNetwork();
        }

        if (PythonOptions.InstrumentObjectStorageAllocation) {
            PythonObjectAllocationInstrumentor.getInstance().printAllocations();
        }

        Py.flushLine();

    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        return object instanceof PNode;
    }

}
