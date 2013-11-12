package edu.uci.python.runtime.standardtypes;

public class PythonBuiltinsContainer {

    private static PythonBuiltinsContainer instance;

    protected PythonBuiltins defaultBuiltins;

    protected PythonBuiltins arrayModuleBuiltins;

    protected PythonBuiltins bisectModuleBuiltins;

    protected PythonBuiltins timeModuleBuiltins;

    protected PythonBuiltins listBuiltins;

    protected PythonBuiltins stringBuiltins;

    protected PythonBuiltins dictionaryBuiltins;

    private PythonBuiltinsContainer() {
    }

    public static PythonBuiltinsContainer getInstance() {
        if (instance == null) {
            instance = new PythonBuiltinsContainer();
        }
        return instance;
    }

    public void setDefaultBuiltins(PythonBuiltins defaultBuiltins) {
        getInstance().defaultBuiltins = defaultBuiltins;
    }

    public void setArrayModuleBuiltins(PythonBuiltins arrayModuleBuiltins) {
        getInstance().arrayModuleBuiltins = arrayModuleBuiltins;
    }

    public void setBisectModuleBuiltins(PythonBuiltins bisectModuleBuiltins) {
        getInstance().bisectModuleBuiltins = bisectModuleBuiltins;
    }

    public void setTimeModuleBuiltins(PythonBuiltins timeModuleBuiltins) {
        getInstance().timeModuleBuiltins = timeModuleBuiltins;
    }

    public void setListBuiltins(PythonBuiltins listBuiltins) {
        getInstance().listBuiltins = listBuiltins;
    }

    public void setStringBuiltins(PythonBuiltins stringBuiltins) {
        getInstance().stringBuiltins = stringBuiltins;
    }

    public void setDictionaryBuiltins(PythonBuiltins dictionaryBuiltins) {
        getInstance().dictionaryBuiltins = dictionaryBuiltins;
    }

    public PythonBuiltins getDefaultBuiltins() {
        return defaultBuiltins;
    }

    public PythonBuiltins getArrayModuleBuiltins() {
        return arrayModuleBuiltins;
    }

    public PythonBuiltins getBisectModuleBuiltins() {
        return bisectModuleBuiltins;
    }

    public PythonBuiltins getTimeModuleBuiltins() {
        return timeModuleBuiltins;
    }

    public PythonBuiltins getListBuiltins() {
        return listBuiltins;
    }

    public PythonBuiltins getStringBuiltins() {
        return stringBuiltins;
    }

    public PythonBuiltins getDictionaryBuiltins() {
        return dictionaryBuiltins;
    }

}
