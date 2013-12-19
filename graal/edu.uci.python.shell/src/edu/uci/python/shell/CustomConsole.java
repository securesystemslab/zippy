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

import java.io.*;

import org.python.antlr.base.*;
import org.python.core.*;
import org.python.util.*;

import edu.uci.python.builtins.*;
import edu.uci.python.parser.*;
import edu.uci.python.runtime.*;

public class CustomConsole extends JLineConsole {

    @Override
    public void execfile(java.io.InputStream s, String name) {
        PythonContext context = new PythonContext(new PythonOptions(), new PythonDefaultBuiltins());
        PythonBuiltinsInitializer.initialize(context);
        execfile(s, name, context);
    }

    public void execfile(java.io.InputStream s, String name, PythonContext context) {
        setSystemState();

        ASTInterpreter.init(false);
        PythonParseResult result = parseToAST(s, name, CompileMode.exec, cflags, context);
        if (PythonOptions.PrintAST) {
            printBanner("Before Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        ASTInterpreter.interpret(result, false);

        if (PythonOptions.PrintAST) {
            printBanner("After Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        Py.flushLine();
    }

    /**
     * Truffle: Parse input program to AST that is ready to interpret itself.
     */
    public static PythonParseResult parseToAST(InputStream istream, String filename, CompileMode kind, CompilerFlags cflags, PythonContext context) {

        mod node = null;
        if (!PythonOptions.PrintFunction) {
            // enable printing flag for python's builtin function (v3.x) in parser.
            String print = "from __future__ import print_function \n";
            InputStream printFlag = new ByteArrayInputStream(print.getBytes());
            InputStream withPrintFlag = new SequenceInputStream(printFlag, istream);

            node = ParserFacade.parse(withPrintFlag, kind, filename, cflags);
        } else {
            node = ParserFacade.parse(istream, kind, filename, cflags);
        }
        TranslationEnvironment environment = new TranslationEnvironment(node, context);
        ScopeTranslator ptp = new ScopeTranslator(environment);
        node = ptp.process(node);

        PythonTreeTranslator ptt = new PythonTreeTranslator(environment, context);
        PythonParseResult result = ptt.translate(node);
        return result;
    }

    public static void printBanner(String phase) {
        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check
    }
}
