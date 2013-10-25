package edu.uci.python.runtime.standardtypes;

import java.util.*;

import edu.uci.python.runtime.datatypes.*;

/**
 * @author Gulfem
 */
public abstract class PythonBuiltins {

    private final Map<String, PBuiltinFunction> builtins = new HashMap<>();

    public abstract void initialize();

    public void setBuiltin(String name, PBuiltinFunction function) {
        builtins.put(name, function);
    }

    public Map<String, PBuiltinFunction> getBuiltins() {
        return builtins;
    }
}
