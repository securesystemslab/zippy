package edu.uci.python.runtime.modules;

import edu.uci.python.runtime.standardtypes.*;

public class PythonModulesContainer {

    public static PModule listModule;
    public static PModule stringModule;
    public static PModule dictionaryModule;

    public static void initialize() {
        listModule = new ListAttribute(PythonBuiltinsContainer.getInstance().getListBuiltins());
        stringModule = new StringAttribute(PythonBuiltinsContainer.getInstance().getStringBuiltins());
        dictionaryModule = new DictionaryAttribute(PythonBuiltinsContainer.getInstance().getDictionaryBuiltins());
    }
}
