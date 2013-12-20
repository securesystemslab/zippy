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
package edu.uci.python.builtins;

import java.util.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtins.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtypes.*;

/**
 * @author Gulfem
 */

public class PythonDefaultBuiltinsLookup implements PythonBuiltinsLookup {

    private final Map<String, PythonModule> builtinModules;
    private final Map<Class<? extends PythonBuiltinObject>, PythonBuiltinClass> builtinTypes;

    public PythonDefaultBuiltinsLookup() {
        builtinModules = new HashMap<>();
        builtinTypes = new HashMap<>();
    }

    public void addBuiltins(PythonContext context) {
        addBuiltinsToModule(context.getBuiltinsModule(), new PythonDefaultBuiltins(), context);
        addModule("__builtins__", context.getBuiltinsModule());
        addModule("__main__", context.getMainModule());

        addModule("array", createModule("array", new ArrayModuleBuiltins(), context));
        addModule("bisect", createModule("bisect", new BisectModuleBuiltins(), context));
        addModule("time", createModule("time", new TimeModuleBuiltins(), context));

        PythonBuiltinClass typeClass = context.getTypeClass();
        addType(PList.class, createType("list", typeClass, new ListBuiltins(), context));
        addType(PString.class, createType("str", typeClass, new StringBuiltins(), context));
        addType(PDict.class, createType("dict", typeClass, new DictionaryBuiltins(), context));
    }

    private void addModule(String name, PythonModule module) {
        builtinModules.put(name, module);
    }

    private void addType(Class<? extends PythonBuiltinObject> clazz, PythonBuiltinClass type) {
        builtinTypes.put(clazz, type);
    }

    private static PythonModule createModule(String name, PythonBuiltins builtins, PythonContext context) {
        PythonModule module = new PythonModule(name, context);
        addBuiltinsToModule(module, builtins, context);
        return module;
    }

    private static PythonBuiltinClass createType(String name, PythonClass superClass, PythonBuiltins builtins, PythonContext context) {
        PythonBuiltinClass clazz = new PythonBuiltinClass(context, superClass, name);
        addBuiltinsToClass(clazz, builtins, context);
        return clazz;
    }

    private static void addBuiltinsToModule(PythonModule module, PythonBuiltins builtins, PythonContext context) {
        builtins.initialize(context);
        Map<String, PBuiltinFunction> builtinFunctions = builtins.getBuiltinFunctions();
        for (Map.Entry<String, PBuiltinFunction> entry : builtinFunctions.entrySet()) {
            String methodName = entry.getKey();
            PBuiltinFunction function = entry.getValue();
            module.setAttribute(methodName, function);
        }

        Map<String, PythonBuiltinClass> builtinClasses = builtins.getBuiltinClasses();
        for (Map.Entry<String, PythonBuiltinClass> entry : builtinClasses.entrySet()) {
            String className = entry.getKey();
            PythonBuiltinClass function = entry.getValue();
            module.setAttribute(className, function);
        }
    }

    private static void addBuiltinsToClass(PythonBuiltinClass clazz, PythonBuiltins builtins, PythonContext context) {
        builtins.initialize(context);
        Map<String, PBuiltinFunction> builtinFunctions = builtins.getBuiltinFunctions();

        for (Map.Entry<String, PBuiltinFunction> entry : builtinFunctions.entrySet()) {
            String methodName = entry.getKey();
            PBuiltinFunction function = entry.getValue();
            clazz.setAttributeUnsafe(methodName, function);
        }
    }

    public PythonModule lookupModule(String name) {
        PythonModule module = builtinModules.get(name);
        return module;
    }

    public PythonBuiltinClass lookupType(Class<? extends PythonBuiltinObject> clazz) {
        PythonBuiltinClass type = builtinTypes.get(clazz);
        return type;
    }
}
