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

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.frame.PackedFrame;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.annotations.*;

/**
 * The Python <code>Module</code> class.
 */
public class PythonModule extends PObject {

    private final String name;

    private final Map<String, PCallable> methods = new HashMap<>();
    private final Map<String, Object> constants = new HashMap<>();

    // The context is stored here - objects can obtain it via their class (which is a module)
    private final PythonContext context;

    public PythonModule(PythonContext context, String name) {
        this.name = name;
        this.context = context;
    }

    public PythonModule(String name) {
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
    public void addMethod(PCallable method) {
        methods.put(method.getName(), method);
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

    public Object lookup(String methodName) {
        PCallable method = lookupMethod(methodName);

        if (method != null) {
            return method;
        }

        return lookupConstant(methodName);
    }

    public PCallable lookupMethod(String methodName) {
        PCallable method;

        // Look in this module
        method = methods.get(methodName);

        if (method != null) {
            return method;
        }

        // Not found
        return null;
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

    /**
     * Load methods that are marked with @ModuleMethod in this class into the module.
     */
    public void addBuiltInMethods() {
        try {
            addBuiltInMethods(this.getClass());
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Error when adding methods for Module " + this.getClass().getSimpleName());
        }
    }

    /**
     * Load methods that are marked with @ModuleMethod in the supplied class into the module.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addBuiltInMethods(final Class definingClass) throws NoSuchMethodException, SecurityException {

        for (Method method : definingClass.getMethods()) {

            final Object finalDefiningModule = this;

            final int m = method.getModifiers();

            final ModuleMethod modmethod = method.getAnnotation(ModuleMethod.class);

            if (Modifier.isPublic(m) && modmethod != null) {
                final Method finalMethod = method;

                final Method method1 = definingClass.getMethod(finalMethod.getName(), new Class[]{Object.class});
                final Method method2 = definingClass.getMethod(finalMethod.getName(), new Class[]{Object.class, Object.class});

                final String methodName = modmethod.value().length() > 0 ? modmethod.value() : finalMethod.getName();

                final PCallable pythonMethod = new PCallable(methodName) {

                    @SlowPath
                    @Override
                    public Object call(PackedFrame caller, Object arg) {
                        try {
                            return method1.invoke(finalDefiningModule, new Object[]{arg});
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @SlowPath
                    @Override
                    public Object call(PackedFrame caller, Object arg0, Object arg1) {
                        try {
                            return method2.invoke(finalDefiningModule, new Object[]{arg0, arg1});
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @SlowPath
                    @Override
                    public Object call(PackedFrame frame, Object[] args, Object[] keywords) {
                        try {
                            return finalMethod.invoke(finalDefiningModule, new Object[]{args, keywords});
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                };
                addMethod(pythonMethod);
            }
        }
    }

    public void addAttributeMethods() throws NoSuchMethodException, SecurityException {
        addAttributeMethods(this.getClass());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addAttributeMethods(final Class definingClass) throws NoSuchMethodException, SecurityException {

        for (Method method : definingClass.getMethods()) {

            final Object finalDefiningModule = this;

            final int m = method.getModifiers();

            final ModuleMethod modmethod = method.getAnnotation(ModuleMethod.class);

            if (Modifier.isPublic(m) && modmethod != null) {

                final Method finalMethod = method;

                final Method method1 = definingClass.getMethod(finalMethod.getName(), new Class[]{Object.class, Object.class});
                final Method method2 = definingClass.getMethod(finalMethod.getName(), new Class[]{Object.class, Object.class, Object.class});

                final String methodName = modmethod.value().length() > 0 ? modmethod.value() : finalMethod.getName();

                final PCallable pythonMethod = new PCallable(methodName) {

                    @Override
                    public Object call(PackedFrame caller, Object arg) {
                        try {
                            return method1.invoke(finalDefiningModule, new Object[]{arg, this.self});
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public Object call(PackedFrame caller, Object arg0, Object arg1) {
                        try {
                            return method2.invoke(finalDefiningModule, new Object[]{arg0, arg1, this.self});
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public Object call(PackedFrame frame, Object[] args, Object[] keywords) {
                        try {
                            return finalMethod.invoke(finalDefiningModule, new Object[]{args, this.self});
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                };

                addMethod(pythonMethod);
            }
        }
    }

    @Override
    public Object getMin() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Object getMax() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int len() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Object multiply(int value) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public PCallable findAttribute(String attrName) {
        return lookupMethod(attrName);
    }

}
