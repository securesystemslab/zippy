package edu.uci.python.builtins;

import edu.uci.python.runtime.standardtypes.*;

public class PythonBuiltinsInitializer {

    public static void initialize() {
        PythonBuiltinsContainer.getInstance().setDefaultBuiltins(new PythonDefaultBuiltins());
        PythonBuiltinsContainer.getInstance().setArrayModuleBuiltins(new ArrayModuleBuiltins());
        PythonBuiltinsContainer.getInstance().setBisectModuleBuiltins(new BisectModuleBuiltins());
        PythonBuiltinsContainer.getInstance().setTimeModuleBuiltins(new TimeModuleBuiltins());
        PythonBuiltinsContainer.getInstance().setListBuiltins(new ListBuiltins());
        PythonBuiltinsContainer.getInstance().setStringBuiltins(new StringBuiltins());
        PythonBuiltinsContainer.getInstance().setDictionaryBuiltins(new DictionaryBuiltins());
    }
}
