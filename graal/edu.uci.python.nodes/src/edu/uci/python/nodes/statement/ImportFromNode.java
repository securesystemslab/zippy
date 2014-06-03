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

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.standardtype.*;

public class ImportFromNode extends PNode {

    private final PythonContext context;
    private final String moduleName;
    private final String importee;
    private final PythonModule relativeto;

    public ImportFromNode(PythonContext context, PythonModule relativeto, String moduleName, String importee) {
        this.context = context;
        this.moduleName = moduleName;
        this.importee = importee;
        this.relativeto = relativeto;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (moduleName.compareTo("") != 0) {
            Object imported = context.getImportManager().importModule(relativeto, moduleName);
            return doImportFrom(imported);
        } else {
            return context.getImportManager().importModule(relativeto, importee);
        }
    }

    private Object doImportFrom(Object importedModule) {
        try {
            if (importedModule instanceof PythonModule) {
                return ((PythonModule) importedModule).getAttribute(importee);
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
