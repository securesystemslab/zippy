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

import java.math.*;
import java.util.*;

import edu.uci.python.builtins.PythonDefaultBuiltinsFactory.*;
import edu.uci.python.datatypes.*;
import edu.uci.python.nodes.*;
import edu.uci.python.nodes.calls.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;
import edu.uci.python.runtime.objects.*;
import edu.uci.python.runtime.standardtypes.PythonBuiltins;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;

/**
 * @author Gulfem
 */

public final class PythonDefaultBuiltins extends PythonBuiltins {

    public abstract static class PythonBasicBuiltinNode extends PythonBuiltinNode {

        public PythonBasicBuiltinNode(String name) {
            super(name);
        }

        public PythonBasicBuiltinNode(PythonBasicBuiltinNode prev) {
            super(prev.getName());
        }

        public static boolean noArgument(Object... args) {
            return args.length == 0;
        }

        public static boolean oneArgument(Object... args) {
            return args.length == 1;
        }

        public static boolean twoArguments(Object... args) {
            return args.length == 2;
        }
    }

    @Builtin(name = "abs", id = 1, numOfArguments = 1)
    public abstract static class PythonAbsNode extends PythonBasicBuiltinNode {

        public PythonAbsNode(String name) {
            super(name);
        }

        public PythonAbsNode(PythonAbsNode prev) {
            this(prev.getName());
        }

        @Specialization
        public int absInt(int arg) {
            return Math.abs(arg);
        }

        @Specialization
        public double absDouble(double arg) {
            return Math.abs(arg);
        }

        @Specialization
        public double absPComplex(PComplex arg) {
            return Math.hypot(arg.getReal(), arg.getImag());
        }
    }

    @Builtin(name = "chr", id = 10, numOfArguments = 1)
    public abstract static class PythonChrNode extends PythonBasicBuiltinNode {

        public PythonChrNode(String name) {
            super(name);
        }

        public PythonChrNode(PythonChrNode prev) {
            this(prev.getName());
        }

        @Specialization
        public String charFromInt(int arg) {
            return Character.toString((char) arg);
        }
    }

    @Builtin(name = "complex", id = 13, numOfArguments = 0, varArgs = true)
    public abstract static class PythonComplexNode extends PythonBasicBuiltinNode {

        public PythonComplexNode(String name) {
            super(name);
        }

        public PythonComplexNode(PythonComplexNode prev) {
            this(prev.getName());
        }

        @Specialization(order = 1, guards = "twoArguments")
        public PComplex complexTwoArguments(Object... args) {
            if (args[0] instanceof Integer && args[1] instanceof Integer) {
                return new PComplex((int) args[0], (int) args[1]);
            }

            /**
             * TODO real and imaginary values can be any numeric value such as int, double, complex
             * Currently, only ints are not supported.
             */
            throw new RuntimeException("Not implemented complex: " + args[0] + " " + args[1]);
        }

        @Specialization(order = 2, guards = "oneArgument")
        public PComplex complexOneArgument(Object... args) {
            if (args[0] instanceof Integer) {
                return new PComplex((int) args[0], 0);
            } else if (args[0] instanceof Double) {
                return new PComplex((double) args[0], 0);
            }
            throw new RuntimeException("Not implemented complex: " + args[0]);
        }

        @Specialization(order = 3, guards = "noArgument")
        public PComplex complexNoArgument(Object... args) {
            return new PComplex(0, 0);
        }
    }

    @Builtin(name = "float", id = 22, numOfArguments = 0, varArgs = true)
    public abstract static class PythonFloatNode extends PythonBasicBuiltinNode {

        public PythonFloatNode(String name) {
            super(name);
        }

        public PythonFloatNode(PythonFloatNode prev) {
            this(prev.getName());
        }

        @Specialization
        public double floatFromString(String arg) {
            throw new RuntimeException("Not implemented integer: ");
        }

        @Specialization
        public double floatFromInt(int arg) {
            return arg;
        }
    }

    @Builtin(name = "frozenset", id = 24, numOfArguments = 1)
    public abstract static class PythonFrozenSetNode extends PythonBasicBuiltinNode {

        public PythonFrozenSetNode(String name) {
            super(name);
        }

        public PythonFrozenSetNode(PythonFrozenSetNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PFrozenSet frozenset(String arg) {
            return new PFrozenSet(stringToCharList(arg));
        }

        @Specialization
        public PFrozenSet frozenset(PSequence sequence) {
            return new PFrozenSet(sequence);
        }

        @Specialization
        public PFrozenSet frozenset(PBaseSet baseSet) {
            return new PFrozenSet(baseSet);
        }

        @Specialization
        public PFrozenSet frozenset(PGenerator arg) {
            return new PFrozenSet(arg);
        }
    }

    @Builtin(name = "int", id = 33, numOfArguments = 1, varArgs = true)
    public abstract static class PythonIntNode extends PythonBasicBuiltinNode {

        public PythonIntNode(String name) {
            super(name);
        }

        public PythonIntNode(PythonIntNode prev) {
            this(prev.getName());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noVariableArguments")
        public int createInt(int arg, Object... args) {
            return arg;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noVariableArguments")
        public Object createInt(double arg, Object... args) {
            return JavaTypeConversions.doubleToInt(arg);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noVariableArguments")
        public Object createInt(String arg, Object... args) {
            return JavaTypeConversions.stringToInt(arg, 10);
        }

        @Specialization
        public Object createInt(Object arg, Object... args) {
            // Covers the case for x = int()
            if (arg instanceof Undefined) {
                return 0;
            }

            if (args.length == 0) {
                return JavaTypeConversions.toInt(arg);
            } else {
                throw new RuntimeException("Not implemented integer with base: " + arg);
            }
        }

        @SuppressWarnings("unused")
        public static boolean noVariableArguments(Object arg, Object... args) {
            return args.length == 0;
        }
    }

    @Builtin(name = "iter", id = 36, numOfArguments = 1, varArgs = true)
    public abstract static class PythonIterNode extends PythonBasicBuiltinNode {

        public PythonIterNode(String name) {
            super(name);
        }

        public PythonIterNode(PythonIterNode prev) {
            this(prev.getName());
        }

        @Specialization
        public Object iter(String object) {
            PString pstring = new PString(object);
            Iterator<Object> iterator = pstring.iterator();
            return iterator;
        }
    }

    @Builtin(name = "len", id = 37, numOfArguments = 1)
    public abstract static class PythonLenNode extends PythonBasicBuiltinNode {

        public PythonLenNode(String name) {
            super(name);
        }

        public PythonLenNode(PythonLenNode prev) {
            this(prev.getName());
        }

        @Specialization
        public int len(String arg) {
            return arg.length();
        }

        @Specialization
        public int len(PSequence arg) {
            return arg.len();
        }

        @Specialization
        public int len(PDictionary arg) {
            return arg.len();
        }

        @Specialization
        public int len(PArray arg) {
            return arg.len();
        }
    }

    @Builtin(name = "list", id = 38, numOfArguments = 1)
    public abstract static class PythonListNode extends PythonBasicBuiltinNode {

        public PythonListNode(String name) {
            super(name);
        }

        public PythonListNode(PythonListNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList list(String arg) {
            return new PList(stringToCharList(arg));
        }

        @Specialization
        public PList list(PSequence sequence) {
            return new PList(sequence);
        }

        @Specialization
        public PList list(PBaseSet baseSet) {
            return new PList(baseSet);
        }

        @Specialization
        public PList list(PGenerator generator) {
            return new PList(generator);
        }
    }

    @Builtin(name = "max", id = 41, numOfArguments = 2)
    public abstract static class PythonMaxNode extends PythonBasicBuiltinNode {

        public PythonMaxNode(String name) {
            super(name);
        }

        public PythonMaxNode(PythonMaxNode prev) {
            this(prev.getName());
        }

        @Specialization
        public int maxIntInt(int arg1, int arg2) {
            return Math.max(arg1, arg2);
        }

        @Specialization
        public double maxDoubleDouble(double arg1, double arg2) {
            return Math.max(arg1, arg2);
        }
    }

    @Builtin(name = "min", id = 43, numOfArguments = 2)
    public abstract static class PythonMinNode extends PythonBasicBuiltinNode {

        public PythonMinNode(String name) {
            super(name);
        }

        public PythonMinNode(PythonMinNode prev) {
            this(prev.getName());
        }

        @Specialization
        public int minIntInt(int arg1, int arg2) {
            return Math.min(arg1, arg2);
        }

        @Specialization
        public double minDoubleDouble(double arg1, double arg2) {
            return Math.min(arg1, arg2);
        }
    }

    @Builtin(name = "next", id = 44, numOfArguments = 1, varArgs = true)
    public abstract static class PythonNextNode extends PythonBasicBuiltinNode {

        public PythonNextNode(String name) {
            super(name);
        }

        public PythonNextNode(PythonNextNode prev) {
            this(prev.getName());
        }

        @Specialization
        public int next(Object iterator) {
            return 10;
        }
    }

    @Builtin(name = "range", id = 52, numOfArguments = 1, varArgs = true)
    public abstract static class PythonRangeNode extends PythonBasicBuiltinNode {

        public PythonRangeNode(String name) {
            super(name);
        }

        public PythonRangeNode(PythonRangeNode prev) {
            this(prev.getName());
        }

        @Specialization(order = 1, guards = "caseStop")
        public PSequence rangeStop(int stop, Object... arguments) {
            return new PRange(stop);
        }

        @Specialization(order = 2, guards = "caseStartStop")
        public PSequence rangeStartStop(int start, Object... args) {
            assert args[0] instanceof Integer;
            return new PRange(start, (int) args[0]);
        }

        @Specialization(order = 3, guards = "caseStartStopStep")
        public PSequence rangeStartStopStep(int start, Object... args) {
            assert args[0] instanceof Integer && args[1] instanceof Integer;
            return new PRange(start, (int) args[0], (int) args[1]);
        }

        @SuppressWarnings("unused")
        public static boolean caseStop(int start, Object... args) {
            return args.length == 0;
        }

        @SuppressWarnings("unused")
        public static boolean caseStartStop(int start, Object... args) {
            return args.length == 1;
        }

        @SuppressWarnings("unused")
        public static boolean caseStartStopStep(int start, Object... args) {
            return args.length == 2;
        }
    }

    @Builtin(name = "set", id = 56, numOfArguments = 1)
    public abstract static class PythonSetNode extends PythonBasicBuiltinNode {

        public PythonSetNode(String name) {
            super(name);
        }

        public PythonSetNode(PythonSetNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PSet set(String arg) {
            return new PSet(stringToCharList(arg));
        }

        @Specialization
        public PSet set(PSequence sequence) {
            return new PSet(sequence);
        }

        @Specialization
        public PSet set(PBaseSet baseSet) {
            return new PSet(baseSet);
        }

        @Specialization
        public PSet set(PGenerator arg) {
            return new PSet(arg);
        }
    }

    private static List<Character> stringToCharList(String s) {
        ArrayList<Character> sequence = new ArrayList<>();

        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            sequence.add(charArray[i]);
        }
        return sequence;
    }

    @Override
    public void initialize() {
        Class<?>[] declaredClasses = PythonDefaultBuiltins.class.getDeclaredClasses();

        for (int i = 0; i < declaredClasses.length; i++) {
            Class<?> clazz = declaredClasses[i];
            Builtin builtin = clazz.getAnnotation(Builtin.class);

            if (builtin != null) {
                PythonBuiltinNode builtinNode = createBuiltin(builtin);
                String methodName = builtin.name();
                PythonBuiltinRootNode rootNode = new PythonBuiltinRootNode(builtinNode);
                CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
                PBuiltinFunction function = new PBuiltinFunction(methodName, callTarget);
                setBuiltin(methodName, function);
            }
        }
    }

    private static PythonBuiltinNode createBuiltin(Builtin builtin) {
        PNode args[];
        if (builtin.varArgs()) {
            args = new PNode[builtin.numOfArguments() + 1];
        } else {
            args = new PNode[builtin.numOfArguments()];

        }

        for (int i = 0; i < builtin.numOfArguments(); i++) {
            args[i] = new ReadArgumentNode(i);
        }

        if (builtin.varArgs()) {
            args[builtin.numOfArguments()] = new ReadVarArgsNode(builtin.numOfArguments());
        }

        switch (builtin.id()) {
            case 1:
                return PythonAbsNodeFactory.create(builtin.name(), args);
            case 10:
                return PythonChrNodeFactory.create(builtin.name(), args);
            case 13:
                return PythonComplexNodeFactory.create(builtin.name(), args);
            case 22:
                return PythonFloatNodeFactory.create(builtin.name(), args);
            case 24:
                return PythonFrozenSetNodeFactory.create(builtin.name(), args);
            case 33:
                return PythonIntNodeFactory.create(builtin.name(), args);
            case 36:
                return PythonIterNodeFactory.create(builtin.name(), args);
            case 37:
                return PythonLenNodeFactory.create(builtin.name(), args);
            case 38:
                return PythonListNodeFactory.create(builtin.name(), args);
            case 41:
                return PythonMaxNodeFactory.create(builtin.name(), args);
            case 43:
                return PythonMinNodeFactory.create(builtin.name(), args);
            case 44:
                return PythonNextNodeFactory.create(builtin.name(), args);
            case 52:
                return PythonRangeNodeFactory.create(builtin.name(), args);
            case 56:
                return PythonSetNodeFactory.create(builtin.name(), args);
            default:
                throw new RuntimeException("Unsupported/Unexpected Builtin: " + builtin);
        }
    }
}
