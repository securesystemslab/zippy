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
import edu.uci.python.nodes.*;
import edu.uci.python.nodes.calls.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;
import edu.uci.python.runtime.standardtypes.*;

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

    // all(iterable)
    @Builtin(name = "all", id = 2, fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonAllNode extends PythonBuiltinNode {

        public PythonAllNode(String name) {
            super(name);
        }

        public PythonAllNode(PythonAllNode prev) {
            this(prev.getName());
        }

        @Specialization
        public boolean all(PSequence sequence) {
            if (sequence.len() == 0) {
                return false;
            }

            Iterator<Object> iterator = sequence.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!JavaTypeConversions.toBoolean(element)) {
                    return false;
                }
            }

            return true;
        }

        @Specialization
        public boolean all(PBaseSet baseset) {
            if (baseset.len() == 0) {
                return false;
            }

            Iterator<Object> iterator = baseset.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!JavaTypeConversions.toBoolean(element)) {
                    return false;
                }
            }

            return true;
        }

        @Specialization
        public boolean all(Object object) {
            if (!(object instanceof Iterable<?>)) {
                throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(object) + "' object is not iterable");
            } else {
                throw new RuntimeException("all does not support iterable object " + object);
            }
        }

    }

    // any(iterable)
    @Builtin(name = "any", id = 3, fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonAnyNode extends PythonBuiltinNode {

        public PythonAnyNode(String name) {
            super(name);
        }

        public PythonAnyNode(PythonAnyNode prev) {
            this(prev.getName());
        }

        @Specialization
        public boolean any(PSequence sequence) {
            if (sequence.len() == 0) {
                return false;
            }

            Iterator<Object> iterator = sequence.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (JavaTypeConversions.toBoolean(element)) {
                    return true;
                }
            }

            return false;
        }

        @Specialization
        public boolean any(PBaseSet baseset) {
            if (baseset.len() == 0) {
                return false;
            }

            Iterator<Object> iterator = baseset.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (JavaTypeConversions.toBoolean(element)) {
                    return true;
                }
            }

            return false;
        }

        @Specialization
        public boolean any(Object object) {
            if (!(object instanceof Iterable<?>)) {
                throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(object) + "' object is not iterable");
            } else {
                throw new RuntimeException("any does not support iterable object " + object);
            }
        }

    }

    // bool([x])
    @Builtin(name = "bool", id = 6, minNumOfArguments = 0, maxNumOfArguments = 1)
    public abstract static class PythonBoolNode extends PythonBuiltinNode {

        public PythonBoolNode(String name) {
            super(name);
        }

        public PythonBoolNode(PythonBoolNode prev) {
            this(prev.getName());
        }

        @Specialization
        public boolean bool(int arg) {
            return arg != 0;
        }

        @Specialization
        public boolean bool(double arg) {
            return arg != 0.0;
        }

        @Specialization
        public boolean bool(String arg) {
            return !arg.isEmpty();
        }

        @Specialization
        public boolean bool(Object object) {
            if (object instanceof PNone) {
                return false;
            }
            return JavaTypeConversions.toBoolean(object);
        }
    }

    // callable(object)
    @Builtin(name = "callable", id = 9, fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonCallableNode extends PythonBuiltinNode {

        public PythonCallableNode(String name) {
            super(name);
        }

        public PythonCallableNode(PythonCallableNode prev) {
            this(prev.getName());
        }

        @SuppressWarnings("unused")
        @Specialization
        public boolean callable(PCallable callable) {
            return true;
        }

        @Specialization
        public boolean callable(Object object) {
            if (object instanceof PFunction) {
                return true;
            } else if (object instanceof PBuiltinFunction) {
                return true;
            }

            return false;
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

    // dir([object])
    @Builtin(name = "dir", id = 16, minNumOfArguments = 0, maxNumOfArguments = 1)
    public abstract static class PythonDirNode extends PythonBuiltinNode {

        public PythonDirNode(String name) {
            super(name);
        }

        public PythonDirNode(PythonDirNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PList dir() {
            return null;
        }
    }

    // enumerate(iterable, start=0)
    @Builtin(name = "enumerate", id = 18, minNumOfArguments = 1, maxNumOfArguments = 2, takesKeywordArguments = true)
    public abstract static class PythonEnumerateNode extends PythonBuiltinNode {

        public PythonEnumerateNode(String name) {
            super(name);
        }

        public PythonEnumerateNode(PythonEnumerateNode prev) {
            this(prev.getName());
        }

        /**
         * TODO enumerate can take a keyword argument start, and currently that's not supported.
         */

        // @SuppressWarnings("unused")
        // @Specialization(guards = "noKeywordArg")
        @Specialization
        public PEnumerate enumerate(String str) {
            return new PEnumerate(new PString(str));
        }

        @Specialization
        public PEnumerate enumerate(PSequence sequence) {
            return new PEnumerate(sequence);
        }

        @Specialization
        public PEnumerate enumerate(PBaseSet set) {
            return new PEnumerate(set);
        }

        @Specialization
        public PEnumerate enumerate(Object arg) {
            if (arg instanceof String) {
                String str = (String) arg;
                return new PEnumerate(stringToCharList(str));
            } else if (arg instanceof PSequence) {
                PSequence sequence = (PSequence) arg;
                return new PEnumerate(sequence);
            } else if (arg instanceof PBaseSet) {
                PBaseSet baseSet = (PBaseSet) arg;
                return new PEnumerate(baseSet);
            } else if (arg instanceof PGenerator) {
                PGenerator generator = (PGenerator) arg;
                return new PEnumerate(generator);
            }

            if (!(arg instanceof Iterable<?>)) {
                throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
            } else {
                throw new RuntimeException("enumerate does not support iterable object " + arg);
            }
        }

        @SuppressWarnings("unused")
        public static boolean noKeywordArg(Object arg, Object keywordArg) {
            return (keywordArg instanceof PNone);
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

    // isinstance(object, classinfo)
    @Builtin(name = "isinstance", id = 34, fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonIsIntanceNode extends PythonBuiltinNode {

        public PythonIsIntanceNode(String name) {
            super(name);
        }

        public PythonIsIntanceNode(PythonIsIntanceNode prev) {
            this(prev.getName());
        }

        @Specialization
        public Object isinstance(PythonObject object, PythonClass clazz) {
            if (object.getPythonClass().equals(clazz)) {
                return true;
            }

            PythonClass superClass = object.getPythonClass().getSuperClass();

            while (superClass != null) {
                if (superClass.equals(clazz)) {
                    return true;
                }

                superClass = superClass.getSuperClass();
            }

            return false;
        }

        @Specialization
        public Object isinstance(Object object, Object clazz) {
            throw new RuntimeException("isintance is not supported for " + object + " " + object.getClass() + ", " + clazz + " " + clazz.getClass());
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

        @SuppressWarnings("unused")
        @Specialization(guards = "noSentinel")
        public Object iter(String str, Object sentinel) {
            PString pstring = new PString(str);
            Iterator<Object> iterator = pstring.iterator();
            return iterator;
        }

        @SuppressWarnings("unused")
        @Specialization
        public Object iter(PSequence sequence, Object sentinel) {
            Iterator<Object> iterator = sequence.iterator();
            return iterator;
        }

        @SuppressWarnings("unused")
        @Specialization
        public Object iter(PBaseSet set, Object sentinel) {
            Iterator<Object> iterator = set.iterator();
            return iterator;
        }

        @SuppressWarnings("unused")
        @Specialization
        public Object iter(Object object, Object sentinel) {
            throw new RuntimeException("Not supported sentinel case");
        }

        @SuppressWarnings("unused")
        public static boolean noSentinel(String object, Object sentinel) {
            return (sentinel instanceof PNone);
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
            if (arg instanceof String) {
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

        @Specialization
        public PList list(Object arg) {
            if (arg instanceof String) {
                String str = (String) arg;
                return new PList(stringToCharList(str));
            } else if (arg instanceof PSequence) {
                PSequence sequence = (PSequence) arg;
                return new PList(sequence);
            } else if (arg instanceof PBaseSet) {
                PBaseSet baseSet = (PBaseSet) arg;
                return new PList(baseSet);
            } else if (arg instanceof PGenerator) {
                PGenerator generator = (PGenerator) arg;
                return new PList(generator);
            }

            if (!(arg instanceof Iterable<?>)) {
                throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
            } else {
                throw new RuntimeException("list does not support iterable object " + arg);
            }
        }
    }

    // max(iterable, *[, key])
    // max(arg1, arg2, *args[, key])
    @Builtin(name = "max", id = 41, minNumOfArguments = 1, takesKeywordArguments = true, takesVariableArguments = true)
    public abstract static class PythonMaxNode extends PythonBuiltinNode {

        public PythonMaxNode(String name) {
            super(name);
        }

        public PythonMaxNode(PythonMaxNode prev) {
            this(prev.getName());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "hasTwoArguments")
        public int maxIntInt(int arg1, int arg2, Object... args) {
            return Math.max(arg1, arg2);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "hasTwoArguments")
        public double maxDoubleDouble(double arg1, double arg2, Object... args) {
            return Math.max(arg1, arg2);
        }

        @Specialization
        public Object max(Object arg1, Object arg2, Object... args) {
            if (arg2 instanceof PNone) {
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

            /**
             * TODO Does not support var args {max(10, 20, 30, 40)} or keyword {max(10, 20, key =
             * func)}
             */
            throw new RuntimeException("Optional keyword-only key argument is not supported");

        }

        @SuppressWarnings("unused")
        public static boolean hasTwoArguments(Object arg1, Object arg2, Object... args) {
            return args.length == 0;
        }
    }

    // min(iterable, *[, key])
    // min(arg1, arg2, *args[, key])
    @Builtin(name = "min", id = 43, minNumOfArguments = 1, takesKeywordArguments = true, takesVariableArguments = true)
    public abstract static class PythonMinNode extends PythonBuiltinNode {

        public PythonMinNode(String name) {
            super(name);
        }

        public PythonMinNode(PythonMinNode prev) {
            this(prev.getName());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "hasTwoArguments")
        public int minIntInt(int arg1, int arg2, Object... args) {
            return Math.min(arg1, arg2);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "hasTwoArguments")
        public double minDoubleDouble(double arg1, double arg2, Object... args) {
            return Math.min(arg1, arg2);
        }

        @Specialization
        public Object min(Object arg1, Object arg2, Object... args) {
            if (arg2 instanceof PNone) {
                if (arg1 instanceof String) {
                    /**
                     * TODO String is not implemented
                     */
                    String str = (String) arg1;
                    PString pstring = new PString(str);
                    return pstring.getMin();
                } else if (arg1 instanceof PSequence) {
                    PSequence sequence = (PSequence) arg1;
                    return sequence.getMin();
                } else if (arg1 instanceof PArray) {
                    PArray array = (PArray) arg1;
                    return array.getMin();
                } else if (arg1 instanceof PDictionary) {
                    PDictionary dictionary = (PDictionary) arg1;
                    return dictionary.getMin();
                } else {
                    throw Py.TypeError("' " + PythonTypesUtil.getPythonTypeName(arg1) + "' object is not iterable");
                }
            } else if (args.length == 0) {
                if (PYTHONTYPES.isDouble(arg1) && PYTHONTYPES.isDouble(arg2)) {
                    double arg1Double = (Double) arg1;
                    double arg2Double = (Double) arg2;
                    return Math.min(arg1Double, arg2Double);
                }
            }

            /**
             * TODO Does not support var args {min(10, 20, 30, 40)} or keyword {min(10, 20, key =
             * func)}
             */
            throw new RuntimeException("Optional keyword-only key argument is not supported");

        }

        @SuppressWarnings("unused")
        public static boolean hasTwoArguments(Object arg1, Object arg2, Object... args) {
            return args.length == 0;
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
        @Specialization(order = 1, guards = "caseStop")
        public PSequence rangeStop(int stop, Object start, Object step) {
            return new PRange(stop);
        }

        @SuppressWarnings("unused")
        @Specialization(order = 2, guards = "caseStartStop")
        public PSequence rangeStartStop(int start, int stop, Object step) {
            return new PRange(start, stop);
        }

        @Specialization(order = 3)
        public PSequence rangeStartStopStep(int start, int stop, int step) {
            return new PRange(start, stop, step);
        }

        @Specialization
        public PSequence rangeStartStopStep(Object start, Object stop, Object step) {
            throw Py.TypeError("range does not support " + start + ", " + stop + ", " + step);
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

        @Specialization
        public PSet set(Object arg) {
            if (arg instanceof String) {
                String str = (String) arg;
                return new PSet(stringToCharList(str));
            } else if (arg instanceof PSequence) {
                PSequence sequence = (PSequence) arg;
                return new PSet(sequence);
            } else if (arg instanceof PBaseSet) {
                PBaseSet baseSet = (PBaseSet) arg;
                return new PSet(baseSet);
            } else if (arg instanceof PGenerator) {
                PGenerator generator = (PGenerator) arg;
                return new PSet(generator);
            }

            if (!(arg instanceof Iterable<?>)) {
                throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
            } else {
                throw new RuntimeException("set does not support iterable object " + arg);
            }
        }
    }

    // tuple([iterable])
    @Builtin(name = "tuple", id = 65, minNumOfArguments = 0, maxNumOfArguments = 1)
    public abstract static class PythonTupleNode extends PythonBuiltinNode {

        public PythonTupleNode(String name) {
            super(name);
        }

        public PythonTupleNode(PythonTupleNode prev) {
            this(prev.getName());
        }

        @Specialization
        public PTuple tuple(String arg) {
            return new PTuple(stringToCharList(arg));
        }

        @Specialization
        public PTuple tuple(PSequence sequence) {
            return new PTuple(sequence);
        }

        @Specialization
        public PTuple tuple(PBaseSet baseSet) {
            return new PTuple(baseSet);
        }

        @Specialization
        public PTuple tuple(PGenerator arg) {
            return new PTuple(arg);
        }

        @Specialization
        public PTuple tuple(Object arg) {
            if (arg instanceof String) {
                String str = (String) arg;
                return new PTuple(stringToCharList(str));
            } else if (arg instanceof PSequence) {
                PSequence sequence = (PSequence) arg;
                return new PTuple(sequence);
            } else if (arg instanceof PBaseSet) {
                PBaseSet baseSet = (PBaseSet) arg;
                return new PTuple(baseSet);
            } else if (arg instanceof PGenerator) {
                PGenerator generator = (PGenerator) arg;
                return new PTuple(generator);
            }

            if (!(arg instanceof Iterable<?>)) {
                throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
            } else {
                throw new RuntimeException("tuple does not support iterable object " + arg);
            }
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
                                    builtin.takesVariableArguments(), callTarget);
                } else {
                    function = new PBuiltinFunction(methodName, builtin.minNumOfArguments(), builtin.maxNumOfArguments(), builtin.hasFixedNumOfArguments(), builtin.takesKeywordArguments(),
                                    builtin.takesVariableArguments(), callTarget);
                }
                setBuiltin(methodName, function);
            }
        }
    }

    private static PythonBuiltinNode createBuiltin(Builtin builtin) {
        PNode[] args;
        int totalNumOfArgs;

        // max and min are special cases
        // They have two possibilities:
        // max(iterable, *[, key])
        // max(arg1, arg2, *args[, key])
        // In order to specialize correctly, they should have 3 arguments
        // arg1, arg2, vararg
        // arg2 is PNone if nothing in max(iterable, *[, key])
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

        switch (builtin.id()) {
            case 1:
                return PythonAbsNodeFactory.create(builtin.name(), args);
            case 2:
                return PythonAllNodeFactory.create(builtin.name(), args);
            case 3:
                return PythonAnyNodeFactory.create(builtin.name(), args);
            case 6:
                return PythonBoolNodeFactory.create(builtin.name(), args);
            case 9:
                return PythonCallableNodeFactory.create(builtin.name(), args);
            case 10:
                return PythonChrNodeFactory.create(builtin.name(), args);
            case 13:
                return PythonComplexNodeFactory.create(builtin.name(), args);
            case 16:
                return PythonDirNodeFactory.create(builtin.name(), args);
            case 18:
                return PythonEnumerateNodeFactory.create(builtin.name(), args);
            case 22:
                return PythonFloatNodeFactory.create(builtin.name(), args);
            case 24:
                return PythonFrozenSetNodeFactory.create(builtin.name(), args);
            case 33:
                return PythonIntNodeFactory.create(builtin.name(), args);
            case 34:
                return PythonIsIntanceNodeFactory.create(builtin.name(), args);
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
            case 65:
                return PythonTupleNodeFactory.create(builtin.name(), args);
            default:
                throw new RuntimeException("Unsupported/Unexpected Builtin: " + builtin);
        }
    }
}
