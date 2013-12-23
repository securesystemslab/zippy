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
package edu.uci.python.nodes.statement;

import java.io.*;

import org.python.core.*;
import org.python.core.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.source.*;
import edu.uci.python.runtime.standardtype.*;

public class ImportNode extends PNode {

    private final PythonContext context;

    private final String fromModuleName;

    private final String importee;

    public ImportNode(PythonContext context, String fromModule, String importee) {
        this.context = context;
        this.fromModuleName = fromModule;
        this.importee = importee;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object importedModule = null;

        if (fromModuleName != null) {
            importedModule = importModule(frame, fromModuleName);
        }

        return doImport(frame, importedModule, importee);
    }

    private Object doImport(VirtualFrame frame, Object importedModule, String name) {
        try {
            if (importedModule != null && importedModule instanceof PythonModule) {
                return ((PythonModule) importedModule).getAttribute(name);
            } else if (importedModule != null) {
                return ((PyObject) importedModule).__getattr__(name);
            } else {
                return importModule(frame, name);
            }
        } catch (PyException pye) {
            if (pye.match(Py.AttributeError)) {
                throw Py.ImportError(String.format("cannot import name %.230s", name));
            } else {
                throw pye;
            }
        }
    }

    private Object importModule(VirtualFrame frame, String name) {
        Object importedModule = context.getPythonBuiltinsLookup().lookupModule(name);
        PythonParseResult result = null;
        CallTarget callTarget = null;
        PythonContext moduleContext = null;
        if (importedModule == null) {

            FileInputStream inputStream;
            try {
                String filename = name + ".py";
                String path = context.getSourceManager().getPath();
                inputStream = new FileInputStream(new RelativeFile(path + File.separatorChar + filename));
                SourceManager sourceManager = new SourceManager(path, filename, inputStream);
                moduleContext = new PythonContext(context, sourceManager);
                importedModule = result = context.getParser().parse(moduleContext, CompileMode.exec, CompilerFlags.getCompilerFlags());

                inputStream.close();
            } catch (IOException e) {
                // do nothing and jython's importer will fix it.
            }

            if (importedModule != null) {
                callTarget = Truffle.getRuntime().createCallTarget(result.getModuleRoot(), frame.getFrameDescriptor());
                callTarget.call(null, new PArguments(null));
                moduleContext = ((PythonParseResult) importedModule).getContext();
                PythonModule module = moduleContext.getPythonBuiltinsLookup().lookupModule("__main__");
                importedModule = new PythonModule(importee, module);
            } else {
                /*
                 * This should be removed the soon we can import any module
                 */
                importedModule = __builtin__.__import__(name);
            }
        }

        return importedModule;
    }
}
