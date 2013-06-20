package org.python.ast.nodes.statements;

import org.python.core.*;
import org.python.core.truffle.BuiltIns;
import org.python.modules.truffle.Module;

import com.oracle.truffle.api.frame.*;

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
            frame.setObject(targetSlots[i], importee);
        }
    }

    private Object doImport(Object importedModule, String name) {
        try {
            if (importedModule != null && importedModule instanceof Module) {
                return ((Module) importedModule).lookupMethod(name);
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
