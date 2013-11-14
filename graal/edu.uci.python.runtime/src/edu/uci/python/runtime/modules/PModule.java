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
package edu.uci.python.runtime.modules;

import java.lang.reflect.*;
import java.util.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.annotations.*;
import edu.uci.python.runtime.standardtypes.*;

/**
 * TODO: replace {@link PModule} with {@link PythonModule}.
 */
public class PModule extends PythonBuiltinObject {

    private final String name;

    protected PythonBuiltins builtins;

    private final Map<String, PythonCallable> methods = new HashMap<>();
    private final Map<String, Object> constants = new HashMap<>();

    private final PythonContext context;

    public PModule(PythonContext context, String name) {
        this.name = name;
        this.context = context;
    }

    public PModule(String name) {
        this.name = name;
        this.context = null;
    }

    public String getName() {
        return name;
    }

    public PythonContext getContext() {
        return context;
    }

    /**
     * Set the value of a constant, possibly redefining it.
     */
    public void setConstant(String constantName, Object value) {
        constants.put(constantName, value);
    }

    /**
     * Add a method to the module. Only adds it to the module itself - not the singleton class of
     * the module instance.
     */
    public void addMethod(String methodName, PythonCallable method) {
        methods.put(methodName, method);
    }

    /**
     * Remove a method from this module.
     */
    public void removeMethod(String methodName) {
        methods.remove(methodName);
    }

    public Object lookupConstant(String constantName) {
        Object value;

        // Look in this module
        value = constants.get(constantName);

        if (value != null) {
            return value;
        }

        // Nothing found
        return null;
    }

    public PythonCallable lookupMethod(String methodName) {
        return builtins.getBuiltinFunction(methodName);
    }

    public PythonCallable lookupAttributeMethod(String methodName, Object self) {
        PBuiltinFunction builtinFunction = builtins.getBuiltinFunction(methodName);
        builtinFunction.setSelf(self);
        return builtinFunction;
    }

    /**
     * Load constants that are marked with @ModuleConstant in this class into the module.
     */
    public void addConstants() {
        for (Field field : this.getClass().getFields()) {
            final int m = field.getModifiers();

            if (Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) && field.getAnnotation(ModuleConstant.class) != null) {
                try {
                    setConstant(field.getName(), field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("access error when populating constants", e);
                }
            }
        }
    }

    @Override
    public PythonCallable findAttribute(String attrName) {
        return lookupMethod(attrName);
    }

}
