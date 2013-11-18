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

import java.util.Arrays;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.builtins.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtypes.*;

public class ListBuiltins extends PythonBuiltins {

    // list.append(x)
    @Builtin(name = "append", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListAppendNode extends PythonBuiltinNode {

        public PythonListAppendNode(String name) {
            super(name);
        }

        public PythonListAppendNode(PythonListAppendNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList append(Object self, Object arg) {
            PList selfList = (PList) self;

            selfList.append(arg);
            return selfList;
        }
    }

    // list.extend(L)
    @Builtin(name = "extend", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListExtendNode extends PythonBuiltinNode {

        public PythonListExtendNode(String name) {
            super(name);
        }

        public PythonListExtendNode(PythonListExtendNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList extend(Object self, Object arg) {
            PList selfList = (PList) self;

            if (arg instanceof PList) {
                selfList.extend((PList) arg);
                return selfList;
            } else {
                throw new RuntimeException("invalid arguments for extend()");
            }
        }
    }

    // list.insert(i, x)
    @Builtin(name = "insert", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class PythonListInsertNode extends PythonBuiltinNode {

        public PythonListInsertNode(String name) {
            super(name);
        }

        public PythonListInsertNode(PythonListInsertNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList insert(Object self, Object arg0, Object arg1) {
            PList selfList = (PList) self;

            if (arg0 instanceof Integer) {
                selfList.insert((int) arg0, arg1);
                return selfList;
            } else {
                throw new RuntimeException("invalid arguments for insert()");
            }
        }
    }

    // list.remove(x)
    @Builtin(name = "remove", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListRemoveNode extends PythonBuiltinNode {

        public PythonListRemoveNode(String name) {
            super(name);
        }

        public PythonListRemoveNode(PythonListRemoveNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList remove(Object self, Object arg) {
            PList selfList = (PList) self;
            int index = selfList.index(arg);
            selfList.delItem(index);
            return selfList;
        }
    }

    // list.pop([i])
    @Builtin(name = "pop", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonListPopNode extends PythonBuiltinNode {

        public PythonListPopNode(String name) {
            super(name);
        }

        public PythonListPopNode(PythonListPopNode prev) {
            this(prev.getName());
        }

        @Specialization
        public Object pop(Object self, Object arg) {
            PList selfList = (PList) self;

            if (arg instanceof Integer) {
                int index = (int) arg;
                Object ret = selfList.getItem(index);
                selfList.delItem(index);
                return ret;
            } else {
                throw new RuntimeException("invalid arguments for pop()");
            }
        }
    }

    // list.index(x)
    @Builtin(name = "index", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListIndexNode extends PythonBuiltinNode {

        public PythonListIndexNode(String name) {
            super(name);
        }

        public PythonListIndexNode(PythonListIndexNode prev) {
            this(prev.getName());
        }

        @Specialization
        public int index(Object self, Object arg) {
            PList selfList = (PList) self;
            return selfList.index(arg);
        }
    }

    // list.count(x)
    @Builtin(name = "count", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListCountNode extends PythonBuiltinNode {

        public PythonListCountNode(String name) {
            super(name);
        }

        public PythonListCountNode(PythonListCountNode prev) {
            this(prev.getName());
        }

        @Specialization
        public int count(Object self, Object arg) {
            PList selfList = (PList) self;
            int count = 0;
            Object[] list = selfList.getSequence();
            for (int i = 0; i < list.length; i++) {
                if (list[i].equals(arg)) {
                    count++;
                }
            }
            return count;
        }
    }

    // list.sort()
    @Builtin(name = "sort", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonListSortNode extends PythonBuiltinNode {

        public PythonListSortNode(String name) {
            super(name);
        }

        public PythonListSortNode(PythonListSortNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList sort(Object self) {
            PList selfList = (PList) self;
            Object[] sorted = selfList.getSequence();
            Arrays.sort(sorted);

            for (int i = 0; i < sorted.length; i++) {
                selfList.setItem(i, sorted[i]);
            }

            return selfList;
        }
    }

    // list.reverse()
    @Builtin(name = "reverse", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonListReverseNode extends PythonBuiltinNode {

        public PythonListReverseNode(String name) {
            super(name);
        }

        public PythonListReverseNode(PythonListReverseNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList reverse(Object self) {
            PList selfList = (PList) self;
            selfList.reverse();
            return selfList;

        }
    }

    @Override
    public void initialize() {
        Class<?>[] declaredClasses = ListBuiltins.class.getDeclaredClasses();

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

        if (builtin.name().equals("append")) {
            return ListBuiltinsFactory.PythonListAppendNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("extend")) {
            return ListBuiltinsFactory.PythonListExtendNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("insert")) {
            return ListBuiltinsFactory.PythonListInsertNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("remove")) {
            return ListBuiltinsFactory.PythonListRemoveNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("pop")) {
            return ListBuiltinsFactory.PythonListPopNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("index")) {
            return ListBuiltinsFactory.PythonListIndexNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("count")) {
            return ListBuiltinsFactory.PythonListCountNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("sort")) {
            return ListBuiltinsFactory.PythonListSortNodeFactory.create(builtin.name(), args);
        } else if (builtin.name().equals("reverse")) {
            return ListBuiltinsFactory.PythonListReverseNodeFactory.create(builtin.name(), args);
        } else {
            throw new RuntimeException("Unsupported/Unexpected Builtin: " + builtin);
        }
    }
}
