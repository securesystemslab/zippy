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
package edu.uci.python.runtime.standardtypes;

import java.math.*;
import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;
import edu.uci.python.runtime.modules.annotations.*;

/**
 * The Python standard built-ins module.
 * 
 */
public class BuiltinsModule extends PythonModule {

    private PythonBuiltins builtins;

    public BuiltinsModule(PythonClass pythonClass, String name, PythonBuiltins builtins) {
        super(pythonClass);
        this.addBuiltinMethodsAndConstants(PythonModule.class);
        this.setAttribute(__NAME__, name);

        if (!PythonOptions.UseSpecializedBuiltins) {
            this.addBuiltinMethodsAndConstants(BuiltinsModule.class);
        } else {
            this.builtins = builtins;
            addBuiltins();
        }
    }

    private void addBuiltins() {
        Map<String, PBuiltinFunction> builtinList = this.builtins.getBuiltins();
        for (Map.Entry<String, PBuiltinFunction> entry : builtinList.entrySet()) {
            String methodName = entry.getKey();
            PBuiltinFunction function = entry.getValue();
            setAttribute(methodName, function);
        }
    }

    @BuiltinMethod public static final PythonCallTarget min = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return min(args[0]);
            } else {
                Object[] copy = Arrays.copyOf(args, args.length);
                Arrays.sort(copy);
                return copy[0];
            }
        }

        public Object min(Object arg) {
            if (arg instanceof PythonBuiltinObject) {
                return ((PythonBuiltinObject) arg).getMin();
            } else if (arg instanceof String) {
                return findMin((String) arg);
            } else {
                throw new RuntimeException("Unexpected argument type for min() ");
            }
        }

        @SlowPath
        private Object findMin(String string) {
            char[] copy = string.toCharArray();
            Arrays.sort(copy);
            return copy[0];
        }
    };

    @BuiltinMethod public static final PythonCallTarget max = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return max(args[0]);
            } else {
                Object[] copy = Arrays.copyOf(args, args.length);
                return sortObjectArray(copy);
            }
        }

        public Object max(Object arg) {
            if (arg instanceof PythonBuiltinObject) {
                return ((PythonBuiltinObject) arg).getMax();
            } else if (arg instanceof String) {
                char[] copy = ((String) arg).toCharArray();
                return sortCharArray(copy);
            } else {
                throw new RuntimeException("Unexpected argument type for max() ");
            }
        }

        @SlowPath
        public Object sortCharArray(char[] array) {
            Arrays.sort(array);
            return array[array.length - 1];
        }

        @SlowPath
        public Object sortObjectArray(Object[] array) {
            Arrays.sort(array);
            return array[array.length - 1];
        }
    };

    @BuiltinMethod public static final PythonCallTarget len = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return len(args[0]);
            } else {
                throw new RuntimeException("wrong number of arguments for len() ");
            }
        }

        public int len(Object arg) {
            if (arg instanceof PythonBuiltinObject) {
                return ((PythonBuiltinObject) arg).len();
            } else if (arg instanceof String) {
                return ((String) arg).length();
            } else {
                throw new RuntimeException("Unexpected argument type for len() ");
            }
        }
    };

    @BuiltinMethod public static final PythonCallTarget print = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();
            PKeyword[] keywords = arguments.getKeywords();

            Object[] values = args;

            String end = null;
            String sep = null;

            if (keywords != null) {
                for (int i = 0; i < keywords.length; i++) { // not support file
                    // keyword now
                    PKeyword kw = keywords[i];
                    if (kw.getName().equals("end")) {
                        end = (String) kw.getValue();
                    } else if (kw.getName().equals("sep")) {
                        sep = (String) kw.getValue();
                    }
                }
            }

            return print(values, sep, end);
        }

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
                System.out.print(sb.toString() + sep + end);
            }
            // CheckStyle: resume system..print check
            return null;
        }
    };

    @BuiltinMethod public static final PythonCallTarget set = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return set(args[0]);
            } else {
                return new PSet();
            }
        }

        @SuppressWarnings("unchecked")
        public PSet set(Object arg) {
            if (arg instanceof String) {
                return new PSet(stringToCharList((String) arg));
            } else if (arg instanceof Iterable) {
                return new PSet((Iterable<Object>) arg);
            } else {
                throw new RuntimeException("Unexpected argument type for set() ");
            }
        }
    };

    private static List<Character> stringToCharList(String s) {
        ArrayList<Character> sequence = new ArrayList<>();

        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            sequence.add(charArray[i]);
        }
        return sequence;
    }

    @BuiltinMethod public static final PythonCallTarget frozenset = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return frozenset(args[0]);
            } else {
                return new PFrozenSet();
            }
        }

        @SuppressWarnings("unchecked")
        public PFrozenSet frozenset(Object arg) {
            if (arg instanceof String) {
                return new PFrozenSet(stringToCharList((String) arg));
            } else if (arg instanceof Iterable) {
                return new PFrozenSet((Iterable<Object>) arg);
            } else {
                throw new RuntimeException("Unexpected argument type for frozenset() ");
            }
        }
    };

    @BuiltinMethod public static final PythonCallTarget enumerate = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();
            if (args.length != 1) {
                throw new RuntimeException("wrong number of arguments for enumerate() ");
            }
            return enumerate(args[0]);
        }

        public PList enumerate(Object arg) {
            if (arg instanceof PList) {
                PList sequence = (PList) arg;
                List<PTuple> results = new ArrayList<>();
                int index = 0;

                for (int i = 0; i < sequence.len(); i++) {
                    results.add(new PTuple(new Object[]{index, sequence.getItem(i)}));
                    index++;
                }

                return new PList(results);
            } else {
                throw new RuntimeException("Unsupported argument type for enumerate() ");
            }
        }
    };

    @BuiltinMethod public static final PythonCallTarget dict = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 0) {
                return new PDictionary();
            } else if (args.length == 1) {
                return dict(args[0]);
            } else {
                throw new RuntimeException("wrong number of arguments for dict()");
            }
        }

        public PDictionary dict(Object arg) {
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
    };

    @BuiltinMethod public static final PythonCallTarget abs = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return abs(args[0]);
            } else {
                throw new RuntimeException("wrong number of arguments for abs()");
            }
        }

        public Object abs(Object arg) {
            if (arg instanceof Integer) {
                int val = (int) arg;
                if (val < 0) {
                    return -val;
                } else {
                    return val;
                }
            } else if (arg instanceof BigInteger) {
                BigInteger val = (BigInteger) arg;
                return val.abs();
            } else if (arg instanceof Double) {
                double val = (double) arg;
                if (val < 0) {
                    return -val;
                } else {
                    return val;
                }
            } else if (arg instanceof PComplex) {
                PComplex val = (PComplex) arg;
                // return Math.hypot(val.getReal(), val.getImag());
                double real = val.getReal();
                double imag = val.getImag();
                return FastMathUtil.hypot(real, imag);
            } else {
                throw new RuntimeException("invalid data type for abs()");
            }
        }
    };

    @BuiltinMethod public static final PythonCallTarget list = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return list(args[0]);
            } else {
                throw new RuntimeException("wrong number of arguments for list()");
            }
        }

        @SuppressWarnings("unchecked")
        public PList list(Object arg) {
            if (arg instanceof String) {
                return new PList(stringToCharList((String) arg));
            } else if (arg instanceof Iterable) {
                return new PList((Iterable<Object>) arg);
            } else {
                throw new RuntimeException("Unexpected argument type");
            }
        }
    };

    @BuiltinMethod public static final PythonCallTarget range = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return new PRange((int) args[0]);
            } else if (args.length == 2) {
                return new PRange((int) args[0], (int) args[1]);
            } else if (args.length == 3) {
                if (args[0] instanceof Integer && args[1] instanceof Integer && args[2] instanceof Integer) {
                    return new PRange((int) args[0], (int) args[1], (int) args[2]);
                } else {
                    throw new RuntimeException("wrong arguments for range() ");
                }
            } else {
                throw new RuntimeException("wrong number of arguments for range() ");
            }
        }
    };

    @BuiltinMethod public static final PythonCallTarget str = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return args[0].toString();
            } else {
                throw new RuntimeException("wrong number of arguments for str() ");
            }
        }
    };

    @BuiltinMethod public static final PythonCallTarget map = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 2) {
                return map(args[0], args[1]);
            } else {
                throw new RuntimeException("wrong number of arguments for str() ");
            }
        }

        public PList map(Object arg0, Object arg1) {
            PCallable callee = (PCallable) arg0;
            Iterator iter = getIterable(arg1);

            ArrayList<Object> sequence = new ArrayList<>();
            while (iter.hasNext()) {
                sequence.add(callee.call(null, iter.next()));
            }

            return new PList(sequence);
        }
    };

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

    @BuiltinMethod public static final PythonCallTarget chr = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            if (args.length == 1) {
                return chr(args[0]);
            } else {
                throw new RuntimeException("wrong number of arguments for chr() ");
            }
        }

        public String chr(Object arg) {
            int value = (int) arg;
            return Character.toString((char) value);
        }
    };

    @BuiltinMethod(unmangledName = "int") public static final PythonCallTarget Int = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();
            return (int) JavaTypeConversions.toInt(args[0]);
        }

    };

    @BuiltinMethod(unmangledName = "float") public static final PythonCallTarget Float = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();
            return JavaTypeConversions.toDouble(args[0]);
        }

    };

    @BuiltinMethod public static final PythonCallTarget complex = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();

            double real = JavaTypeConversions.toDouble(args[0]);
            double image = JavaTypeConversions.toDouble(args[1]);
            return new PComplex(real, image);
        }

    };

    @BuiltinMethod public static final PythonCallTarget zip = new PythonCallTarget() {

        /**
         * zip() method, should return a python iterator, but we use list as a temporary solution.
         */
        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();
            int itemsize = args.length;

            Iterator[] argList = new Iterator[itemsize];

            int index = 0;
            for (int i = 0; i < args.length; i++) {
                argList[index++] = getIterable(args[i]);
            }

            ArrayList<PTuple> tuples = new ArrayList<>();

            OutterLoop: while (true) {
                Object[] temp = new Object[itemsize];

                for (int i = 0; i < itemsize; i++) {
                    if (argList[i].hasNext()) {
                        temp[i] = argList[i].next();
                    } else {
                        break OutterLoop;
                    }
                }

                tuples.add(new PTuple(temp));
            }

            return new PList(tuples);
        }

    };

    @BuiltinMethod public static final PythonCallTarget isinstance = new PythonCallTarget() {

        @Override
        public Object call(PackedFrame frame, PArguments arguments) {
            Object[] args = arguments.getArgumentsArray();
            if (args[1] instanceof PythonClass) {
                PythonClass clazz = (PythonClass) args[1];
                PythonObject obj = (PythonObject) args[0];

                if (obj.getPythonClass().equals(clazz)) {
                    return true;
                }

                PythonClass superClass = obj.getPythonClass().getSuperClass();

                while (superClass != null) {
                    if (superClass.equals(clazz)) {
                        return true;
                    }

                    superClass = superClass.getSuperClass();
                }

                return false;
            }

            // TODO: tuple case is not supported yet.
            throw Py.TypeError("isinstance() arg 2 must be a type or tuple of types");
        }
    };
}
