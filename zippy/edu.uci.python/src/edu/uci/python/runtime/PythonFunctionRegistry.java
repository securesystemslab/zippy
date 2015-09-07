package edu.uci.python.runtime;

import java.util.*;

import edu.uci.python.runtime.function.*;

public final class PythonFunctionRegistry {

    private final Map<String, PFunction> functions = new HashMap<>();

    public PFunction lookup(String name) {
        PFunction result = functions.get(name);
        return result;
    }

    public void register(String name, PFunction rootNode) {
        PFunction function = lookup(name);
        if (function == null)
            functions.put(name, rootNode);
    }

    /**
     * Returns the sorted list of all functions, for printing purposes only.
     */
    public List<PFunction> getFunctions() {
        List<PFunction> result = new ArrayList<>(functions.values());
        Collections.sort(result, new Comparator<PFunction>() {
            public int compare(PFunction f1, PFunction f2) {
                return f1.toString().compareTo(f2.toString());
            }
        });
        return result;
    }

}
