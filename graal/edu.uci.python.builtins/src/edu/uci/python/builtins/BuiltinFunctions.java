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

import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.misc.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.CompilerDirectives.SlowPath;

/**
 * @author Gulfem
 * @author zwei
 */
public final class BuiltinFunctions extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return BuiltinFunctionsFactory.getFactories();
    }

    // abs(x)
    @Builtin(name = "abs", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class PythonAbsNode extends PythonBuiltinNode {

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

        @Specialization
        public boolean all(PIterable iterable) {
            if (iterable.len() == 0) {
                return false;
            }

            PIterator iterator = iterable.__iter__();

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
            throw new RuntimeException("all does not support iterable object " + object);
        }
    }

    // any(iterable)
    @Builtin(name = "any", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class PythonAnyNode extends PythonBuiltinNode {

        @Specialization
        public boolean any(PIterable iterable) {
            if (iterable.len() == 0) {
                return false;
            }

            PIterator iterator = iterable.__iter__();

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
            throw new RuntimeException("any does not support iterable object " + object);
        }
    }

    // callable(object)
    @Builtin(name = "callable", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class PythonCallableNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(order = 1)
        public boolean callable(PythonCallable callable) {
            return true;
        }

        @Specialization
        public boolean callable(Object object) {
            return object instanceof PythonCallable;
        }
    }

    // chr(i)
    @Builtin(name = "chr", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class PythonChrNode extends PythonBuiltinNode {

        @Specialization
        public String charFromInt(int arg) {
            return Character.toString((char) arg);
        }

        @Specialization
        public char charFromObject(Object arg) {
            if (arg instanceof Double) {
                throw Py.TypeError("integer argument expected, got float");
            }

            throw Py.TypeError("an integer is required");
        }
    }

    // isinstance(object, classinfo)
    @Builtin(name = "isinstance", hasFixedNumOfArguments = true, fixedNumOfArguments = 2)
    public abstract static class PythonIsIntanceNode extends PythonBuiltinNode {

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

        @SuppressWarnings("unused")
        @Specialization(order = 1)
        public Object iter(String str, PNone sentinel) {
            return new PStringIterator(str);
        }

        @SuppressWarnings("unused")
        @Specialization(order = 2)
        public Object iter(PIterable iterable, PNone sentinel) {
            return iterable.__iter__();
        }

        @SuppressWarnings("unused")
        @Specialization()
        public Object iter(Object object, Object sentinel) {
            throw new RuntimeException("Not supported sentinel case");
        }
    }

    // len(s)
    @Builtin(name = "len", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class PythonLenNode extends PythonBuiltinNode {

        @Specialization
        public int len(String arg) {
            return arg.length();
        }

        @Specialization(order = 1)
        public int len(PIterable iterable) {
            return iterable.len();
        }

        @Generic
        public int len(Object arg) {
            throw Py.TypeError("object of type '" + PythonTypesUtil.getPythonTypeName(arg) + "' has no len()");
        }
    }

    // max(iterable, *[, key])
    // max(arg1, arg2, *args[, key])
    @Builtin(name = "max", minNumOfArguments = 1, takesKeywordArguments = true, takesVariableArguments = true, keywordNames = {"key"})
    public abstract static class PythonMaxNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(order = 1, guards = "hasOneArgument")
        public Object maxSequence(PSequence arg1, Object[] args, Object keywordArg) {
            return arg1.getMax();
        }

        @SuppressWarnings("unused")
        @Specialization(order = 2, guards = "hasOneArgument")
        public Object maxBaseSet(PBaseSet arg1, Object[] args, Object keywordArg) {
            return arg1.getMax();
        }

        @SuppressWarnings("unused")
        @Specialization(order = 3, guards = "hasOneArgument")
        public Object maxDictionary(PDict arg1, Object[] args, Object keywordArg) {
            return arg1.getMax();
        }

        /**
         * Incomplete. Only deals with ints now.
         */
        @SuppressWarnings("unused")
        @Specialization(order = 4)
        public Object maxPIterator(PIterator arg1, Object[] args, PNone keywordArg) {
            int max = Integer.MIN_VALUE;

            try {
                while (true) {
                    int item = (int) arg1.__next__();
                    max = Math.max(max, item);
                }
            } catch (StopIterationException e) {
            }

            return max;
        }

        @Specialization(order = 5)
        public Object maxGeneric(Object arg1, Object[] args, Object keywordArg) {
            if (keywordArg instanceof PNone) {
                if (args.length == 1) {
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

        @SuppressWarnings("unused")
        @Specialization
        public Object next(PIterator iterator, PNone defaultObject) {
            return iterator.__next__();
        }

        @SuppressWarnings("unused")
        @Specialization
        public Object next(Object iterator, Object defaultObject) {
            throw new RuntimeException("Unsupported iterator " + iterator);
        }
    }

    // print(*objects, sep=' ', end='\n', file=sys.stdout, flush=False)
    @Builtin(name = "print", minNumOfArguments = 0, takesKeywordArguments = true, takesVariableArguments = true, takesVariableKeywords = true, keywordNames = {"sep", "end", "file", "flush"}, requiresContext = true)
    public abstract static class PythonPrintNode extends PythonBuiltinNode {

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
                getContext().getStandardOut().print(System.getProperty("line.separator"));
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

                getContext().getStandardOut().print(sb.toString() + sep + end);

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
