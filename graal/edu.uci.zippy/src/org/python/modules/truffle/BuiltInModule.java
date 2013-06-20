package org.python.modules.truffle;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.python.ast.datatypes.PCallable;
import org.python.ast.datatypes.PComplex;
import org.python.ast.datatypes.PDictionary;
import org.python.ast.datatypes.PFrozenSet;
import org.python.ast.datatypes.PKeyword;
import org.python.ast.datatypes.PList;
import org.python.ast.datatypes.PObject;
import org.python.ast.datatypes.PSequence;
import org.python.ast.datatypes.PSet;
import org.python.ast.datatypes.PString;
import org.python.ast.datatypes.PTuple;
import org.python.modules.truffle.annotations.ModuleConstant;
import org.python.modules.truffle.annotations.ModuleMethod;

public class BuiltInModule extends Module {
    private static final long MASK_NON_SIGN_LONG = 0x7fffffffffffffffl;
    
    public BuiltInModule() {
        try {
            addBuiltInMethods();
        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        addConstants();
    }
    
    public static int getExponent(final double d) {
        // NaN and Infinite will return 1024 anywho so can use raw bits
        return (int) ((Double.doubleToRawLongBits(d) >>> 52) & 0x7ff) - 1023;
    }
    
    public static double abs(double x) {
        return Double.longBitsToDouble(MASK_NON_SIGN_LONG & Double.doubleToRawLongBits(x));
    }
    
    public static double scalb(final double d, final int n) {

        // first simple and fast handling when 2^n can be represented using normal numbers
        if ((n > -1023) && (n < 1024)) {
            return d * Double.longBitsToDouble(((long) (n + 1023)) << 52);
        }

        // handle special cases
        if (Double.isNaN(d) || Double.isInfinite(d) || (d == 0)) {
            return d;
        }
        if (n < -2098) {
            return (d > 0) ? 0.0 : -0.0;
        }
        if (n > 2097) {
            return (d > 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }

        // decompose d
        final long bits = Double.doubleToRawLongBits(d);
        final long sign = bits & 0x8000000000000000L;
        int  exponent   = ((int) (bits >>> 52)) & 0x7ff;
        long mantissa   = bits & 0x000fffffffffffffL;

        // compute scaled exponent
        int scaledExponent = exponent + n;

        if (n < 0) {
            // we are really in the case n <= -1023
            if (scaledExponent > 0) {
                // both the input and the result are normal numbers, we only adjust the exponent
                return Double.longBitsToDouble(sign | (((long) scaledExponent) << 52) | mantissa);
            } else if (scaledExponent > -53) {
                // the input is a normal number and the result is a subnormal number

                // recover the hidden mantissa bit
                mantissa = mantissa | (1L << 52);

                // scales down complete mantissa, hence losing least significant bits
                final long mostSignificantLostBit = mantissa & (1L << (-scaledExponent));
                mantissa = mantissa >>> (1 - scaledExponent);
                if (mostSignificantLostBit != 0) {
                    // we need to add 1 bit to round up the result
                    mantissa++;
                }
                return Double.longBitsToDouble(sign | mantissa);

            } else {
                // no need to compute the mantissa, the number scales down to 0
                return (sign == 0L) ? 0.0 : -0.0;
            }
        } else {
            // we are really in the case n >= 1024
            if (exponent == 0) {

                // the input number is subnormal, normalize it
                while ((mantissa >>> 52) != 1) {
                    mantissa = mantissa << 1;
                    --scaledExponent;
                }
                ++scaledExponent;
                mantissa = mantissa & 0x000fffffffffffffL;

                if (scaledExponent < 2047) {
                    return Double.longBitsToDouble(sign | (((long) scaledExponent) << 52) | mantissa);
                } else {
                    return (sign == 0L) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                }

            } else if (scaledExponent < 2047) {
                return Double.longBitsToDouble(sign | (((long) scaledExponent) << 52) | mantissa);
            } else {
                return (sign == 0L) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            }
        }

    }
    
    public static double sqrt(final double a) {
        return Math.sqrt(a);
    }
    
    //FastMath
    public static double hypot(final double x, final double y) {
        if (Double.isInfinite(x) || Double.isInfinite(y)) {
            return Double.POSITIVE_INFINITY;
        } else if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        } else {

            final int expX = getExponent(x);
            final int expY = getExponent(y);
            if (expX > expY + 27) {
                // y is neglectible with respect to x
                return abs(x);
            } else if (expY > expX + 27) {
                // x is neglectible with respect to y
                return abs(y);
            } else {

                // find an intermediate scale to avoid both overflow and underflow
                final int middleExp = (expX + expY) / 2;

                // scale parameters without losing precision
                final double scaledX = scalb(x, -middleExp);
                final double scaledY = scalb(y, -middleExp);

                // compute scaled hypotenuse
                final double scaledH = sqrt(scaledX * scaledX + scaledY * scaledY);

                // remove scaling
                return scalb(scaledH, middleExp);

            }

        }
    }
    
    @ModuleConstant
    public final static String __name__ = "__main__";
    
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
    public int len(Object args[], Object[] keywords) {
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
    public Object print(Object args[], Object[] keywords) {
        Object values[] = args;

        String end = null;
        String sep = null;

        if (keywords != null) {
            for (int i = 0; i < keywords.length; i++) { // not support file keyword now
                PKeyword kw = (PKeyword) keywords[i];
                if (kw.getName().equals("end"))
                    end = (String) kw.getValue();
                else if (kw.getName().equals("sep"))
                    sep = (String) kw.getValue();
            }
        }

        return print(values, sep, end);
    }

    private Object print(Object values[], String sep, String end) {
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
        return null;
    }
    
    public Object print(Object value) {
        String end = System.getProperty("line.separator");
        System.out.print(value + end);
        return null;
    }
    
    public Object print(Object value1, Object value2) {
        String end = System.getProperty("line.separator");
        System.out.print(value1 + " " + value2 + end);
        return null;
    }

    private List<Character> stringToCharList(String s) {
        ArrayList<Character> list = new ArrayList<Character>();
        
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
                results.add(new PTuple(new Object[] { index, list.getItem(i) }));
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
            Map<Object, Object> newMap = new HashMap<Object, Object>();

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
            if (val < 0)
                return -val;
            else
                return val;
        } else if (arg instanceof BigInteger) {
            BigInteger val = (BigInteger) arg;
            return val.abs();
        } else if (arg instanceof Double) {
            double val = (double) arg;
            if (val < 0)
                return -val;
            else
                return val;
        } else if (arg instanceof PComplex) {
            PComplex val = (PComplex) arg;
            //return Math.hypot(val.getReal(), val.getImag());
            double real = val.getReal();
            double imag = val.getImag();
            return hypot(real, imag);
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

        
    private Object doubleToInt(Double num) {
        if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE)
            return BigInteger.valueOf(num.longValue());
        else
            return num.intValue();
    }

    private Object stringToInt(String num, int base) {
        if ((base >= 2 && base <= 32) || base == 0) {
            BigInteger bi = asciiToBigInteger(num, 10, false);
            if (bi.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) > 0 || bi.compareTo(BigInteger.valueOf((long) Integer.MIN_VALUE)) < 0)
                return bi;
            else
                return bi.intValue();
        } else {
            throw new RuntimeException("base is out of range for int()");
        }
    }

    // The built-in function returns an integer or BigInteger
    @ModuleMethod
    public Object toInt(Object[] args, Object[] keywords) {
        if (args.length == 1) {
            return toInt(args[0]);
        } else if (args.length == 2) {
            return toInt(args[0], args[1]);
        } else {
            throw new RuntimeException("wrong number of arguments for int()");
        }
    }
    
    public Object toInt(Object arg) {
        if (arg instanceof Integer || arg instanceof BigInteger)
            return arg;
        else if (arg instanceof Double) {
            return doubleToInt((Double) arg);
        } else if (arg instanceof String) {
            return stringToInt((String) arg, 10);
        } else {
            throw new RuntimeException("invalid value for int()");
        }
    }
    
    public Object toInt(Object arg1, Object arg2) {
        if (arg1 instanceof String && arg2 instanceof Integer) {
            return stringToInt((String) arg1, (Integer) arg2);
        } else {
            throw new RuntimeException("invalid base or val for int()");
        }
    }

    // Copied directly from Jython
    private BigInteger asciiToBigInteger(String str, int base, boolean isLong) {
        int b = 0;
        int e = str.length();

        while (b < e && Character.isWhitespace(str.charAt(b)))
            b++;

        while (e > b && Character.isWhitespace(str.charAt(e - 1)))
            e--;

        char sign = 0;
        if (b < e) {
            sign = str.charAt(b);
            if (sign == '-' || sign == '+') {
                b++;
                while (b < e && Character.isWhitespace(str.charAt(b)))
                    b++;
            }

            if (base == 16) {
                if (str.charAt(b) == '0') {
                    if (b < e - 1 &&
                            Character.toUpperCase(str.charAt(b + 1)) == 'X') {
                        b += 2;
                    }
                }
            } else if (base == 0) {
                if (str.charAt(b) == '0') {
                    if (b < e - 1 && Character.toUpperCase(str.charAt(b + 1)) == 'X') {
                        base = 16;
                        b += 2;
                    } else if (b < e - 1 && Character.toUpperCase(str.charAt(b + 1)) == 'O') {
                        base = 8;
                        b += 2;
                    } else if (b < e - 1 && Character.toUpperCase(str.charAt(b + 1)) == 'B') {
                        base = 2;
                        b += 2;
                    } else {
                        base = 8;
                    }
                }
            } else if (base == 8) {
                if (b < e - 1 && Character.toUpperCase(str.charAt(b + 1)) == 'O') {
                    b += 2;
                }
            } else if (base == 2) {
                if (b < e - 1 &&
                        Character.toUpperCase(str.charAt(b + 1)) == 'B') {
                    b += 2;
                }
            }
        }

        if (base == 0) {
            base = 10;
        }

        // if the base >= 22, then an 'l' or 'L' is a digit!
        if (isLong && base < 22 && e > b && (str.charAt(e - 1) == 'L' || str.charAt(e - 1) == 'l')) {
            e--;
        }

        String s = str;
        if (b > 0 || e < str.length()) {
            s = str.substring(b, e);
        }

        BigInteger bi;
        if (sign == '-') {
            bi = new BigInteger("-" + s, base);
        } else {
            bi = new BigInteger(s, base);
        }
        return bi;
    }
    
    private PList rangeInt(int start, int stop, int step) {
        ArrayList<Object> list = new ArrayList<Object>();

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

    private PList rangeBigInt(Object start, Object stop, Object step) {
        BigInteger bigStart = getBigInt(start);
        BigInteger bigStop = getBigInt(stop);
        BigInteger bigStep = getBigInt(step);

        ArrayList<Object> list = new ArrayList<Object>();

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

    private BigInteger getBigInt(Object num) {
        if (num instanceof BigInteger)
            return (BigInteger) num;
        else if (num instanceof Integer)
            return BigInteger.valueOf((long) num);
        else
            return null; // this should not happen
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
     * str() currently only handle one argument
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
        
        ArrayList<Object> list = new ArrayList<Object>();
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
        return Character.toString ((char) value);
    }
    
    public String chr(Object arg0, Object arg1) {
        throw new RuntimeException("wrong number of arguments for chr() ");
    }
    
    /**
     *  zip() method, should return a python iterator, but we use list as a temporary solution
     */
    
    @SuppressWarnings("rawtypes")
    @ModuleMethod
    public PList zip(Object[] args, Object[] keywords) {
        int itemsize = args.length;
        
        Iterator[] argList = new Iterator[itemsize];
        
        int index = 0;
        for (int i = 0; i< args.length; i++) {
            argList[index++] = getIterable(args[i]);
        }
        
        ArrayList<PTuple> tuples = new ArrayList<PTuple>();
              
        OutterLoop:
        while (true) {
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

        ArrayList<PTuple> tuples = new ArrayList<PTuple>();
              
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
    private Iterator<Object> getIterable(Object o) {
        if (o instanceof String) {
            return new PString((String) o).iterator();
        } else if (o instanceof Iterable) {
            return ((Iterable<Object>) o).iterator();
        } else {
            throw new RuntimeException("argument is not iterable ");
        }
    }
}
