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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.modules.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtypes.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.CompilerDirectives.SlowPath;

/**
 * @author Gulfem
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

            @Specialization
            public char charFromInt(Object arg) {
                if (arg instanceof Double) {
                    throw Py.TypeError("integer argument expected, got float");
                }

                throw Py.TypeError("an integer is required");
            }
        }

        // enumerate(iterable, start=0)
        @Builtin(name = "enumerate", hasFixedNumOfArguments = true, fixedNumOfArguments = 1, takesKeywordArguments = true, keywordNames = {"start"})
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
            @Specialization
            // @Specialization(guards = "noKeywordArg")
            public PIterator enumerate(String str, Object keywordArg) {
                return new PEnumerate(new PString(str));
            }

            @SuppressWarnings("unused")
            @Specialization
            // @Specialization(guards = "noKeywordArg")
            public PIterator enumerate(PSequence sequence, Object keywordArg) {
                return new PEnumerate(sequence);
            }

            @SuppressWarnings("unused")
            @Specialization
            // @Specialization(guards = "noKeywordArg")
            public PIterator enumerate(PBaseSet set, Object keywordArg) {
                return new PEnumerate(set);
            }

            @Specialization
            public PIterator enumerate(Object arg, Object keywordArg) {
                if (keywordArg instanceof PNone) {
                    if (arg instanceof String) {
                        String str = (String) arg;
                        return new PEnumerate(stringToCharList(str));
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
            @Specialization
            public Object iter(PSequence sequence, Object sentinel) {
                return sequence.__iter__();
            }

            @SuppressWarnings("unused")
            @Specialization
            public Object iter(PBaseSet set, Object sentinel) {
                return new PBaseSetIterator(set);
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

            @Specialization
            public int len(PSequence arg) {
                return arg.len();
            }

            @Specialization
            public int len(PBaseSet arg) {
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
                } else if (arg instanceof PBaseSet) {
                    PBaseSet argument = (PBaseSet) arg;
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
            @Specialization(guards = "hasTwoArguments")
            public int maxIntInt(int arg1, int arg2, Object[] args, Object keywordArg) {
                return Math.max(arg1, arg2);
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasTwoArguments")
            public double maxDoubleDouble(double arg1, double arg2, Object[] args, Object keywordArg) {
                return Math.max(arg1, arg2);
            }

            @Specialization
            public Object max(Object arg1, Object arg2, Object[] args, Object keywordArg) {
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
                    if (arg1 instanceof Integer && arg2 instanceof Integer) {
                        int arg1Int = (int) arg1;
                        int arg2Int = (int) arg2;
                        return Math.max(arg1Int, arg2Int);
                    } else if (arg1 instanceof Double && arg2 instanceof Double) {
                        double arg1Double = (Double) arg1;
                        double arg2Double = (Double) arg2;
                        return Math.max(arg1Double, arg2Double);
                    }
                }

                /**
                 * TODO Does not support var args {max(10, 20, 30, 40)} or keyword {max(10, 20, key
                 * = func)}
                 */
                throw new RuntimeException("Optional keyword-only key argument is not supported");

            }

            @SuppressWarnings("unused")
            public static boolean hasTwoArguments(Object arg1, Object arg2, Object[] args, Object keywordArg) {
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
            @Specialization(guards = "hasTwoArguments")
            public int minIntInt(int arg1, int arg2, Object[] args, Object keywordArg) {
                return Math.min(arg1, arg2);
            }

            @SuppressWarnings("unused")
            @Specialization(guards = "hasTwoArguments")
            public double minDoubleDouble(double arg1, double arg2, Object[] args, Object keywordArg) {
                return Math.min(arg1, arg2);
            }

            @Specialization
            public Object min(Object arg1, Object arg2, Object[] args, Object keywordArg) {
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
                    } else if (arg1 instanceof PBaseSet) {
                        PBaseSet baseSet = (PBaseSet) arg1;
                        return baseSet.getMin();
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
                } else {
                    Object[] copy = new Object[args.length + 2];
                    copy[0] = arg1;
                    copy[1] = arg2;
                    System.arraycopy(args, 0, copy, 2, args.length);
                    Arrays.sort(copy);
                    return copy[0];
                }

                /**
                 * TODO Does not support var args {min(10, 20, 30, 40)} or keyword {min(10, 20, key
                 * = func)}
                 */
                throw new RuntimeException("Optional keyword-only key argument is not supported");

            }

            @SuppressWarnings("unused")
            public static boolean hasTwoArguments(Object arg1, Object arg2, Object[] args, Object keywordArg) {
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
            public int next(Object iterator) {
                return 10;
            }
        }

        @Builtin(name = "print", minNumOfArguments = 0, takesKeywordArguments = true, takesVariableArguments = true, takesVariableKeywords = true, keywordNames = {"sep", "end", "file", "flush"}, requiresContext = true)
        public abstract static class PythonPrintNode extends PythonBuiltinNode {

            // private final PythonContext context;

// public PythonPrintNode(String name, PythonContext context) {
// super(name);
// this.context = context;
// }

            public PythonPrintNode(String name) {
                super(name);
            }

// public PythonPrintNode(PythonPrintNode prev) {
// this(prev.getName(), prev.context);
// }

            public PythonPrintNode(PythonPrintNode prev) {
                this(prev.getName());
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
                    System.out.println();
                } else {
                    if (sep == null) {
                        sep = "";
                    }

                    if (end == null) {
                        end = System.getProperty("line.separator");
                    }

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < values.length - 1; i++) {
                        sb.append(values[i] + " ");
                    }

                    sb.append(values[values.length - 1]);
                    // context.getStandardOut().print(sb.toString());

                    System.out.print(sb.toString() + sep + end);
                }
                // CheckStyle: resume system..print check
                return null;
            }
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
            public PDictionary dictionary(Object[] args) {
                if (args.length == 0) {
                    return new PDictionary();
                } else {
                    Object arg = args[0];

                    if (arg instanceof PDictionary) {
                        // argument is a mapping type
                        return new PDictionary(((PDictionary) arg).getMap());
                    } else if (arg instanceof PSequence) { // iterator type
                        Iterator<?> iter = ((PSequence) arg).iterator();
                        Map<Object, Object> newMap = new HashMap<>();

                        while (iter.hasNext()) {
                            Object obj = iter.next();

                            if (obj instanceof PSequence && ((PSequence) obj).len() == 2) {
                                newMap.put(((PSequence) obj).getItem(0), ((PSequence) obj).getItem(1));
                            } else {
                                throw new RuntimeException("invalid args for dict()");
                            }
                        }

                        return new PDictionary(newMap);
                    } else {
                        throw new RuntimeException("invalid args for dict()");
                    }
                }
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

            @Specialization
            public Object map(Object arg0, Object arg1, Object[] iterators) {
                if (iterators.length == 0) {
                    return map(arg0, arg1);
                }

                throw new RuntimeException("wrong number of arguments for map() ");

            }

            public PList map(Object arg0, Object arg1) {
                PythonCallable callee = (PythonCallable) arg0;
                Iterator iter = getIterable(arg1);

                ArrayList<Object> sequence = new ArrayList<>();
                while (iter.hasNext()) {
                    sequence.add(callee.call(null, new Object[]{iter.next()}));
                }

                return new PList(sequence);
            }

            @SuppressWarnings("unchecked")
            private static Iterator<Object> getIterable(Object o) {
                if (o instanceof String) {
                    return new PString((String) o).iterator();
                } else if (o instanceof Iterable) {
                    return ((Iterable<Object>) o).iterator();
                } else {
                    throw new RuntimeException("argument is not iterable ");
                }
            }
        }

        // object()
// @Builtin(name = "object", hasFixedNumOfArguments = true, fixedNumOfArguments = 0, isClass = true)
// public abstract static class PythonObjectNode extends PythonBuiltinNode {
//
// private final PythonContext context;
//
// public PythonObjectNode(String name, PythonContext context) {
// super(name);
// this.context = context;
// }
//
// public PythonObjectNode(PythonObjectNode prev) {
// this(prev.getName(), prev.context);
// }
//
// @Specialization
// public Object object() {
// return new PythonObject(context.getObjectClass());
// }
// }

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
                }

                if (!(arg instanceof Iterable<?>)) {
                    throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
                } else {
                    throw new RuntimeException("tuple does not support iterable object " + arg);
                }
            }

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
}
