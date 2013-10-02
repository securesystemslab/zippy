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
package edu.uci.python.runtime.modules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.annotations.*;

public class BuiltInModule extends PModule {

    public BuiltInModule() {
        super("builtin");
        addBuiltInMethods();
        addConstants();
    }

    @ModuleConstant public static final String __name__ = "__main__";

    @ModuleMethod
    public Object min(Object[] args, Object[] keywords) {
        if (args.length == 1) {
            return min(args[0]);
        } else {
            Object[] copy = Arrays.copyOf(args, args.length);
            Arrays.sort(copy);
            return copy[0];
        }
    }

    // Specialized with one argument
    public Object min(Object arg) {
        if (arg instanceof PObject) {
            return ((PObject) arg).getMin();
        } else if (arg instanceof String) {
            char[] copy = ((String) arg).toCharArray();
            Arrays.sort(copy);
            return copy[0];
        } else {
            throw new RuntimeException("Unexpected argument type for min() ");
        }
    }

    public Object min(Object arg0, Object arg1) {
        Object[] copy = {arg0, arg1};
        Arrays.sort(copy);
        return copy[0];
    }

    @ModuleMethod
    public Object max(Object[] args, Object[] keywords) {
        if (args.length == 1) {
            return max(args[0]);
        } else {
            Object[] copy = Arrays.copyOf(args, args.length);
            Arrays.sort(copy);
            return copy[copy.length - 1];
        }
    }

    public Object max(Object arg) {
        if (arg instanceof PObject) {
            return ((PObject) arg).getMax();
        } else if (arg instanceof String) {
            char[] copy = ((String) arg).toCharArray();
            Arrays.sort(copy);
            return copy[copy.length - 1];
        } else {
            throw new RuntimeException("Unexpected argument type for max() ");
        }
    }

    public Object max(Object arg0, Object arg1) {
        Object[] copy = {arg0, arg1};
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }

    @ModuleMethod
    public int len(Object[] args, Object[] keywords) {
        if (args.length == 1) {
            return len(args[0]);
        } else {
            throw new RuntimeException("wrong number of arguments for len() ");
        }
    }

    public int len(Object arg) {
        if (arg instanceof PObject) {
            return ((PObject) arg).len();
        } else if (arg instanceof String) {
            return ((String) arg).length();
        } else {
            throw new RuntimeException("Unexpected argument type for len() ");
        }
    }

    public int len(Object arg1, Object arg2) {
        throw new RuntimeException("wrong number of arguments for len() ");
    }

    @ModuleMethod
    public Object print(Object[] args, Object[] keywords) {
        Object[] values = args;

        String end = null;
        String sep = null;

        if (keywords != null) {
            for (int i = 0; i < keywords.length; i++) { // not support file
                // keyword now
                PKeyword kw = (PKeyword) keywords[i];
                if (kw.getName().equals("end")) {
                    end = (String) kw.getValue();
                } else if (kw.getName().equals("sep")) {
                    sep = (String) kw.getValue();
                }
            }
        }

        return print(values, sep, end);
    }

    private static Object print(Object[] values, String sep, String end) {
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

    public Object print(Object value) {
        String end = System.getProperty("line.separator");
        // CheckStyle: stop system..print check
        System.out.print(value + end);
        // CheckStyle: resume system..print check
        return null;
    }

    public Object print(Object value1, Object value2) {
        String end = System.getProperty("line.separator");
        // CheckStyle: stop system..print check
        System.out.print(value1 + " " + value2 + end);
        // CheckStyle: resume system..print check
        return null;
    }

    private static List<Character> stringToCharList(String s) {
        ArrayList<Character> list = new ArrayList<>();

        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            list.add(charArray[i]);
        }
        return list;
    }

    @ModuleMethod
    public PSet set(Object[] args, Object[] keywords) {
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

    public PSet set(Object arg1, Object arg2) {
        return new PSet();
    }

    @ModuleMethod
    public PFrozenSet frozenset(Object[] args, Object[] keywords) {
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

    public PFrozenSet frozenset(Object arg1, Object arg2) {
        return new PFrozenSet();
    }

    @ModuleMethod
    public PList enumerate(Object[] args, Object[] keywords) {
        if (args.length != 1) {
            throw new RuntimeException("wrong number of arguments for enumerate() ");
        }
        return enumerate(args[0]);
    }

    public PList enumerate(Object arg) {
        if (arg instanceof PList) {
            PList list = (PList) arg;
            List<PTuple> results = new ArrayList<PTuple>();
            int index = 0;

            for (int i = 0; i < list.len(); i++) {
                results.add(new PTuple(new Object[]{index, list.getItem(i)}));
                index++;
            }

            return new PList(results);
        } else {
            throw new RuntimeException("Unsupported argument type for enumerate() ");
        }
    }

    public PList enumerate(Object arg1, Object arg2) {
        throw new RuntimeException("wrong number of arguments for enumerate()");
    }

    @ModuleMethod
    public PDictionary dict(Object[] args, Object[] keywords) {
        // we don't support keywords arguments currently
        if (args.length == 0) {
            return new PDictionary();
        } else if (args.length == 1) {
            return dict(args[0]);
        } else {
            throw new RuntimeException("wrong number of arguments for dict()");
        }
    }

    public PDictionary dict(Object arg) {
        if (arg instanceof PDictionary) { // argument is a mapping
                                          // type
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

    public PDictionary dict(Object arg1, Object arg2) {
        throw new RuntimeException("wrong number of arguments for dict()");
    }

    @ModuleMethod
    public Object abs(Object[] args, Object[] keywords) {
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

    public Object abs(Object arg1, Object arg2) {
        throw new RuntimeException("wrong number of args for abs()");
    }

    @ModuleMethod
    public PList list(Object[] args, Object[] keywords) {
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

    public PList list(Object arg1, Object arg2) {
        throw new RuntimeException("wrong number of arguments for list()");
    }

    private static PList rangeInt(int start, int stop, int step) {
        ArrayList<Object> list = new ArrayList<>();

        if (step > 0) {
            for (int i = start; i < stop; i += step) {
                list.add(i);
            }
            return new PList(list);
        } else if (step < 0) {
            if (start >= stop) {
                for (int i = start; i > stop; i -= step) {
                    list.add(i);
                }
                return new PList(list);
            } else {
                throw new RuntimeException("start should not be less than stop!");
            }
        } else {
            throw new RuntimeException("step can not be zero!");
        }
    }

    private static PList rangeBigInt(Object start, Object stop, Object step) {
        BigInteger bigStart = getBigInt(start);
        BigInteger bigStop = getBigInt(stop);
        BigInteger bigStep = getBigInt(step);

        ArrayList<Object> list = new ArrayList<>();

        if (bigStep.compareTo(BigInteger.ZERO) == 1) {
            for (BigInteger i = bigStart; i.compareTo(bigStop) == -1; i.add(bigStep)) {
                list.add(i);
            }
            return new PList(list);
        } else if (bigStep.compareTo(BigInteger.ZERO) == -1) {
            if (bigStart.compareTo(bigStop) == 1 || bigStart.compareTo(bigStop) == 0) {
                for (BigInteger i = bigStart; i.compareTo(bigStop) == 1; i.subtract(bigStep)) {
                    list.add(i);
                }
                return new PList(list);
            } else {
                throw new RuntimeException("start should not be less than stop!");
            }
        } else {
            throw new RuntimeException("step can not be zero!");
        }
    }

    private static BigInteger getBigInt(Object num) {
        if (num instanceof BigInteger) {
            return (BigInteger) num;
        } else if (num instanceof Integer) {
            return BigInteger.valueOf((long) num);
        } else {
            return null; // this should not happen
        }
    }

    @ModuleMethod
    public PList range(Object[] args, Object[] keywords) {
        if (args.length == 1) {
            return range(args[0]);
        } else if (args.length == 2) {
            return range(args[0], args[1]);
        } else if (args.length == 3) {
            if (args[0] instanceof Integer && args[1] instanceof Integer && args[2] instanceof Integer) {
                return rangeInt((Integer) args[0], (Integer) args[1], (Integer) args[2]);
            } else {
                return rangeBigInt(args[0], args[1], args[2]);
            }
        } else {
            throw new RuntimeException("wrong number of arguments for range() ");
        }
    }

    public PList range(Object arg) {
        if (arg instanceof Integer) {
            return rangeInt(0, (Integer) arg, 1);
        } else if (arg instanceof Double) {
            int intArg = ((Double) arg).intValue();
            return rangeInt(0, intArg, 1);
        } else {
            return rangeBigInt(BigInteger.ZERO, arg, BigInteger.ONE);
        }
    }

    public PList range(Object arg1, Object arg2) {
        if (arg1 instanceof Integer && arg2 instanceof Integer) {
            return rangeInt((Integer) arg1, (Integer) arg2, 1);
        } else {
            return rangeBigInt(arg1, arg2, BigInteger.ONE);
        }
    }

    /**
     * str() currently only handle one argument.
     */
    @ModuleMethod
    public String str(Object[] args, Object[] keywords) {
        if (args.length == 1) {
            return args[0].toString();
        } else {
            throw new RuntimeException("wrong number of arguments for str() ");
        }
    }

    public String str(Object arg) {
        return arg.toString();
    }

    public String str(Object arg0, Object arg1) {
        throw new RuntimeException("wrong number of arguments for str() ");
    }

    @ModuleMethod
    public PList map(Object[] args, Object[] keywords) {
        if (args.length == 2) {
            return map(args[0], args[1]);
        } else {
            throw new RuntimeException("wrong number of arguments for str() ");
        }
    }

    public PList map(Object arg) {
        throw new RuntimeException("wrong number of arguments for map() ");
    }

    @SuppressWarnings("rawtypes")
    public PList map(Object arg0, Object arg1) {
        PCallable callee = (PCallable) arg0;
        Iterator iter = getIterable(arg1);

        ArrayList<Object> list = new ArrayList<>();
        while (iter.hasNext()) {
            list.add(callee.call(null, iter.next()));
        }

        return new PList(list);
    }

    @ModuleMethod
    public String chr(Object[] args, Object[] keywords) {
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

    public String chr(Object arg0, Object arg1) {
        throw new RuntimeException("wrong number of arguments for chr() ");
    }

    /**
     * int().
     */
    // Checkstyle: stop method name check
    @ModuleMethod("int")
    public int Int(Object[] args, Object[] keywords) {
        return Int(args[0]);
    }

    public int Int(Object arg) {
        return (int) JavaTypeConversions.toInt(arg);
    }

    public int Int(Object arg0, Object arg1) {
        return (int) JavaTypeConversions.toInt(arg0, arg1);
    }

    // Checkstyle: resume method name check

    /**
     * zip() method, should return a python iterator, but we use list as a temporary solution.
     */
    @SuppressWarnings("rawtypes")
    @ModuleMethod
    public PList zip(Object[] args, Object[] keywords) {
        int itemsize = args.length;

        Iterator[] argList = new Iterator[itemsize];

        int index = 0;
        for (int i = 0; i < args.length; i++) {
            argList[index++] = getIterable(args[i]);
        }

        ArrayList<PTuple> tuples = new ArrayList<PTuple>();

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

    @SuppressWarnings("unchecked")
    public PList zip(Object arg) {
        if (arg instanceof String) {
            return new PList(new PString((String) arg).getList());
        } else if (arg instanceof Iterable) {
            return new PList(((Iterable<Object>) arg));
        } else {
            throw new RuntimeException("argument is not iterable ");
        }
    }

    @SuppressWarnings("rawtypes")
    public PList zip(Object arg0, Object arg1) {
        Iterator iter0 = getIterable(arg0);
        Iterator iter1 = getIterable(arg1);

        ArrayList<PTuple> tuples = new ArrayList<>();

        while (true) {
            Object[] temp = new Object[2];

            if (iter0.hasNext()) {
                temp[0] = iter0.next();
            } else {
                break;
            }

            if (iter1.hasNext()) {
                temp[1] = iter1.next();
            } else {
                break;
            }

            tuples.add(new PTuple(temp));
        }

        return new PList(tuples);
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
