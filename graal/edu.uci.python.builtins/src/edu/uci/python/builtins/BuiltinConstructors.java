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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.misc.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

/**
 * @author Gulfem
 * @author zwei
 *
 */
public final class BuiltinConstructors extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return BuiltinConstructorsFactory.getFactories();
    }

    // bool([x])
    @Builtin(name = "bool", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    public abstract static class BoolNode extends PythonBuiltinNode {

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
    @Builtin(name = "complex", minNumOfArguments = 0, maxNumOfArguments = 2, isConstructor = true)
    public abstract static class ComplexNode extends PythonBuiltinNode {

        @Specialization
        public PComplex complexFromDoubleDouble(double real, double imaginary) {
            return new PComplex(real, imaginary);
        }

        @SuppressWarnings("unused")
        @Specialization
        public PComplex complexFromDouble(double real, PNone image) {
            return new PComplex(real, 0);
        }

        @SuppressWarnings("unused")
        @Specialization
        public PComplex complexFromNone(PNone real, PNone image) {
            return new PComplex(0, 0);
        }

        @Specialization
        public PComplex complexFromObjectObject(Object real, Object imaginary) {
            if (real instanceof String) {
                if (!(imaginary instanceof PNone)) {
                    throw Py.TypeError("complex() can't take second arg if first is a string");
                }

                String realPart = (String) real;
                return JavaTypeConversions.convertStringToComplex(realPart);
            }

            throw Py.TypeError("can't convert real " + real + " imag " + imaginary);
        }
    }

    // dict(**kwarg)
    // dict(mapping, **kwarg)
    // dict(iterable, **kwarg)
    @Builtin(name = "dict", minNumOfArguments = 0, takesVariableArguments = true, isConstructor = true)
    public abstract static class DictionaryNode extends PythonBuiltinNode {

        protected static boolean emptyArgument(PTuple args) {
            return args.len() == 0;
        }

        protected static boolean oneArgument(PTuple args) {
            return args.len() == 1;
        }

        protected static boolean firstArgIsDict(PTuple args) {
            return args.getItem(0) instanceof PDict;
        }

        protected static boolean firstArgIsIterable(PTuple args) {
            return args.getItem(0) instanceof PIterable;
        }

        protected static boolean firstArgIsIterator(PTuple args) {
            return args.getItem(0) instanceof PIterator;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 0, guards = "emptyArgument")
        public PDict dictEmpty(PTuple args) {
            return new PDict();
        }

        @Specialization(order = 1, guards = {"oneArgument", "firstArgIsDict"})
        public PDict dictFromDict(PTuple args) {
            return new PDict(((PDict) args.getItem(0)).getMap());
        }

        @Specialization(order = 2, guards = {"oneArgument", "firstArgIsIterable"})
        public PDict dictFromIterable(PTuple args) {
            PIterable iterable = (PIterable) args.getItem(0);
            PIterator iter = iterable.__iter__();
            return new PDict(iter);
        }

        @Specialization(order = 3, guards = {"oneArgument", "firstArgIsIterator"})
        public PDict dictFromIterator(PTuple args) {
            PIterator iter = (PIterator) args.getItem(0);
            return new PDict(iter);
        }

        @SuppressWarnings("unused")
        @Generic
        public PDict dictionary(Object args) {
            throw new RuntimeException("invalid args for dict()");
        }
    }

    // enumerate(iterable, start=0)
    @Builtin(name = "enumerate", hasFixedNumOfArguments = true, fixedNumOfArguments = 1, takesKeywordArguments = true, keywordNames = {"start"}, isConstructor = true)
    public abstract static class EnumerateNode extends PythonBuiltinNode {
        /**
         * TODO enumerate can take a keyword argument start, and currently that's not supported.
         */

        @SuppressWarnings("unused")
        @Specialization(order = 1)
        public PEnumerate enumerate(String str, PNone keywordArg) {
            PString pstr = new PString(str);
            return new PEnumerate(pstr);
        }

        @SuppressWarnings("unused")
        @Specialization(order = 2)
        public PEnumerate enumerate(PIterable iterable, PNone keywordArg) {
            return new PEnumerate(iterable);
        }

        @Specialization
        public PEnumerate enumerate(Object arg, Object keywordArg) {
            CompilerAsserts.neverPartOfCompilation();
            if (keywordArg instanceof PNone) {
                throw new RuntimeException("enumerate does not support iterable object " + arg);
            } else {
                throw new RuntimeException("enumerate does not support keyword argument " + keywordArg);
            }
        }
    }

    // float([x])
    @Builtin(name = "float", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    public abstract static class FloatNode extends PythonBuiltinNode {

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
    @Builtin(name = "frozenset", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    public abstract static class FrozenSetNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(order = 0, guards = "emptyArguments")
        public PFrozenSet frozensetEmpty(Object arg) {
            return new PFrozenSet();
        }

        @Specialization
        public PFrozenSet frozenset(String arg) {
            return new PFrozenSet(new PStringIterator(arg));
        }

        @Specialization
        public PFrozenSet frozenset(PBaseSet baseSet) {
            return new PFrozenSet(baseSet);
        }

        @Specialization
        public PFrozenSet frozensetSequence(PSequence sequence) {
            return new PFrozenSet(sequence.__iter__());
        }

        @Specialization
        public PFrozenSet frozensetIterator(PIterator iterator) {
            PFrozenSet set = new PFrozenSet(iterator);
            return set;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 10)
        public PFrozenSet frozenset(Object arg) {
            throw new UnsupportedOperationException();
        }
    }

    // int(x=0)
    // int(x, base=10)
    @Builtin(name = "int", minNumOfArguments = 0, maxNumOfArguments = 1, takesKeywordArguments = true, keywordNames = {"base"}, isConstructor = true)
    public abstract static class IntNode extends PythonBuiltinNode {

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
    @Builtin(name = "list", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    public abstract static class ListNode extends PythonBuiltinNode {

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
        public PList listSequence(PList list) {
            return new PList(list.getStorage().copy());
        }

        @Specialization
        public PList listIterator(PIterator iterator) {
            return new PList(iterator);
        }

        @Specialization
        public PList listIterable(PIterable iterable) {
            return new PList(iterable.__iter__());
        }

        @Specialization
        public PList listObject(Object arg) {
            CompilerAsserts.neverPartOfCompilation();
            throw new RuntimeException("list does not support iterable object " + arg);
        }
    }

    // map(function, iterable, ...)
    @Builtin(name = "map", minNumOfArguments = 2, takesVariableArguments = true, isConstructor = true)
    public abstract static class MapNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(order = 1)
        public Object mapString(PythonCallable function, String str, PTuple iterators) {
            return doMap(function, new PString(str).__iter__());
        }

        @SuppressWarnings("unused")
        @Specialization(order = 2)
        public Object mapFunctionIterable(PythonCallable function, PIterable iterable, PTuple iterators) {
            return doMap(function, iterable.__iter__());
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

        @SuppressWarnings("unused")
        @Specialization(order = 3)
        public Object mapClassIterable(PythonClass clazz, PIterable iterable, PTuple iterators) {
            PIterator iter = iterable.__iter__();
            PList list = new PList();

            try {
                while (true) {
                    Object item = iter.__next__();
                    PythonObject obj = new PythonObject(clazz);
                    Object[] selfWithArgs = new Object[2];

                    selfWithArgs[0] = obj;
                    selfWithArgs[1] = item;

                    PythonCallable initMethod = clazz.lookUpMethod("__init__");
                    if (initMethod != null) {
                        initMethod.call(null, selfWithArgs);
                        list.append(obj);
                    }

                }
            } catch (StopIterationException e) {

            }

            return list;
        }

        @SuppressWarnings("unused")
        @Specialization
        public Object mapSequence(Object function, Object iterable, PTuple iterators) {
            throw new RuntimeException("map is not supported for " + function + " " + function.getClass() + " iterable " + iterable + " " + iterable.getClass());
        }
    }

    // object()
    @Builtin(name = "object", fixedNumOfArguments = 0, isConstructor = true)
    public abstract static class ObjectNode extends PythonBuiltinNode {

        @Specialization
        public PythonObject doObject() {
            PythonContext context = getContext();
            return new PythonObject(context.getObjectClass());
        }
    }

    // range(stop)
    // range(start, stop[, step])
    @Builtin(name = "range", minNumOfArguments = 1, maxNumOfArguments = 3, isConstructor = true)
    public abstract static class RangeNode extends PythonBuiltinNode {

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
    @Builtin(name = "set", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    public abstract static class SetNode extends PythonBuiltinNode {

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
        public PSet set(PIterator iterator) {
            return new PSet(iterator);
        }

        @Specialization(guards = "emptyArguments")
        public PSet set(@SuppressWarnings("unused") PNone none) {
            return new PSet();
        }

        @Specialization
        public PSet set(Object arg) {
            if (!(arg instanceof Iterable<?>)) {
                throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
            } else {
                throw new RuntimeException("set does not support iterable object " + arg);
            }
        }
    }

    // str(object='')
    // str(object=b'', encoding='utf-8', errors='strict')
    @Builtin(name = "str", minNumOfArguments = 0, maxNumOfArguments = 1, takesKeywordArguments = true, takesVariableKeywords = true, keywordNames = {"object, encoding, errors"}, isConstructor = true)
    public abstract static class StrNode extends PythonBuiltinNode {

        @Specialization
        public String str(PythonObject obj) {
            return PythonBuiltinNode.callAttributeSlowPath(obj, "__str__");
        }

        @Specialization
        public String str(Object arg) {
            return arg.toString();
        }
    }

    // tuple([iterable])
    @Builtin(name = "tuple", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    public abstract static class TupleNode extends PythonBuiltinNode {

        @Specialization(order = 1)
        public PTuple tuple(String arg) {
            return new PTuple(new PStringIterator(arg));
        }

        @Specialization(order = 2)
        public PTuple tuple(PIterable iterable) {
            return new PTuple(iterable.__iter__());
        }

        @Specialization(order = 3)
        public PTuple tuple(PIterator iterator) {
            return new PTuple(iterator);
        }

        @Specialization
        public PTuple tuple(Object arg) {
            throw new RuntimeException("tuple does not support iterable object " + arg);
        }
    }

    // zip(*iterables)
    @Builtin(name = "zip", minNumOfArguments = 0, takesVariableArguments = true, isConstructor = true)
    public abstract static class ZipNode extends PythonBuiltinNode {

        @Specialization
        public PZip zip(PTuple args) {
            PIterable[] iterables = new PIterable[args.len()];

            for (int i = 0; i < args.len(); i++) {
                iterables[i] = getIterable(args.getItem(i));
            }

            return new PZip(iterables);
        }

        private static PIterable getIterable(Object arg) {
            if (arg instanceof PIterable) {
                return (PIterable) arg;
            } else if (arg instanceof String) {
                return new PString((String) arg);
            }

            throw new RuntimeException("zip does not support iterable object " + arg.getClass());
        }
    }

}
