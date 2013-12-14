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

import org.python.core.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.misc.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtypes.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.CompilerDirectives.SlowPath;

/**
 * @author Gulfem
 * @author zwei
 */
public final class PythonDefaultBuiltins extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return PythonDefaultBuiltinsFactory.getFactories();
    }

    public static class PythonBuiltinFunctions {

        // abs(x)
        @Builtin(name = "abs", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
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
        @Builtin(name = "all", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
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

                PIterator iterator = sequence.__iter__();

                try {
                    while (true) {
                        Object element = iterator.__next__();
                        if (!JavaTypeConversions.toBoolean(element)) {
                            return false;
                        }
                    }
                } catch (StopIterationException e) {
                    // fall through
                }

                return true;
            }

            @Specialization
            public boolean all(PBaseSet baseset) {
                if (baseset.len() == 0) {
                    return false;
                }

                PIterator iterator = baseset.__iter__();

                try {
                    while (true) {
                        Object element = iterator.__next__();
                        if (!JavaTypeConversions.toBoolean(element)) {
                            return false;
                        }
                    }
                } catch (StopIterationException e) {
                    // fall through
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
        @Builtin(name = "any", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
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

                PIterator iterator = sequence.__iter__();

                try {
                    while (true) {
                        Object element = iterator.__next__();
                        if (JavaTypeConversions.toBoolean(element)) {
                            return true;
                        }
                    }
                } catch (StopIterationException e) {
                    // fall through
                }

                return false;
            }

            @Specialization
            public boolean any(PBaseSet baseset) {
                if (baseset.len() == 0) {
                    return false;
                }

                PIterator iterator = baseset.__iter__();

                try {
                    while (true) {
                        Object element = iterator.__next__();
                        if (JavaTypeConversions.toBoolean(element)) {
                            return true;
                        }
                    }
                } catch (StopIterationException e) {
                    // fall through
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

        // callable(object)
        @Builtin(name = "callable", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
        public abstract static class PythonCallableNode extends PythonBuiltinNode {

            public PythonCallableNode(String name) {
                super(name);
            }

            public PythonCallableNode(PythonCallableNode prev) {
                this(prev.getName());
            }

            @SuppressWarnings("unused")
            @Specialization
            public boolean callable(PythonCallable callable) {
                return true;
            }

            @Specialization
            public boolean callable(Object object) {
                if (object instanceof PythonCallable) {
                    return true;
                }

                return false;
            }
        }

        // chr(i)
        @Builtin(name = "chr", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
        public abstract static class PythonChrNode extends PythonBuiltinNode {

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

// @Specialization
// public char charFromInt(int arg) {
// if (arg < 0 || arg > 0x10FFFF) {
// throw Py.ValueError("chr() arg not in range(0x110000)");
// }
// return (char) arg;
// }

            @Specialization
            public char charFromInt(Object arg) {
                if (arg instanceof Double) {
                    throw Py.TypeError("integer argument expected, got float");
                }

                throw Py.TypeError("an integer is required");
            }
        }

        // isinstance(object, classinfo)
        @Builtin(name = "isinstance", hasFixedNumOfArguments = true, fixedNumOfArguments = 2)
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
        @Builtin(name = "iter", minNumOfArguments = 1, maxNumOfArguments = 2)
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
                return new PStringIterator(str);
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "noSentinel")
            public Object iter(PSequence sequence, Object sentinel) {
                return sequence.__iter__();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "noSentinel")
            public Object iter(PBaseSet set, Object sentinel) {
                return set.__iter__();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "noSentinel")
            public Object iter(PDict dictionary, Object sentinel) {
                return dictionary.__iter__();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "noSentinel")
            public Object iter(Object object, Object sentinel) {
                throw new RuntimeException("Not supported sentinel case");
            }

            @SuppressWarnings("unused")
            public static boolean noSentinel(Object object, Object sentinel) {
                return (sentinel instanceof PNone);
            }
        }

        // len(s)
        @Builtin(name = "len", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
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

            @Specialization(order = 1)
            public int len(PList list) {
                return list.len();
            }

            @Specialization(order = 2)
            public int len(PTuple tuple) {
                return tuple.len();
            }

            @Specialization(order = 3)
            public int len(PRange range) {
                return range.len();
            }

            @Specialization(order = 4)
            public int len(PArray array) {
                return array.len();
            }

            @Specialization(order = 5)
            public int len(PBaseSet arg) {
                return arg.len();
            }

            @Specialization(order = 6)
            public int len(PDict arg) {
                return arg.len();
            }

            @Specialization
            public int len(Object arg) {
                if (arg instanceof String) {
                    String argument = (String) arg;
                    return argument.length();
                } else if (arg instanceof PList) {
                    PList argument = (PList) arg;
                    return argument.len();
                } else if (arg instanceof PTuple) {
                    PTuple argument = (PTuple) arg;
                    return argument.len();
                } else if (arg instanceof PRange) {
                    PRange argument = (PRange) arg;
                    return argument.len();
                } else if (arg instanceof PArray) {
                    PArray argument = (PArray) arg;
                    return argument.len();
                } else if (arg instanceof PBaseSet) {
                    PBaseSet argument = (PBaseSet) arg;
                    return argument.len();
                } else if (arg instanceof PDict) {
                    PDict argument = (PDict) arg;
                    return argument.len();
                }

                String message = "object of type '" + PythonTypesUtil.getPythonTypeName(arg) + "' has no len()";
                typeError(message);
                return 0;
            }
        }

        // max(iterable, *[, key])
        // max(arg1, arg2, *args[, key])
        @Builtin(name = "max", minNumOfArguments = 1, takesKeywordArguments = true, takesVariableArguments = true, keywordNames = {"key"})
        public abstract static class PythonMaxNode extends PythonBuiltinNode {

            public PythonMaxNode(String name) {
                super(name);
            }

            public PythonMaxNode(PythonMaxNode prev) {
                this(prev.getName());
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasOneArgument")
            public Object maxString(String arg1, Object[] args, Object keywordArg) {
                PString pstring = new PString(arg1);
                return pstring.getMax();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasOneArgument")
            public Object maxSequence(PSequence arg1, Object[] args, Object keywordArg) {
                return arg1.getMax();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasOneArgument")
            public Object maxBaseSet(PBaseSet arg1, Object[] args, Object keywordArg) {
                return arg1.getMax();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasOneArgument")
            public Object maxDictionary(PDict arg1, Object[] args, Object keywordArg) {
                return arg1.getMax();
            }

            @Specialization
            public Object maxGeneric(Object arg1, Object[] args, Object keywordArg) {
                if (keywordArg instanceof PNone) {
                    if (arg1 instanceof Iterable) {
                        throw new RuntimeException("Multiple iterables are not supported");
                    } else if (args.length == 1) {
                        return getMax(arg1, args[0]);
                    } else {
                        Object[] argsArray = new Object[args.length + 1];
                        argsArray[0] = arg1;
                        System.arraycopy(args, 0, argsArray, 1, args.length);
                        Object max = getMax(argsArray);
                        return max;
                    }
                } else {
                    throw new RuntimeException("Optional keyword-only key argument is not supported");
                }

            }

            private static Object getMax(Object arg1, Object arg2) {
                if (arg1 instanceof Integer) {
                    int arg1Int = (Integer) arg1;
                    if (arg2 instanceof Integer) {
                        int arg2Int = (Integer) arg2;
                        return Math.max(arg1Int, arg2Int);
                    }
                } else if (arg1 instanceof Double) {
                    double arg1Double = (Double) arg1;
                    if (arg2 instanceof Integer || arg2 instanceof Double) {
                        double arg2Double = (Double) arg2;
                        return Math.max(arg1Double, arg2Double);
                    }
                }
                throw new RuntimeException("Unsupported min operation");
            }

            private static Object getMax(Object[] args) {
                Arrays.sort(args);
                return args[args.length - 1];
            }

            @SuppressWarnings("unused")
            public static boolean hasOneArgument(Object arg1, Object[] args, Object keywordArg) {
                return (args.length == 0 && keywordArg instanceof PNone);
            }

        }

        // min(iterable, *[, key])
        // min(arg1, arg2, *args[, key])
        @Builtin(name = "min", minNumOfArguments = 1, takesKeywordArguments = true, takesVariableArguments = true, keywordNames = {"key"})
        public abstract static class PythonMinNode extends PythonBuiltinNode {

            public PythonMinNode(String name) {
                super(name);
            }

            public PythonMinNode(PythonMinNode prev) {
                this(prev.getName());
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasOneArgument")
            public Object minString(String arg1, Object[] args, Object keywordArg) {
                PString pstring = new PString(arg1);
                return pstring.getMin();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasOneArgument")
            public Object minSequence(PSequence arg1, Object[] args, Object keywordArg) {
                return arg1.getMin();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasOneArgument")
            public Object minBaseSet(PBaseSet arg1, Object[] args, Object keywordArg) {
                return arg1.getMin();
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasOneArgument")
            public Object minDictionary(PDict arg1, Object[] args, Object keywordArg) {
                return arg1.getMin();
            }

            @Specialization
            public Object minGeneric(Object arg1, Object[] args, Object keywordArg) {
                if (keywordArg instanceof PNone) {
                    if (arg1 instanceof Iterable) {
                        throw new RuntimeException("Multiple iterables are not supported");
                    } else if (args.length == 1) {
                        return getMin(arg1, args[0]);
                    } else {
                        Object[] argsArray = new Object[args.length + 1];
                        argsArray[0] = arg1;
                        System.arraycopy(args, 0, argsArray, 1, args.length);
                        Object min = getMin(argsArray);
                        return min;
                    }
                } else {
                    throw new RuntimeException("Optional keyword-only key argument is not supported");
                }
            }

            private static Object getMin(Object arg1, Object arg2) {
                if (arg1 instanceof Integer) {
                    int arg1Int = (Integer) arg1;
                    if (arg2 instanceof Integer) {
                        int arg2Int = (Integer) arg2;
                        return Math.min(arg1Int, arg2Int);
                    }
                } else if (arg1 instanceof Double) {
                    double arg1Double = (Double) arg1;
                    if (arg2 instanceof Integer || arg2 instanceof Double) {
                        double arg2Double = (Double) arg2;
                        return Math.min(arg1Double, arg2Double);
                    }
                }
                throw new RuntimeException("Unsupported min operation");
            }

            private static Object getMin(Object[] args) {
                Object[] copy = args;
                Arrays.sort(copy);
                return copy[0];
            }

            @SuppressWarnings("unused")
            public static boolean hasOneArgument(Object arg1, Object[] args, Object keywordArg) {
                return (args.length == 0 && keywordArg instanceof PNone);
            }

        }

        // next(iterator[, default])
        @Builtin(name = "next", minNumOfArguments = 1, maxNumOfArguments = 2)
        public abstract static class PythonNextNode extends PythonBuiltinNode {

            public PythonNextNode(String name) {
                super(name);
            }

            public PythonNextNode(PythonNextNode prev) {
                this(prev.getName());
            }

            @SuppressWarnings("unused")
            @Specialization
            public Object next(PIterator iterator, Object defaultObject) {
                return iterator.__next__();
            }

            @SuppressWarnings("unused")
            @Specialization
            public Object next(Object iterator, Object defaultObject) {
                throw new RuntimeException("Unsupported iterator " + iterator);
            }

            @SuppressWarnings("unused")
            public static boolean noDefault(Object object, Object defaultObject) {
                return (defaultObject instanceof PNone);
            }
        }

        // print(*objects, sep=' ', end='\n', file=sys.stdout, flush=False)
        @Builtin(name = "print", minNumOfArguments = 0, takesKeywordArguments = true, takesVariableArguments = true, takesVariableKeywords = true, keywordNames = {"sep", "end", "file", "flush"}, requiresContext = true)
        public abstract static class PythonPrintNode extends PythonBuiltinNode {

            private final PythonContext context;

            public PythonPrintNode(String name, PythonContext context) {
                super(name);
                this.context = context;
            }

            public PythonPrintNode(PythonPrintNode prev) {
                this(prev.getName(), prev.context);
            }

            @Specialization
            public Object print(Object[] values, Object[] keywords) {
                String sep = null;
                String end = null;

                if (keywords != null) {
                    for (int i = 0; i < keywords.length; i++) { // not support file
                        PKeyword keyword = (PKeyword) keywords[i];
                        if (keyword.getName().equals("end")) {
                            end = (String) keyword.getValue();
                        } else if (keyword.getName().equals("sep")) {
                            sep = (String) keyword.getValue();
                        }
                    }
                }

                return print(values, sep, end);
            }

            @SlowPath
            private Object print(Object[] values, String possibleSep, String possibleEnd) {
                String sep = possibleSep;
                String end = possibleEnd;
                // CheckStyle: stop system..print check
                if (values.length == 0) {
                    context.getStandardOut().print(System.getProperty("line.separator"));
                } else {
                    if (sep == null) {
                        sep = "";
                    }

                    if (end == null) {
                        end = System.getProperty("line.separator");
                    }

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < values.length - 1; i++) {
                        if (values[i] instanceof Boolean) {
                            sb.append(((boolean) values[i] ? "True" : "False") + " ");
                        } else {
                            sb.append(values[i] + " ");
                        }
                    }

                    if (values[values.length - 1] instanceof Boolean) {
                        sb.append(((boolean) values[values.length - 1] ? "True" : "False"));
                    } else {
                        sb.append(values[values.length - 1]);
                    }

                    context.getStandardOut().print(sb.toString() + sep + end);

                }
                // CheckStyle: resume system..print check
                return null;
            }
        }

        @SlowPath
        private static void typeError(String message) {
            throw Py.TypeError(message);
        }
    }

    public static class PythonBuiltinClasses {

        // bool([x])
        @Builtin(name = "bool", minNumOfArguments = 0, maxNumOfArguments = 1, isClass = true)
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

        // complex([real[, imag]])
        @Builtin(name = "complex", minNumOfArguments = 0, maxNumOfArguments = 2, isClass = true)
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

        // dict(**kwarg)
        // dict(mapping, **kwarg)
        // dict(iterable, **kwarg)
        @Builtin(name = "dict", minNumOfArguments = 0, takesVariableArguments = true, isClass = true)
        public abstract static class PythonDictionaryNode extends PythonBuiltinNode {

            public PythonDictionaryNode(String name) {
                super(name);
            }

            public PythonDictionaryNode(PythonDictionaryNode prev) {
                this(prev.getName());
            }

            @Specialization
            public PDict dictionary(Object[] args) {
                if (args.length == 0) {
                    return new PDict();
                } else {
                    Object arg = args[0];

                    if (arg instanceof PDict) {
                        // argument is a mapping type
                        return new PDict(((PDict) arg).getMap());
                    } else if (arg instanceof PSequence) {
                        // iterator type
                        PIterator iter = ((PSequence) arg).__iter__();
                        Map<Object, Object> newMap = new HashMap<>();

                        try {
                            while (true) {
                                Object obj = iter.__next__();
                                if (obj instanceof PSequence && ((PSequence) obj).len() == 2) {
                                    newMap.put(((PSequence) obj).getItem(0), ((PSequence) obj).getItem(1));
                                } else {
                                    throw new RuntimeException("invalid args for dict()");
                                }
                            }
                        } catch (StopIterationException e) {
                            // fall through
                        }

                        return new PDict(newMap);
                    } else {
                        throw new RuntimeException("invalid args for dict()");
                    }
                }
            }
        }

        // enumerate(iterable, start=0)
        @Builtin(name = "enumerate", hasFixedNumOfArguments = true, fixedNumOfArguments = 1, takesKeywordArguments = true, keywordNames = {"start"}, isClass = true)
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

            @SuppressWarnings("unused")
            @Specialization(guards = "noKeywordArg")
            public PEnumerate enumerate(String str, Object keywordArg) {
                PString pstr = new PString(str);
                return new PEnumerate(pstr);
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "noKeywordArg")
            public PEnumerate enumerate(PSequence sequence, Object keywordArg) {
                return new PEnumerate(sequence);
            }

            @SuppressWarnings("unused")
            @Specialization
            public PEnumerate enumerate(PBaseSet set, Object keywordArg) {
                return new PEnumerate(set);
            }

            @Specialization
            public PEnumerate enumerate(Object arg, Object keywordArg) {
                CompilerAsserts.neverPartOfCompilation();
                if (keywordArg instanceof PNone) {
                    if (arg instanceof String) {
                        String str = (String) arg;
                        PString pstr = new PString(str);
                        return new PEnumerate(pstr);
                    } else if (arg instanceof PSequence) {
                        PSequence sequence = (PSequence) arg;
                        return new PEnumerate(sequence);
                    } else if (arg instanceof PBaseSet) {
                        PBaseSet baseSet = (PBaseSet) arg;
                        return new PEnumerate(baseSet);
                    }

                    if (!(arg instanceof Iterable<?>)) {
                        throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
                    } else {
                        throw new RuntimeException("enumerate does not support iterable object " + arg);
                    }

                } else {
                    throw new RuntimeException("enumerate does not support keyword argument " + keywordArg);
                }
            }

            @SuppressWarnings("unused")
            public static boolean noKeywordArg(Object arg, Object keywordArg) {
                return (keywordArg instanceof PNone);
            }
        }

        // float([x])
        @Builtin(name = "float", minNumOfArguments = 0, maxNumOfArguments = 1, isClass = true)
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
        @Builtin(name = "frozenset", minNumOfArguments = 0, maxNumOfArguments = 1, isClass = true)
        public abstract static class PythonFrozenSetNode extends PythonBuiltinNode {

            public PythonFrozenSetNode(String name) {
                super(name);
            }

            public PythonFrozenSetNode(PythonFrozenSetNode prev) {
                this(prev.getName());
            }

            @Specialization
            public PFrozenSet frozenset(String arg) {
                return new PFrozenSet(new PStringIterator(arg));
            }

            @Specialization
            public PFrozenSet frozenset(PSequence sequence) {
                return new PFrozenSet(sequence.__iter__());
            }

            @Specialization
            public PFrozenSet frozenset(PBaseSet baseSet) {
                return new PFrozenSet(baseSet);
            }

            @SuppressWarnings("unused")
            @Specialization
            public PFrozenSet frozenset(Object arg) {
                return new PFrozenSet();
            }
        }

        // int(x=0)
        // int(x, base=10)
        @Builtin(name = "int", minNumOfArguments = 0, maxNumOfArguments = 1, takesKeywordArguments = true, keywordNames = {"base"}, isClass = true)
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

        // list([iterable])
        @Builtin(name = "list", minNumOfArguments = 0, maxNumOfArguments = 1, isClass = true)
        public abstract static class PythonListNode extends PythonBuiltinNode {

            public PythonListNode(String name) {
                super(name);
            }

            public PythonListNode(PythonListNode prev) {
                this(prev.getName());
            }

            @Specialization
            public PList listString(String arg) {
                char[] chars = arg.toCharArray();
                PList list = new PList();

                for (char c : chars) {
                    list.append(c);
                }

                return list;
            }

            @Specialization
            public PList listRange(PRange range) {
                return new PList(range.__iter__());
            }

            @Specialization
            public PList listSequence(PSequence sequence) {
                return new PList(sequence.getStorage().copy());
            }

            @Specialization
            public PList listSet(PBaseSet baseSet) {
                return new PList(baseSet.__iter__());
            }

            @Specialization
            public PList listEnumerate(PEnumerate enumerate) {
                return new PList(enumerate.__iter__());
            }

            @Specialization
            public PList listZip(PZip zip) {
                return new PList(zip.__iter__());
            }

            @Specialization
            public PList listObject(Object arg) {
                CompilerAsserts.neverPartOfCompilation();
                /**
                 * This is not ideal!<br>
                 * Truffle DSL does not support polymorphism for built-ins. It would be better if we
                 * can rewrite the node by ourself.
                 */
                if (arg instanceof String) {
                    return listString((String) arg);
                } else if (arg instanceof PRange) {
                    return listRange((PRange) arg);
                } else if (arg instanceof PSequence) {
                    return listSequence((PSequence) arg);
                } else if (arg instanceof PBaseSet) {
                    return listSet((PBaseSet) arg);
                } else if (arg instanceof PEnumerate) {
                    return listEnumerate((PEnumerate) arg);
                } else if (arg instanceof PZip) {
                    return listZip((PZip) arg);
                }

                if (!(arg instanceof Iterable<?>)) {
                    throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
                } else {
                    throw new RuntimeException("list does not support iterable object " + arg);
                }
            }
        }

        // map(function, iterable, ...)
        @Builtin(name = "map", minNumOfArguments = 2, takesVariableArguments = true, isClass = true)
        public abstract static class PythonMapNode extends PythonBuiltinNode {

            public PythonMapNode(String name) {
                super(name);
            }

            public PythonMapNode(PythonMapNode prev) {
                this(prev.getName());
            }

            @SuppressWarnings("unused")
            @Specialization
            public Object mapString(PythonCallable arg0, String arg1, Object[] iterators) {
                return doMap(arg0, new PString(arg1).__iter__());
            }

            @SuppressWarnings("unused")
            @Specialization
            public Object mapSequence(PythonCallable arg0, PSequence arg1, Object[] iterators) {
                return doMap(arg0, arg1.__iter__());
            }

            private static PList doMap(PythonCallable mappingFunction, PIterator iter) {
                PList list = new PList();

                try {
                    while (true) {
                        list.append(mappingFunction.call(null, new Object[]{iter.__next__()}));
                    }
                } catch (StopIterationException e) {

                }

                return list;
            }
        }

        // range(stop)
        // range(start, stop[, step])
        @Builtin(name = "range", minNumOfArguments = 1, maxNumOfArguments = 3, isClass = true)
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
                if (start instanceof Integer) {
                    int intStart = (int) start;
                    if (stop instanceof PNone) {
                        return new PRange(intStart);
                    } else if (stop instanceof Integer) {
                        int intStop = (int) stop;
                        if (step instanceof PNone) {
                            return new PRange(intStart, intStop);
                        } else {
                            int intStep = (int) step;
                            return new PRange(intStart, intStop, intStep);
                        }
                    }
                }

                throw Py.TypeError("range does not support " + start + ", " + stop + ", " + step);
            }

            @SuppressWarnings("unused")
            public static boolean caseStop(int stop, Object start, Object step) {
                return start == PNone.NONE && step == PNone.NONE;
            }

            @SuppressWarnings("unused")
            public static boolean caseStartStop(int start, int stop, Object step) {
                return step == PNone.NONE;
            }
        }

        // set([iterable])
        @Builtin(name = "set", minNumOfArguments = 0, maxNumOfArguments = 1, isClass = true)
        public abstract static class PythonSetNode extends PythonBuiltinNode {

            public PythonSetNode(String name) {
                super(name);
            }

            public PythonSetNode(PythonSetNode prev) {
                this(prev.getName());
            }

            @Specialization
            public PSet set(String arg) {
                return new PSet(new PStringIterator(arg));
            }

            @Specialization
            public PSet set(PSequence sequence) {
                return new PSet(sequence.__iter__());
            }

            @Specialization
            public PSet set(PBaseSet baseSet) {
                return new PSet(baseSet);
            }

            @Specialization
            public PSet set(Object arg) {
                if (arg instanceof String) {
                    String str = (String) arg;
                    return new PSet(new PStringIterator(str));
                } else if (arg instanceof PSequence) {
                    PSequence sequence = (PSequence) arg;
                    return new PSet(sequence.__iter__());
                } else if (arg instanceof PBaseSet) {
                    PBaseSet baseSet = (PBaseSet) arg;
                    return new PSet(baseSet);
                }

                if (!(arg instanceof Iterable<?>)) {
                    throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
                } else {
                    throw new RuntimeException("set does not support iterable object " + arg);
                }
            }
        }

        // str(object='')
        // str(object=b'', encoding='utf-8', errors='strict')
        @Builtin(name = "str", minNumOfArguments = 0, maxNumOfArguments = 1, takesKeywordArguments = true, takesVariableKeywords = true, keywordNames = {"object, encoding, errors"}, isClass = true)
        public abstract static class PythonStrNode extends PythonBuiltinNode {

            public PythonStrNode(String name) {
                super(name);
            }

            public PythonStrNode(PythonStrNode prev) {
                this(prev.getName());
            }

            @Specialization
            public String str(Object arg) {
                return arg.toString();
            }
        }

        // tuple([iterable])
        @Builtin(name = "tuple", minNumOfArguments = 0, maxNumOfArguments = 1, isClass = true)
        public abstract static class PythonTupleNode extends PythonBuiltinNode {

            public PythonTupleNode(String name) {
                super(name);
            }

            public PythonTupleNode(PythonTupleNode prev) {
                this(prev.getName());
            }

            @Specialization
            public PTuple tuple(String arg) {
                return new PTuple(new PStringIterator(arg));
            }

            @Specialization(order = 2)
            public PTuple tuple(PRange range) {
                return new PTuple(range.__iter__());
            }

            @Specialization(order = 3)
            public PTuple tuple(PSequence sequence) {
                return new PTuple(sequence.__iter__());
            }

            @Specialization
            public PTuple tuple(PBaseSet baseSet) {
                return new PTuple(baseSet.__iter__());
            }

            @Specialization
            public PTuple tuple(Object arg) {
                if (arg instanceof String) {
                    String str = (String) arg;
                    return new PTuple(new PStringIterator(str));
                } else if (arg instanceof PSequence) {
                    PSequence sequence = (PSequence) arg;
                    return new PTuple(sequence.__iter__());
                } else if (arg instanceof PBaseSet) {
                    PBaseSet baseSet = (PBaseSet) arg;
                    return new PTuple(baseSet.__iter__());
                }

                if (!(arg instanceof Iterable<?>)) {
                    throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
                } else {
                    throw new RuntimeException("tuple does not support iterable object " + arg);
                }
            }
        }

        // zip(*iterables)
        @Builtin(name = "zip", minNumOfArguments = 0, takesVariableArguments = true, isClass = true)
        public abstract static class PythonZipNode extends PythonBuiltinNode {

            public PythonZipNode(String name) {
                super(name);
            }

            public PythonZipNode(PythonZipNode prev) {
                this(prev.getName());
            }

            @Specialization
            public PZip zip(Object[] args) {
                PIterable[] iterables = new PIterable[args.length];

                for (int i = 0; i < args.length; i++) {
                    iterables[i] = getIterable(args[i]);
                }

                return new PZip(iterables);
            }

            private static PIterable getIterable(Object arg) {
                if (arg instanceof String) {
                    String str = (String) arg;
                    PString pstr = new PString(str);
                    return pstr;
                } else if (arg instanceof PSequence) {
                    PSequence sequence = (PSequence) arg;
                    return sequence;
                } else if (arg instanceof PBaseSet) {
                    PBaseSet baseSet = (PBaseSet) arg;
                    return baseSet;
                }

                if (!(arg instanceof Iterable<?>)) {
                    throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
                } else {
                    throw new RuntimeException("zip does not support iterable object " + arg.getClass());
                }

            }
        }
    }

}
