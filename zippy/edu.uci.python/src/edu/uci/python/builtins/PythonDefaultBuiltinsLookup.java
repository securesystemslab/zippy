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

import java.util.HashMap;
import java.util.Map;

import edu.uci.python.builtins.module.ArrayModuleBuiltins;
import edu.uci.python.builtins.module.FunctoolsModuleBuiltins;
import edu.uci.python.builtins.module.MathModuleBuiltins;
import edu.uci.python.builtins.module.RandomModuleBuiltins;
import edu.uci.python.builtins.module.TimeModuleBuiltins;
import edu.uci.python.builtins.type.DictBuiltins;
import edu.uci.python.builtins.type.GeneratorBuiltins;
import edu.uci.python.builtins.type.ListBuiltins;
import edu.uci.python.builtins.type.ObjectBuiltins;
import edu.uci.python.builtins.type.SetBuiltins;
import edu.uci.python.builtins.type.StringBuiltins;
import edu.uci.python.builtins.type.TupleBuiltins;
import edu.uci.python.nodes.interop.InteropNodes;
import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.builtin.PythonBuiltinClass;
import edu.uci.python.runtime.builtin.PythonBuiltinsLookup;
import edu.uci.python.runtime.datatype.PDict;
import edu.uci.python.runtime.datatype.PFloat;
import edu.uci.python.runtime.datatype.PGenerator;
import edu.uci.python.runtime.datatype.PInt;
import edu.uci.python.runtime.function.PBuiltinFunction;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.sequence.PSet;
import edu.uci.python.runtime.sequence.PString;
import edu.uci.python.runtime.sequence.PTuple;
import edu.uci.python.runtime.standardtype.PythonBuiltinObject;
import edu.uci.python.runtime.standardtype.PythonModule;

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
        PythonModule builtinsModule = createModule("builtins", context, new BuiltinFunctions(), new BuiltinConstructors(), new InteropNodes());
        builtinsModule.setAttribute("object", context.getObjectClass());
        addModule("builtins", builtinsModule);

        addModule("array", createModule("array", context, new ArrayModuleBuiltins()));
        addModule("time", createModule("time", context, new TimeModuleBuiltins()));
        addModule("math", createModule("math", context, new MathModuleBuiltins()));
        addModule("random", createModule("random", context, new RandomModuleBuiltins()));
        addModule("functools", createModule("functools", context, new FunctoolsModuleBuiltins()));

        // Only populate builtins, no need to add it to the builtinTypes lookup.
        createType("object", context, builtinsModule, new ObjectBuiltins());
        addType(PInt.class, (PythonBuiltinClass) builtinsModule.getAttribute("int"));
        addType(PFloat.class, (PythonBuiltinClass) builtinsModule.getAttribute("float"));

        addType(PList.class, createType("list", context, builtinsModule, new ListBuiltins()));
        addType(PTuple.class, createType("tuple", context, builtinsModule, new TupleBuiltins()));
        addType(PString.class, createType("str", context, builtinsModule, new StringBuiltins()));
        addType(PDict.class, createType("dict", context, builtinsModule, new DictBuiltins()));
        addType(PSet.class, createType("set", context, builtinsModule, new SetBuiltins()));

        addType(PGenerator.class, createType("generator", context, new GeneratorBuiltins()));

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

    private static PythonBuiltinClass createType(String name, PythonContext context, PythonBuiltins builtins) {
        PythonBuiltinClass clazz = new PythonBuiltinClass(context, name, context.getTypeClass());
        addBuiltinsToClass(clazz, builtins, context);
        return clazz;
    }

    private static void addBuiltinsToModule(PythonModule module, PythonBuiltins builtins, PythonContext context) {
        if (builtins != null) {
            builtins.initialize(context);
            Map<String, Object> builtinConstants = builtins.getBuiltinConstants();
            for (Map.Entry<String, Object> entry : builtinConstants.entrySet()) {
                String constantName = entry.getKey();
                Object object = entry.getValue();
                module.setAttribute(constantName, object);
            }

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
            Map<String, Object> builtinConstants = builtins.getBuiltinConstants();
            for (Map.Entry<String, Object> entry : builtinConstants.entrySet()) {
                String className = entry.getKey();
                Object object = entry.getValue();
                clazz.setAttributeUnsafe(className, object);
            }

            Map<String, PBuiltinFunction> builtinFunctions = builtins.getBuiltinFunctions();
            for (Map.Entry<String, PBuiltinFunction> entry : builtinFunctions.entrySet()) {
                String className = entry.getKey();
                PBuiltinFunction function = entry.getValue();
                clazz.setAttributeUnsafe(className, function);
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
