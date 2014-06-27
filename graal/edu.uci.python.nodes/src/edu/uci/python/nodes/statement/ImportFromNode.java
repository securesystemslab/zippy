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

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.standardtype.*;

public class ImportFromNode extends PNode {

    private final PythonContext context;
    private final String[] fromModules;
    private final String importee;
    private final PythonModule relativeto;

    public ImportFromNode(PythonContext context, PythonModule relativeto, String fromModules, String importee) {
        this.context = context;
        this.fromModules = fromModules.split("\\.");
        this.importee = importee;
        this.relativeto = relativeto;
        assert this.fromModules != null && this.fromModules.length > 0;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        CompilerAsserts.neverPartOfCompilation();

        Object current = relativeto;

        for (int i = 0; i < fromModules.length; i++) {
            String importModuleName = fromModules[i];

            if (importModuleName.compareTo("") == 0) {
                return context.getImportManager().importModule((PythonModule) current, importee);
            } else {
                current = context.getImportManager().importModule((PythonModule) current, importModuleName);
            }
        }

        return doImportFrom(current);
    }

    private Object doImportFrom(Object importedModule) {
        try {
            if (importedModule instanceof PythonModule) {
                PythonModule module = (PythonModule) importedModule;
                Object attr = module.getAttribute(importee);

                if (attr == PNone.NONE) {
                    attr = context.getImportManager().importModule(module, importee);
                }

                return attr;
            } else {
                return ((PyObject) importedModule).__getattr__(importee);
            }
        } catch (PyException pye) {
            if (pye.match(Py.AttributeError)) {
                throw Py.ImportError(String.format("cannot import name %.230s", importee));
            } else {
                throw pye;
            }
        }
    }

}
