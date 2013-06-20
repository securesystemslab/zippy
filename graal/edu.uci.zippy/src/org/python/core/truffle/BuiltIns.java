package org.python.core.truffle;

import java.util.*;

import org.python.core.__builtin__;
import org.python.modules.truffle.ArrayModule;
import org.python.modules.truffle.BisectModule;
import org.python.modules.truffle.Module;

public class BuiltIns {

    public static final HashMap<String, Module> moduleMap = moduleMapInit();

    public static HashMap<String, Module> moduleMapInit() {
        HashMap<String, Module> map = new HashMap<String, Module>();
        map.put("array", new ArrayModule());
        map.put("bisect", new BisectModule());
        return map;
    }
    
    public static Object importModule(String name) {
        Object importedModule = moduleMap.get(name);
        if (importedModule == null)
            importedModule = __builtin__.__import__(name);
        return importedModule;
    }
}
