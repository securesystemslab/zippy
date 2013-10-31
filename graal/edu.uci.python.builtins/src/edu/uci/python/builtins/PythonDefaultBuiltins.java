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

import static edu.uci.python.nodes.truffle.PythonTypesGen.*;

import java.util.*;

import org.python.core.*;

import edu.uci.python.builtins.PythonDefaultBuiltinsFactory.*;
import edu.uci.python.datatypes.*;
import edu.uci.python.nodes.*;
import edu.uci.python.nodes.calls.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;
import edu.uci.python.runtime.standardtypes.PythonBuiltins;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;

/**
 * @author Gulfem
 */

public final class PythonDefaultBuiltins extends PythonBuiltins {

    // abs(x)
    @Builtin(name = "abs", id = 1, fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonAbsNode extends PythonBuiltinNode {

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
            return FastMathUtil.hypot(arg.getReal(), arg.getImag());
        }

        @Specialization
        public double absObject(Object arg) {
            throw Py.TypeError("bad operand type for abs(): '" + PythonTypesUtil.getPythonTypeName(arg) + "'");
        }
    }

    // chr(i)
    @Builtin(name = "chr", id = 10, fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonChrNode extends PythonBuiltinNode {

        public PythonChrNode(String name) {
            super(name);
        }

        public PythonChrNode(PythonChrNode prev) {
            this(prev.getName());
        }

        @Specialization
        public char charFromInt(int arg) {
            return JavaTypeConversions.convertIntToChar(arg);
        }

        @Specialization
        public char charFromInt(Object arg) {
            if (arg instanceof Double) {
                throw Py.TypeError("integer argument expected, got float");
            }

            throw Py.TypeError("an integer is required");
        }
    }

    // complex([real[, imag]])
    @Builtin(name = "complex", id = 13, minNumOfArguments = 0, maxNumOfArguments = 2)
    public abstract static class PythonComplexNode extends PythonBuiltinNode {

        public PythonComplexNode(String name) {
            super(name);
        }

        public PythonComplexNode(PythonComplexNode prev) {
            this(prev.getName());
        }

        @Specialization(guards = "hasRealAndImaginary")
        public PComplex complexFromIntInt(int real, int imaginary) {
            return new PComplex(real, imaginary);
        }

        @Specialization(guards = "hasRealAndImaginary")
        public PComplex complexFromDoubleDouble(double real, double imaginary) {
            return new PComplex(real, imaginary);
        }

        @Specialization
        public PComplex complexFromObjectObject(Object real, Object imaginary) {
            if (real instanceof PNone) {
                return new PComplex(0, 0);
            }

            if (real instanceof Integer || real instanceof Double) {
                double realPart = (double) real;
                if (imaginary instanceof PNone) {
                    return new PComplex(realPart, 0);
                } else if (imaginary instanceof Integer || imaginary instanceof Double) {
                    double imagPart = (double) imaginary;
                    return new PComplex(realPart, imagPart);
                }
            } else if (real instanceof String) {
                if (!(imaginary instanceof PNone)) {
                    throw Py.TypeError("complex() can't take second arg if first is a string");
                }

                String realPart = (String) real;
                return JavaTypeConversions.convertStringToComplex(realPart);
            }

            throw Py.TypeError("can't convert real " + real + " imag " + imaginary);
        }

        public static boolean hasRealAndImaginary(Object real, Object imaginary) {
            return !(real instanceof PNone) && !(imaginary instanceof PNode);
        }
    }

    // float([x])
    @Builtin(name = "float", id = 22, minNumOfArguments = 0, maxNumOfArguments = 1)
    public abstract static class PythonFloatNode extends PythonBuiltinNode {

        public PythonFloatNode(String name) {
            super(name);
        }

        public PythonFloatNode(PythonFloatNode prev) {
            this(prev.getName());
        }

        @Specialization
        public double floatFromInt(int arg) {
            return arg;
        }

        @Specialization
        public double floatFromString(String arg) {
            return JavaTypeConversions.convertStringToDouble(arg);
        }

        @Specialization
        public double floatFromObject(Object arg) {
            if (arg instanceof PNone) {
                return 0.0;
            }

            throw Py.TypeError("can't convert " + arg.getClass().getSimpleName() + " to float ");
        }
    }

    // frozenset([iterable])
    @Builtin(name = "frozenset", id = 24, minNumOfArguments = 0, maxNumOfArguments = 1)
    public abstract static class PythonFrozenSetNode extends PythonBuiltinNode {

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

    // int(x=0)
    // int(x, base=10)
    @Builtin(name = "int", id = 33, minNumOfArguments = 0, maxNumOfArguments = 2, takesKeywordArguments = true)
    public abstract static class PythonIntNode extends PythonBuiltinNode {

        public PythonIntNode(String name) {
            super(name);
        }

        public PythonIntNode(PythonIntNode prev) {
            this(prev.getName());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noKeywordArg")
        public int createInt(int arg, Object keywordArg) {
            return arg;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noKeywordArg")
        public Object createInt(double arg, Object keywordArg) {
            return JavaTypeConversions.doubleToInt(arg);
        }

        @Specialization
        public Object createInt(Object arg, Object keywordArg) {
            // Covers the case for x = int()
            if (arg instanceof PNone) {
                return 0;
            }

            if (keywordArg instanceof PNone) {
                return JavaTypeConversions.toInt(arg);
            } else {
                throw new RuntimeException("Not implemented integer with base: " + keywordArg);
            }
        }

        @SuppressWarnings("unused")
        public static boolean noKeywordArg(Object arg, Object keywordArg) {
            return (keywordArg instanceof PNone);
        }
    }

    // iter(object[, sentinel])
    @Builtin(name = "iter", id = 36, minNumOfArguments = 1, maxNumOfArguments = 2)
    public abstract static class PythonIterNode extends PythonBuiltinNode {

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

    // len(s)
    @Builtin(name = "len", id = 37, fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonLenNode extends PythonBuiltinNode {

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

        @Specialization
        public int len(Object arg) {
            if (arg instanceof PNone) {
                throw Py.TypeError("len() takes exactly 1 argument (0 given)");
            } else if (arg instanceof String) {
                String argument = (String) arg;
                return argument.length();
            } else if (arg instanceof PSequence) {
                PSequence argument = (PSequence) arg;
                return argument.len();
            } else if (arg instanceof PDictionary) {
                PDictionary argument = (PDictionary) arg;
                return argument.len();
            } else if (arg instanceof PArray) {
                PArray argument = (PArray) arg;
                return argument.len();
            }

            throw Py.TypeError("object of type '" + PythonTypesUtil.getPythonTypeName(arg) + "' has no len()");
        }
    }

    // list([iterable])
    @Builtin(name = "list", id = 38, minNumOfArguments = 0, maxNumOfArguments = 1)
    public abstract static class PythonListNode extends PythonBuiltinNode {

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

    // max(iterable, *[, key])
    // max(arg1, arg2, *args[, key])
    @Builtin(name = "max", id = 41, minNumOfArguments = 1, maxNumOfArguments = 3, takesKeywordArguments = true, takesVariableArguments = true)
    public abstract static class PythonMaxNode extends PythonBuiltinNode {

        public PythonMaxNode(String name) {
            super(name);
        }

        public PythonMaxNode(PythonMaxNode prev) {
            this(prev.getName());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "twoArguments")
        public int maxIntInt(int arg1, int arg2, Object... args) {
            return Math.max(arg1, arg2);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "twoArguments")
        public double maxDoubleDouble(double arg1, double arg2, Object... args) {
            return Math.max(arg1, arg2);
        }

        @Specialization
        public Object max(Object arg1, Object arg2, Object... args) {
            if (arg1 instanceof PNone) {
                throw Py.TypeError("max expected 1 arguments, got 0");
            } else if (arg2 instanceof PNone) {
                if (arg1 instanceof String) {
                    /**
                     * TODO String is not implemented
                     */
                    String str = (String) arg1;
                    PString pstring = new PString(str);
                    return pstring.getMax();
                } else if (arg1 instanceof PSequence) {
                    PSequence sequence = (PSequence) arg1;
                    return sequence.getMax();
                } else if (arg1 instanceof PArray) {
                    PArray array = (PArray) arg1;
                    return array.getMax();
                } else if (arg1 instanceof PDictionary) {
                    PDictionary dictionary = (PDictionary) arg1;
                    return dictionary.getMax();
                } else {
                    throw Py.TypeError("' " + PythonTypesUtil.getPythonTypeName(arg1) + "' object is not iterable");
                }
            } else if (args.length == 0) {
                if (PYTHONTYPES.isDouble(arg1) && PYTHONTYPES.isDouble(arg2)) {
                    double arg1Double = (Double) arg1;
                    double arg2Double = (Double) arg2;
                    return Math.max(arg1Double, arg2Double);
                }
            }

            throw new RuntimeException("Optional keyword-only key argument is not supported");
        }

        @SuppressWarnings("unused")
        public static boolean oneArgument(Object arg1, Object arg2, Object... args) {
            return (arg2 instanceof PNone && args.length == 0);
        }

        @SuppressWarnings("unused")
        public static boolean twoArguments(Object arg1, Object arg2, Object... args) {
            return args.length == 0;
        }
    }

    // min(iterable, *[, key])
    // min(arg1, arg2, *args[, key])
    @Builtin(name = "min", id = 43, minNumOfArguments = 1, maxNumOfArguments = 3, takesKeywordArguments = true, takesVariableArguments = true)
    public abstract static class PythonMinNode extends PythonBuiltinNode {

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

    // next(iterator[, default])
    @Builtin(name = "next", id = 44, minNumOfArguments = 1, maxNumOfArguments = 2)
    public abstract static class PythonNextNode extends PythonBuiltinNode {

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

    // range(stop)
    // range(start, stop[, step])
    @Builtin(name = "range", id = 52, minNumOfArguments = 1, maxNumOfArguments = 3)
    public abstract static class PythonRangeNode extends PythonBuiltinNode {

        public PythonRangeNode(String name) {
            super(name);
        }

        public PythonRangeNode(PythonRangeNode prev) {
            this(prev.getName());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStop")
        public PSequence rangeStop(int stop, Object start, Object step) {
            return new PRange(stop);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStartStop")
        public PSequence rangeStartStop(int start, int stop, Object step) {
            return new PRange(start, stop);
        }

        @Specialization
        public PSequence rangeStartStopStep(int start, int stop, int step) {
            return new PRange(start, stop, step);
        }

        @SuppressWarnings("unused")
        public static boolean caseStop(int stop, Object start, Object step) {
            return (start instanceof PNone) && (step instanceof PNone);
        }

        @SuppressWarnings("unused")
        public static boolean caseStartStop(int start, int stop, Object step) {
            return (step instanceof PNone);
        }
    }

    // set([iterable])
    @Builtin(name = "set", id = 56, minNumOfArguments = 0, maxNumOfArguments = 1)
    public abstract static class PythonSetNode extends PythonBuiltinNode {

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

    /*
     * zip(*iterables)
     * 
     * @Builtin(name = "zip", id = 67, minNumOfArguments = 0) public abstract static class
     * PythonZipNode extends PythonBuiltinNode {
     * 
     * public PythonZipNode(String name) { super(name); }
     * 
     * public PythonZipNode(PythonZipNode prev) { this(prev.getName()); } }
     */

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
                PBuiltinFunction function;
                if (builtin.hasFixedNumOfArguments()) {
                    function = new PBuiltinFunction(methodName, builtin.fixedNumOfArguments(), builtin.fixedNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(),
                                    callTarget);
                } else {
                    function = new PBuiltinFunction(methodName, builtin.minNumOfArguments(), builtin.maxNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(), callTarget);
                }
                setBuiltin(methodName, function);
            }
        }
    }

    private static PythonBuiltinNode createBuiltin(Builtin builtin) {
        PNode[] args;
        int totalNumOfArgs;

        if (builtin.hasFixedNumOfArguments()) {
            totalNumOfArgs = builtin.fixedNumOfArguments();
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
