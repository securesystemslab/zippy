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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.translation.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.Options;

import com.oracle.truffle.api.nodes.*;

public class CustomConsole extends JLineConsole {

    @Override
    public void execfile(java.io.InputStream s, String name) {
        setSystemState();

        ASTInterpreter.init((PyStringMap) getLocals(), false);
        RootNode root = parseToAST(s, name, CompileMode.exec, cflags);

        if (Options.PrintAST) {
            visualizeAST(root, "Before Specialization");
        }

        ASTInterpreter.interpret(root, false);

        if (Options.PrintAST) {
            visualizeAST(root, "After Specialization");
        }

        Py.flushLine();
    }

    /**
     * Truffle: Parse input program to AST that is ready to interpret itself.
     */
    public static RootNode parseToAST(InputStream istream, String filename, CompileMode kind, CompilerFlags cflags) {
        mod node = ParserFacade.parse(istream, kind, filename, cflags);
        TranslationEnvironment environment = new TranslationEnvironment(node);
        PythonTreeProcessor ptp = new PythonTreeProcessor(environment);
        node = ptp.process(node);

        PythonTreeTranslator ptt = new PythonTreeTranslator(environment);
        RootNode rootNode = ptt.translate(node);
        return rootNode;
    }

    public static void visualizeAST(RootNode tree, String phase) {
        if (!Options.PrintAST) {
            return;
        }

        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check

        // There are two ways to visualize an AST.
        ((ModuleNode) tree).visualize(0);
    }

}
