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
package edu.uci.python.nodes.statements;

import org.python.core.*;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.modules.*;

public class ImportNode extends StatementNode {

    final String fromModule;

    final FrameSlot[] targetSlots;

    final String[] aliases;

    public ImportNode(FrameSlot[] targetSlots, String fromModule, String[] aliases) {
        this.targetSlots = targetSlots;
        this.fromModule = fromModule;
        this.aliases = aliases;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        Object importedModule = null;

        if (fromModule != null) {
            importedModule = BuiltIns.importModule(fromModule);
        }

        int index = 0;
        for (int i = 0; i < targetSlots.length; i++) {
            Object importee = doImport(importedModule, aliases[index++]);
            try {
                frame.setObject(targetSlots[i], importee);
            } catch (FrameSlotTypeException e) {
                FrameUtil.setObjectSafe(frame, targetSlots[i], importee);
            }
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object importedModule = null;

        if (fromModule != null) {
            importedModule = BuiltIns.importModule(fromModule);
        }

        int index = 0;
        for (int i = 0; i < targetSlots.length; i++) {
            Object importee = doImport(importedModule, aliases[index++]);
            try {
                frame.setObject(targetSlots[i], importee);
            } catch (FrameSlotTypeException e) {
                FrameUtil.setObjectSafe(frame, targetSlots[i], importee);
            }
        }

        return null;
    }

    private static Object doImport(Object importedModule, String name) {
        try {
            if (importedModule != null && importedModule instanceof PythonModule) {
                return ((PythonModule) importedModule).lookupMethod(name);
            } else if (importedModule != null) {
                return ((PyObject) importedModule).__getattr__(name);
            } else {
                Object module = BuiltIns.importModule(name);
                return module;
            }
        } catch (PyException pye) {
            if (pye.match(Py.AttributeError)) {
                throw Py.ImportError(String.format("cannot import name %.230s", name));
            } else {
                throw pye;
            }
        }
    }

}
