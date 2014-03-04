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
package edu.uci.python.parser;

import java.io.*;

import org.python.antlr.base.*;
import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.optimize.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.standardtype.*;

public class PythonParserImpl implements PythonParser {

    /**
     * Truffle: Parse input program to AST that is ready to interpret itself.
     */

    @Override
    public PythonParseResult parse(PythonContext context, PythonModule module, Source source, CompilerFlags cflags) {
        org.python.antlr.base.mod node;
        InputStream istream = new ByteArrayInputStream(source.getCode().getBytes());
        String filename = source.getPath();
        node = ParserFacade.parse(istream, CompileMode.exec, filename, cflags);

        TranslationEnvironment environment = new TranslationEnvironment(context, module);
        ScopeTranslator ptp = new ScopeTranslator(environment);
        node = ptp.process(node);

        PythonTreeTranslator ptt = new PythonTreeTranslator(environment, context);
        PythonParseResult result = ptt.translate(node);

        if (PythonOptions.OptimizeGeneratorExpressions) {
            for (RootNode functionRoot : result.getFunctionRoots()) {
                if (functionRoot instanceof FunctionRootNode) {
                    new GeneratorExpressionOptimizer((FunctionRootNode) functionRoot).optimize();
                }
            }
        }

        return result;
    }

    @Override
    public PythonParseResult parse(PythonContext context, PythonModule module, CompilerFlags cflags) {
        Source source = context.getSourceManager().get(module.getModulePath());
        return parse(context, module, source, cflags);
    }

    @Override
    public PythonParseResult parse(PythonContext context, PythonModule module, String expression) {
        mod node = ParserFacade.parseExpressionOrModule(new StringReader(expression), "<eval>", CompilerFlags.getCompilerFlags());

        TranslationEnvironment environment = new TranslationEnvironment(context, module);
        ScopeTranslator ptp = new ScopeTranslator(environment);
        node = ptp.process(node);

        PythonTreeTranslator ptt = new PythonTreeTranslator(environment, context);
        return ptt.translate(node);
    }

}
