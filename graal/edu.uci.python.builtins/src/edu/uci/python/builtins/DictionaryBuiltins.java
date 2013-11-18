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

import java.util.ArrayList;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.builtins.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtypes.*;

public final class DictionaryBuiltins extends PythonBuiltins {

    // setdefault(key[, default])
    @Builtin(name = "setdefault", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionarySetDefaultNode extends PythonBuiltinNode {

        public PythonDictionarySetDefaultNode(String name) {
            super(name);
        }

        public PythonDictionarySetDefaultNode(PythonDictionarySetDefaultNode prev) {
            this(prev.getName());
        }

        @Specialization
        public Object setDefalut(Object self, Object arg0, Object arg1) {
            PDictionary dict = (PDictionary) self;

            if (dict.getMap().containsKey(arg0)) {
                return dict.getMap().get(arg0);
            } else {
                dict.getMap().put(arg0, arg1);
                return arg1;
            }
        }
    }

    // pop(key[, default])
    @Builtin(name = "pop", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryPopNode extends PythonBuiltinNode {

        public PythonDictionaryPopNode(String name) {
            super(name);
        }

        public PythonDictionaryPopNode(PythonDictionaryPopNode prev) {
            this(prev.getName());
        }

        @Specialization
        public Object pop(Object self, Object arg0, Object arg1) {
            PDictionary dict = (PDictionary) self;

            Object retVal = dict.getMap().get(arg0);
            if (retVal != null) {
                dict.getMap().remove(arg0);
                return retVal;
            } else {
                return arg1;
            }
        }
    }

    // keys()
    @Builtin(name = "keys", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryKeysNode extends PythonBuiltinNode {

        public PythonDictionaryKeysNode(String name) {
            super(name);
        }

        public PythonDictionaryKeysNode(PythonDictionaryKeysNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList keys(Object self) {
            PDictionary dict = (PDictionary) self;
            return new PList(new ArrayList<>(dict.getMap().keySet()));
        }
    }

    // items()
    @Builtin(name = "items", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryItemsNode extends PythonBuiltinNode {

        public PythonDictionaryItemsNode(String name) {
            super(name);
        }

        public PythonDictionaryItemsNode(PythonDictionaryItemsNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList items(Object self) {
            PDictionary dict = (PDictionary) self;
            return new PList(dict.getMap().entrySet());
        }
    }

    // get(key[, default])
    @Builtin(name = "get", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryGetNode extends PythonBuiltinNode {

        public PythonDictionaryGetNode(String name) {
            super(name);
        }

        public PythonDictionaryGetNode(PythonDictionaryGetNode prev) {
            this(prev.getName());
        }

        @Specialization
        public Object get(Object self, Object arg0, Object arg1) {
            PDictionary dict = (PDictionary) self;

            if (dict.getMap().get(arg0) != null) {
                return dict.getMap().get(arg0);
            } else {
                return arg1;
            }
        }
    }

    // copy()
    @Builtin(name = "copy", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryCopyNode extends PythonBuiltinNode {

        public PythonDictionaryCopyNode(String name) {
            super(name);
        }

        public PythonDictionaryCopyNode(PythonDictionaryCopyNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PDictionary copy(Object self) {
            PDictionary dict = (PDictionary) self;
            return new PDictionary(dict.getMap());
        }
    }

    // clear()
    @Builtin(name = "clear", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryClearNode extends PythonBuiltinNode {

        public PythonDictionaryClearNode(String name) {
            super(name);
        }

        public PythonDictionaryClearNode(PythonDictionaryClearNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PDictionary copy(Object self) {
            PDictionary dict = (PDictionary) self;
            dict.getMap().clear();
            return dict;
        }
    }

    // values()
    @Builtin(name = "values", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryValuesNode extends PythonBuiltinNode {

        public PythonDictionaryValuesNode(String name) {
            super(name);
        }

        public PythonDictionaryValuesNode(PythonDictionaryValuesNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList values(Object self) {
            PDictionary dict = (PDictionary) self;
            return new PList(new ArrayList<>(dict.getMap().values()));
        }
    }

    @Override
    public void initialize() {
        Class<?>[] declaredClasses = DictionaryBuiltins.class.getDeclaredClasses();

        for (int i = 0; i < declaredClasses.length; i++) {
            Class<?> clazz = declaredClasses[i];
            PBuiltinFunction function = findBuiltinFunction(clazz);

            if (function != null) {
                setBuiltinFunction(function.getName(), function);
            }
        }
    }

    private static PBuiltinFunction findBuiltinFunction(Class<?> clazz) {
        Builtin builtin = clazz.getAnnotation(Builtin.class);

        if (builtin != null) {
            String methodName = builtin.name();
            PythonBuiltinNode builtinNode = createBuiltin(builtin);
            BuiltinFunctionRootNode rootNode = new BuiltinFunctionRootNode(builtinNode);
            CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
            Arity arity = new Arity(methodName, builtin.fixedNumOfArguments(), builtin.fixedNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(),
                            builtin.takesVariableArguments());
            PBuiltinFunction builtinClass;

            if (builtin.hasFixedNumOfArguments()) {
                builtinClass = new PBuiltinFunction(methodName, arity, callTarget);
            } else {
                builtinClass = new PBuiltinFunction(methodName, arity, callTarget);
            }

            return builtinClass;
        }

        return null;
    }

    private static PythonBuiltinNode createBuiltin(Builtin builtin) {
        PNode[] args;
        int totalNumOfArgs;
        if (builtin.name().equals("max") || builtin.name().equals("min")) {
            totalNumOfArgs = 3;
        } else if (builtin.hasFixedNumOfArguments()) {
            totalNumOfArgs = builtin.fixedNumOfArguments();
        } else if (builtin.takesVariableArguments()) {
            totalNumOfArgs = builtin.minNumOfArguments() + 1;
        } else {
            totalNumOfArgs = builtin.maxNumOfArguments();
        }

        args = new PNode[totalNumOfArgs];
        for (int i = 0; i < totalNumOfArgs; i++) {
            args[i] = new ReadArgumentNode(i);
        }

        if (builtin.takesVariableArguments()) {
            args[totalNumOfArgs - 1] = new ReadVarArgsNode(totalNumOfArgs - 1);
        } else {
            if (builtin.takesKeywordArguments()) {
                args[totalNumOfArgs - 1] = new ReadArgumentNode(totalNumOfArgs - 1);
            }
        }

        if (builtin.name().equals("setdefault")) {
            return DictionaryBuiltinsFactory.PythonDictionarySetDefaultNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("pop")) {
            return DictionaryBuiltinsFactory.PythonDictionaryPopNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("keys")) {
            return DictionaryBuiltinsFactory.PythonDictionaryKeysNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("items")) {
            return DictionaryBuiltinsFactory.PythonDictionaryItemsNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("get")) {
            return DictionaryBuiltinsFactory.PythonDictionaryGetNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("copy")) {
            return DictionaryBuiltinsFactory.PythonDictionaryCopyNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("clear")) {
            return DictionaryBuiltinsFactory.PythonDictionaryClearNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("values")) {
            return DictionaryBuiltinsFactory.PythonDictionaryValuesNodeFactory.create(builtin.name(), args);
        } else {
            throw new RuntimeException("Unsupported/Unexpected Builtin: " + builtin);
        }
    }
}
