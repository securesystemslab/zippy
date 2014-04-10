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

import edu.uci.python.builtins.module.*;
import edu.uci.python.builtins.type.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

/**
 * @author Gulfem
 * @author zwei
 */

public final class PythonDefaultBuiltinsLookup implements PythonBuiltinsLookup {

    private final Map<String, PythonModule> builtinModules;
    private final Map<Class<? extends PythonBuiltinObject>, PythonBuiltinClass> builtinTypes;

    public PythonDefaultBuiltinsLookup() {
        builtinModules = new HashMap<>();
        builtinTypes = new HashMap<>();
    }

    public PythonModule populateBuiltins(PythonContext context) {
        PythonModule builtinsModule = createModule("__builtins__", context, new BuiltinFunctions(), new BuiltinConstructors());
        builtinsModule.setAttribute("object", context.getObjectClass());
        addModule("__builtins__", builtinsModule);

        addModule("array", createModule("array", context, new ArrayModuleBuiltins()));
        addModule("time", createModule("time", context, new TimeModuleBuiltins()));
        addModule("math", createModule("math", context, new MathModuleBuiltins()));

        addType(PList.class, createType("list", context, builtinsModule, new ListBuiltins()));
        addType(PString.class, createType("str", context, builtinsModule, new StringBuiltins()));
        addType(PDict.class, createType("dict", context, builtinsModule, new DictionaryBuiltins()));

        return builtinsModule;
    }

    public void addModule(String name, PythonModule module) {
        builtinModules.put(name, module);
    }

    private void addType(Class<? extends PythonBuiltinObject> clazz, PythonBuiltinClass type) {
        builtinTypes.put(clazz, type);
    }

    private static PythonModule createModule(String name, PythonContext context, PythonBuiltins... builtins) {
        PythonModule module = new PythonModule(context, name, null);
        assert module.usePrivateLayout();

        for (PythonBuiltins builtin : builtins) {
            addBuiltinsToModule(module, builtin, context);
        }
        return module;
    }

    private static PythonBuiltinClass createType(String name, PythonContext context, PythonModule builtinsModule, PythonBuiltins builtins) {
        PythonBuiltinClass clazz = (PythonBuiltinClass) builtinsModule.getAttribute(name);
        addBuiltinsToClass(clazz, builtins, context);
        return clazz;
    }

    private static void addBuiltinsToModule(PythonModule module, PythonBuiltins builtins, PythonContext context) {
        if (builtins != null) {
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
    }

    private static void addBuiltinsToClass(PythonBuiltinClass clazz, PythonBuiltins builtins, PythonContext context) {
        if (builtins != null) {
            builtins.initialize(context);
            Map<String, PBuiltinFunction> builtinFunctions = builtins.getBuiltinFunctions();

            for (Map.Entry<String, PBuiltinFunction> entry : builtinFunctions.entrySet()) {
                String methodName = entry.getKey();
                PBuiltinFunction function = entry.getValue();
                clazz.setAttributeUnsafe(methodName, function);
            }
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
