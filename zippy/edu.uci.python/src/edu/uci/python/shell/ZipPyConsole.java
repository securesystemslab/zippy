/*
 * Copyright (c) 2013, Regents of the University of California
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
package edu.uci.python.shell;

import java.io.File;
import java.io.IOException;

import org.python.core.Py;
import org.python.util.InteractiveConsole;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.Source.Builder;

import edu.uci.python.PythonLanguage;
import edu.uci.python.builtins.PythonDefaultBuiltinsLookup;
import edu.uci.python.nodes.ModuleNode;
import edu.uci.python.parser.PythonParserImpl;
import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.PythonParseResult;
import edu.uci.python.runtime.PythonParser;
import edu.uci.python.runtime.function.PArguments;
import edu.uci.python.runtime.object.PythonObjectAllocationInstrumentor;
import edu.uci.python.runtime.standardtype.PythonModule;

public class ZipPyConsole extends InteractiveConsole {

    @Override
    public void execfile(java.io.InputStream s, String name) {
        PythonParser parser = new PythonParserImpl();
        PythonContext context = null;

        try {
            Builder<IOException, RuntimeException, RuntimeException> builder = Source.newBuilder(new File(name));
            builder.mimeType(PythonLanguage.MIME_TYPE);
            Source source = builder.build();
            context = new PythonContext(new PythonOptions(), new PythonDefaultBuiltinsLookup(), parser);
            execfile(context, source);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    public PythonParseResult execfile(PythonContext context, Source source) {
        setSystemState();

        PythonModule module = context.createMainModule(source.getPath());
        PythonParseResult result = context.getParser().parse(context, module, source);

        if (PythonOptions.PrintAST) {
            printBanner("Before Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        ModuleNode root = (ModuleNode) result.getModuleRoot();
        RootCallTarget moduleCallTarget = Truffle.getRuntime().createCallTarget(root);

        moduleCallTarget.call(PArguments.empty());

        if (PythonOptions.PrintAST) {
            printBanner("After Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        if (PythonOptions.InstrumentObjectStorageAllocation) {
            PythonObjectAllocationInstrumentor.getInstance().printAllocations();
        }

        Py.flushLine();
        return result;
    }

    public PythonParseResult parseFile(PythonContext context, Source source) {
        PythonModule module = context.createMainModule(source.getPath());
        PythonParseResult result = context.getParser().parse(context, module, source);

        if (PythonOptions.PrintAST) {
            printBanner("After Parsing");
            result.printAST();
        }

        return result;
    }

    public static void printBanner(String phase) {
        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check
    }
}
