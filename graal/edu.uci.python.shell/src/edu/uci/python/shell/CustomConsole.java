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

import org.python.core.*;
import org.python.util.*;

import com.oracle.truffle.api.*;

import edu.uci.python.builtins.*;
import edu.uci.python.parser.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.standardtype.*;

public class CustomConsole extends JLineConsole {

    @Override
    public void execfile(java.io.InputStream s, String name) {
        PythonParser parser = new PythonParserImpl();
        PythonContext context = new PythonContext(new PythonOptions(), new PythonDefaultBuiltinsLookup(), parser);

        try {
            Source source = context.getSourceManager().get(name);
            execfile(context, source);
        } finally {
            context.shutdown();
        }
    }

    public void execfile(PythonContext context, Source source) {
        setSystemState();

        PythonModule module = context.createMainModule();
        PythonParseResult result = context.getParser().parse(context, module, source, CompileMode.exec, cflags);

        if (PythonOptions.PrintAST) {
            printBanner("Before Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        ASTInterpreter.interpret(result);

        if (PythonOptions.PrintAST) {
            printBanner("After Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        if (PythonOptions.ProfileGeneratorCalls) {
            if (PythonOptions.ParallelizeGeneratorCalls) {
                PParallelGenerator.printProfiledTime();
            } else {
                PGenerator.printProfiledTime();
            }
        }

        Py.flushLine();
    }

    public void parseFile(PythonContext context, Source source) {
        PythonModule module = context.createMainModule();
        PythonParseResult result = context.getParser().parse(context, module, source, CompileMode.exec, cflags);

        if (PythonOptions.PrintAST) {
            printBanner("After Parsing");
            result.printAST();
        }
    }

    public static void printBanner(String phase) {
        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check
    }
}
