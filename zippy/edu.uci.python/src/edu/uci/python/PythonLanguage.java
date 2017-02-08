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

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;

import edu.uci.python.builtins.PythonDefaultBuiltinsLookup;
import edu.uci.python.nodes.PNode;
import edu.uci.python.parser.PythonParserImpl;
import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.function.PFunction;

@TruffleLanguage.Registration(name = "Python", version = "3.3", mimeType = PythonLanguage.MIME_TYPE)
public class PythonLanguage extends TruffleLanguage<PythonContext> {

    public static final String MIME_TYPE = "application/x-python";
    public static final String EXTENSION = ".py";

    public static final PythonLanguage INSTANCE = new PythonLanguage();

    @Override
    protected PythonContext createContext(Env env) {
        return new PythonContext(new PythonOptions(), new PythonDefaultBuiltinsLookup(), new PythonParserImpl());
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

    public Node unprotectedCreateFindContextNode() {
        return super.createFindContextNode();
    }

    public PythonContext unprotectedFindContext(Node node) {
        return super.findContext(node);
    }

}
