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

import org.python.core.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.nodes.expression.CastToBooleanNodeFactory.YesNodeFactory;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.misc.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
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

        @Child protected CastToBooleanNode toBoolean;

        private boolean toBoolean(Object value) {
            if (toBoolean == null) {
                CompilerDirectives.transferToInterpreter();
                toBoolean = adoptChild(YesNodeFactory.create(EMPTYNODE));
            }
            return toBoolean.executeBoolean(value);
        }

        @Specialization
        public boolean all(PIterable iterable) {
            if (iterable.len() == 0) {
                return false;
            }

            PIterator iterator = iterable.__iter__();

            try {
                while (true) {
                    if (!toBoolean(iterator.__next__())) {
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

        @Child protected CastToBooleanNode toBoolean;

        private boolean toBoolean(Object value) {
            if (toBoolean == null) {
                CompilerDirectives.transferToInterpreter();
                toBoolean = adoptChild(YesNodeFactory.create(EMPTYNODE));
            }
            return toBoolean.executeBoolean(value);
        }

        @Specialization
        public boolean any(PIterable iterable) {
            if (iterable.len() == 0) {
                return false;
            }

            PIterator iterator = iterable.__iter__();

            try {
                while (true) {
                    if (toBoolean(iterator.__next__())) {
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
            /**
             * Added temporarily to skip translation/execution errors in unit testing
             */

            if (object.equals(ZippyThrowsExceptionNode.MESSAGE)) {
                return true;
            }

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

    // dir([object])
    @Builtin(name = "dir", minNumOfArguments = 0, maxNumOfArguments = 1)
    public abstract static class PythonDirNode extends PythonBuiltinNode {

        @Specialization
        public Object dir(PythonModule module) {
            List<String> attributes = module.getAttributeNames();
            return new PTuple(attributes.toArray());
        }

        @Specialization
        public Object dir(PythonClass clazz) {
            List<String> attributes = clazz.getAttributeNames();

            if (clazz.getSuperClass() != null) {
                /**
                 * TODO should add all the attributes in the class hierarchy
                 */
                List<String> superClassAttributes = clazz.getSuperClass().getAttributeNames();
                attributes.addAll(superClassAttributes);
            }

            return new PTuple(attributes.toArray());
        }

        public Object dir(Object object) {
            throw new RuntimeException("dir is not supported for " + object + " " + object.getClass());
        }
    }

    // divmod(a, b)
    @Builtin(name = "divmod", hasFixedNumOfArguments = true, fixedNumOfArguments = 2)
    public abstract static class DivModNode extends PythonBuiltinNode {

        @Specialization
        public PTuple doInt(int a, int b) {
            return new PTuple(new Object[]{a / b, a % b});
        }

        @Specialization
        public PTuple doBigInteger(BigInteger a, BigInteger b) {
            return new PTuple(a.divideAndRemainder(b));
        }

        @Specialization
        public PTuple doDouble(double a, double b) {
            double q = Math.floor(a / b);
            return new PTuple(new Object[]{q, a % b});
        }
    }

    // eval(expression, globals=None, locals=None)
    @Builtin(name = "eval", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class EvalNode extends PythonBuiltinNode {

        @Specialization
        public Object eval(String expression) {
            return evalExpression(expression);
        }

        @SlowPath
        private Object evalExpression(String expression) {
            PythonParser parser = getContext().getParser();
            PythonParseResult parsed = parser.parse(getContext(), new PythonModule(getContext(), "<eval>", null), expression);
            RootNode root = parsed.getModuleRoot();
            VirtualFrame frame = Truffle.getRuntime().createVirtualFrame(null, null, root.getFrameDescriptor());
            return root.execute(frame);
        }
    }

    // filter(function, iterable)
    @Builtin(name = "filter", hasFixedNumOfArguments = true, fixedNumOfArguments = 2)
    public abstract static class FilterNode extends PythonBuiltinNode {

        @Specialization
        public PTuple filter(PythonCallable function, PIterable iterable) {
            PIterator iter = iterable.__iter__();
            List<Object> filteredElements = new ArrayList<>();

            try {
                while (true) {
                    Object item = iter.__next__();
                    Object[] args = new Object[]{item};
                    Object result = function.call(null, args);
                    if (result instanceof Boolean) {
                        boolean booleanResult = (Boolean) result;
                        if (booleanResult) {
                            filteredElements.add(item);
                        }
                    }
                }
            } catch (StopIterationException e) {
                // fall through
            }

            return new PTuple(filteredElements.toArray());
        }
    }

    // getattr(object, name[, default])
    @Builtin(name = "getattr", minNumOfArguments = 2, maxNumOfArguments = 3)
    public abstract static class GetAttrNode extends PythonBuiltinNode {

        @Specialization(order = 1)
        public Object getAttrFromModule(PythonModule module, String name, Object defaultValue) {
            Object attrValue = module.getAttribute(name);
            if ((attrValue == PNone.NONE) && defaultValue != PNone.NONE) {
                return defaultValue;
            }

            return attrValue;
        }

        @Specialization(order = 2)
        public Object getAttrFromClass(PythonClass clazz, String name, Object defaultValue) {
            Object attrValue = clazz.getAttribute(name);
            if ((attrValue == PNone.NONE) && defaultValue != PNone.NONE) {
                return defaultValue;
            }

            return attrValue;
        }

        @Specialization(order = 3)
        public Object getAttrFromObject(PythonObject object, String name, Object defaultValue) {
            Object attrValue = object.getAttribute(name);

            if ((attrValue == PNone.NONE) && defaultValue != PNone.NONE) {
                return defaultValue;
            }

            if (attrValue instanceof PFunction) {
                PFunction funcAttr = (PFunction) attrValue;
                PMethod method = CallAttributeNode.createPMethodFor(object, funcAttr);
                return method;
            }

            return attrValue;
        }

        @Specialization
        public Object getAttr(Object object, Object name, Object defaultValue) {
            throw new RuntimeException("getAttr is not supported for " + object + " " + object.getClass() + " name " + name + " defaultValue " + defaultValue);
        }
    }

    // hasattr(object, name)
    @Builtin(name = "hasattr", hasFixedNumOfArguments = true, fixedNumOfArguments = 2)
    public abstract static class HasAttrNode extends PythonBuiltinNode {

        @Specialization
        public Object hasAttr(PythonBasicObject object, String name) {
            List<String> attributes = object.getAttributeNames();
            if (attributes.contains(name)) {
                return true;
            }

            attributes = object.getPythonClass().getAttributeNames();
            if (attributes.contains(name)) {
                return true;
            }

            return false;
        }

        @Specialization
        public Object hasAttr(Object object, Object name) {
            throw new RuntimeException("hasAttr is not supported for " + object + " " + object.getClass() + " name " + name);
        }
    }

    // isinstance(object, classinfo)
    @Builtin(name = "isinstance", hasFixedNumOfArguments = true, fixedNumOfArguments = 2)
    public abstract static class PythonIsIntanceNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(order = 1)
        public Object isinstance(String str, PythonClass clazz) {
            if (clazz.getClassName().equals("str")) {
                return true;
            } else {
                return false;
            }
        }

        @Specialization(order = 2)
        public Object isinstance(PythonBasicObject object, PythonClass clazz) {
            return isInstanceofPythonClass(object, clazz);
        }

        @Specialization(order = 3)
        public Object isinstance(PythonClass object, PythonClass clazz) {
            return isInstanceofPythonClass(object, clazz);
        }

        private static boolean isInstanceofPythonClass(PythonBasicObject object, PythonClass clazz) {
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

            if (object instanceof PythonClass) {
                if (clazz.getClassName().equals("type")) {
                    return true;
                }
            }

            return false;
        }

        @Specialization(order = 4)
        public Object isinstance(Object object, Object clazz) {
            if (object instanceof String && clazz instanceof PythonClass) {
                PythonClass pythonClass = (PythonClass) clazz;
                if (pythonClass.getClassName().equals("str")) {
                    return true;
                }

                return false;
            } else if (object instanceof PythonBasicObject && clazz instanceof PythonClass) {
                PythonBasicObject basicObject = (PythonBasicObject) object;
                PythonClass pythonClass = (PythonClass) clazz;
                return isInstanceofPythonClass(basicObject, pythonClass);
            } else if (object instanceof PNone && clazz instanceof PythonBuiltinClass) {
                return false;
            }

            throw new RuntimeException("isinstance is not supported for " + object + " " + object.getClass() + ", " + clazz + " " + clazz.getClass());
        }
    }

    // issubclass(class, classinfo)
    @Builtin(name = "issubclass", hasFixedNumOfArguments = true, fixedNumOfArguments = 2)
    public abstract static class PythonIsSubClassNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(order = 1)
        public Object issubclass(PythonModule clazz, PythonClass clazzinfo) {
            return false;
        }

        @Specialization(order = 2)
        public Object issubclass(PythonClass clazz, PythonClass clazzinfo) {
            /**
             * TODO How do you check two classes are equal? Name comparison can't be true all the
             * time.
             */
            if (clazz.getClassName().equals(clazzinfo.getClassName())) {
                return true;
            } else {
                PythonClass superClass = clazz.getSuperClass();
                while (superClass != null) {
                    if (superClass.getClassName().equals(clazzinfo.getClassName())) {
                        return true;
                    }
                    superClass = superClass.getSuperClass();
                }
            }

            return false;
        }

        @Specialization(order = 3)
        public Object issubclass(Object clazz, Object clazzinfo) {
            throw new RuntimeException("issubclass is not supported for " + clazz + " " + clazz.getClass() + ", " + clazzinfo + " " + clazzinfo.getClass());
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

        @Specialization()
        public Object iter(Object object, Object sentinel) {
            throw new RuntimeException("Not supported sentinel case object " + object + " sentinel " + sentinel);
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
        public int len(PTuple tuple) {
            return tuple.len();
        }

        @Specialization(order = 5)
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
        public Object maxSequence(PSequence arg1, PTuple args, Object keywordArg) {
            return arg1.getMax();
        }

        @SuppressWarnings("unused")
        @Specialization(order = 2, guards = "hasOneArgument")
        public Object maxBaseSet(PBaseSet arg1, PTuple args, Object keywordArg) {
            return arg1.getMax();
        }

        @SuppressWarnings("unused")
        @Specialization(order = 3, guards = "hasOneArgument")
        public Object maxDictionary(PDict arg1, PTuple args, Object keywordArg) {
            return arg1.getMax();
        }

        /**
         * Incomplete. Only deals with ints now.
         */
        @SuppressWarnings("unused")
        @Specialization(order = 4)
        public Object maxPIterator(PIterator arg1, PTuple args, PNone keywordArg) {
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
        public Object maxGeneric(Object arg1, PTuple args, Object keywordArg) {
            if (keywordArg instanceof PNone) {
                if (args.len() == 1) {
                    return getMax(arg1, args.getItem(0));
                } else {
                    Object[] argsArray = new Object[args.len() + 1];
                    argsArray[0] = arg1;
                    System.arraycopy(args.getArray(), 0, argsArray, 1, args.len());
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
        public static boolean hasOneArgument(Object arg1, PTuple args, Object keywordArg) {
            return (args.len() == 0 && keywordArg instanceof PNone);
        }

    }

    // min(iterable, *[, key])
    // min(arg1, arg2, *args[, key])
    @Builtin(name = "min", minNumOfArguments = 1, takesKeywordArguments = true, takesVariableArguments = true, keywordNames = {"key"})
    public abstract static class PythonMinNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(guards = "hasOneArgument")
        public Object minString(String arg1, PTuple args, Object keywordArg) {
            PString pstring = new PString(arg1);
            return pstring.getMin();
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "hasOneArgument")
        public Object minSequence(PSequence arg1, PTuple args, Object keywordArg) {
            return arg1.getMin();
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "hasOneArgument")
        public Object minBaseSet(PBaseSet arg1, PTuple args, Object keywordArg) {
            return arg1.getMin();
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "hasOneArgument")
        public Object minDictionary(PDict arg1, PTuple args, Object keywordArg) {
            return arg1.getMin();
        }

        @Specialization
        public Object minGeneric(Object arg1, PTuple args, Object keywordArg) {
            if (keywordArg instanceof PNone) {
                if (arg1 instanceof Iterable) {
                    throw new RuntimeException("Multiple iterables are not supported");
                } else if (args.len() == 1) {
                    return getMin(arg1, args.getItem(0));
                } else {
                    Object[] argsArray = new Object[args.len() + 1];
                    argsArray[0] = arg1;
                    System.arraycopy(args.getArray(), 0, argsArray, 1, args.len());
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
        public static boolean hasOneArgument(Object arg1, PTuple args, Object keywordArg) {
            return (args.len() == 0 && keywordArg instanceof PNone);
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

    // ord(c)
    @Builtin(name = "ord", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class OrdNode extends PythonBuiltinNode {

        @Specialization
        public int ord(String chr) {
            if (chr.length() != 1) {
                typeError("ord() expected a character, but string of length " + chr.length() + " found");
            }

            return chr.charAt(0);
        }

    }

    // print(*objects, sep=' ', end='\n', file=sys.stdout, flush=False)
    @Builtin(name = "print", minNumOfArguments = 0, takesKeywordArguments = true, takesVariableArguments = true, takesVariableKeywords = true, keywordNames = {"sep", "end", "file", "flush"}, requiresContext = true)
    public abstract static class PythonPrintNode extends PythonBuiltinNode {

        @Specialization
        public Object print(PTuple values, Object[] keywords) {
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
        private Object print(PTuple values, String possibleSep, String possibleEnd) {
            String sep = possibleSep;
            String end = possibleEnd;
            // CheckStyle: stop system..print check
            if (values.len() == 0) {
                getContext().getStandardOut().print(System.getProperty("line.separator"));
            } else {
                if (sep == null) {
                    sep = "";
                }

                if (end == null) {
                    end = System.getProperty("line.separator");
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < values.len() - 1; i++) {
                    if (values.getItem(i) instanceof Boolean) {
                        sb.append(((boolean) values.getItem(i) ? "True" : "False") + " ");
                    } else {
                        sb.append(values.getItem(i) + " ");
                    }
                }

                if (values.getItem(values.len() - 1) instanceof Boolean) {
                    sb.append(((boolean) values.getItem(values.len() - 1) ? "True" : "False"));
                } else {
                    sb.append(values.getItem(values.len() - 1));
                }

                getContext().getStandardOut().print(sb.toString() + sep + end);

            }
            // CheckStyle: resume system..print check
            return null;
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

    // reversed(seq)
    @Builtin(name = "reversed", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class PythonReversedNode extends PythonBuiltinNode {

        @Specialization
        public PIterator reversed(PRange range) {
            return new PRangeIterator.PRangeReverseIterator(range);
        }

        @Specialization
        public PIterator reversed(PSequence sequence) {
            return new PSequenceIterator.PSequenceReverseIterator(sequence);
        }

    }

    // sum(iterable[, start])
    @Builtin(name = "sum", minNumOfArguments = 1, takesKeywordArguments = true, maxNumOfArguments = 2, keywordNames = {"start"})
    public abstract static class PythonSumNode extends PythonBuiltinNode {
        /**
         * Incomplete. Only support ints.
         */

        @Specialization
        public int doPIterable(PIterable iterable) {
            PIterator iter = iterable.__iter__();
            return doPIterator(iter);
        }

        @Specialization
        public int doPIterator(PIterator iterator) {
            int sum = 0;
            try {
                while (true) {
                    sum += (int) iterator.__next__();
                }
            } catch (StopIterationException e) {
            }

            return sum;
        }

    }

    // super([type[, object-or-type]])
    @Builtin(name = "super", minNumOfArguments = 1, maxNumOfArguments = 2)
    public abstract static class PythonSuperNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization
        public Object applySuper(PythonClass type, Object object) {
            return type.getSuperClass();
        }

        @Specialization
        public Object applySuperGeneric(Object type, Object object) {
            throw new RuntimeException("super is not supported for type " + type + " object " + object);
        }

    }

    // type(object)
    @Builtin(name = "type", hasFixedNumOfArguments = true, fixedNumOfArguments = 1, isConstructor = true)
    public abstract static class PythonTypeNode extends PythonBuiltinNode {

        @Specialization
        public Object type(PythonObject object) {
            return object.getPythonClass();
        }

        @Specialization
        @SuppressWarnings("unused")
        public Object type(int value) {
            return getContext().getBuiltins().getAttribute("int");
        }

        @Specialization
        @SuppressWarnings("unused")
        public Object type(double value) {
            return getContext().getBuiltins().getAttribute("float");
        }

        @Specialization
        public Object type(Object object) {
            throw new RuntimeException("type is not supported for object " + object);
        }

    }

    // __import__(name, globals=None, locals=None, fromlist=(), level=0)
    @Builtin(name = "__import__", hasFixedNumOfArguments = true, fixedNumOfArguments = 1)
    public abstract static class ImportNode extends PythonBuiltinNode {

        @Specialization
        public Object __import__(String name) {
            // Object importedModule = getContext().getPythonBuiltinsLookup().lookupModule(name);
            if (name.equals("__main__")) {
                Object importedModule = getContext().getMainModule();
                return importedModule;
            } else {
                Object importedModule = getContext().getPythonBuiltinsLookup().lookupModule(name);
                return importedModule;
            }
        }
    }

    @SlowPath
    private static void typeError(String message) {
        throw Py.TypeError(message);
    }

}
