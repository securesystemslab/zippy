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

import java.math.*;

public class JavaTypeConversions {

    public static Object stringToInt(String num, int base) {
        if ((base >= 2 && base <= 32) || base == 0) {
            BigInteger bi = asciiToBigInteger(num, 10, false);
            if (bi.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 || bi.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
                return bi;
            } else {
                return bi.intValue();
            }
        } else {
            throw new RuntimeException("base is out of range for int()");
        }
    }

    public static Object toInt(Object arg) {
        if (arg instanceof Integer || arg instanceof BigInteger) {
            return arg;
        } else if (arg instanceof Double) {
            return doubleToInt((Double) arg);
        } else if (arg instanceof String) {
            return stringToInt((String) arg, 10);
        } else {
            throw new RuntimeException("invalid value for int()");
        }
    }

    public static Object toInt(Object arg1, Object arg2) {
        if (arg1 instanceof String && arg2 instanceof Integer) {
            return stringToInt((String) arg1, (Integer) arg2);
        } else {
            throw new RuntimeException("invalid base or val for int()");
        }
    }

    public static Object doubleToInt(Double num) {
        if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
            return BigInteger.valueOf(num.longValue());
        } else {
            return num.intValue();
        }
    }

    public static double toDouble(Object value) {
        try {
            return (double) value;
        } catch (ClassCastException e) {
            // fall through
        }

        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof BigInteger) {
            return ((BigInteger) value).doubleValue();
        } else {
            throw new RuntimeException("unexpected value type " + value.getClass());
        }
    }

    // Copied directly from Jython
    private static BigInteger asciiToBigInteger(String str, int possibleBase, boolean isLong) {
        int base = possibleBase;
        int b = 0;
        int e = str.length();

        while (b < e && Character.isWhitespace(str.charAt(b))) {
            b++;
        }

        while (e > b && Character.isWhitespace(str.charAt(e - 1))) {
            e--;
        }

        char sign = 0;
        if (b < e) {
            sign = str.charAt(b);
            if (sign == '-' || sign == '+') {
                b++;
                while (b < e && Character.isWhitespace(str.charAt(b))) {
                    b++;
                }
            }

            if (base == 16) {
                if (str.charAt(b) == '0') {
                    if (b < e - 1 && Character.toUpperCase(str.charAt(b + 1)) == 'X') {
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
                if (b < e - 1 && Character.toUpperCase(str.charAt(b + 1)) == 'B') {
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

    // Taken from Jython PyString's atof method
    public static double convertStringToDouble(String str) {
        StringBuilder s = null;
        int n = str.length();

        for (int i = 0; i < n; i++) {
            char ch = str.charAt(i);
            if (ch == '\u0000') {
                throw new RuntimeException("null byte in argument for float()");
            }
            if (Character.isDigit(ch)) {
                if (s == null)
                    s = new StringBuilder(str);
                int val = Character.digit(ch, 10);
                s.setCharAt(i, Character.forDigit(val, 10));
            }
        }
        String sval = str;
        if (s != null) {
            sval = s.toString();
        }
        try {
            // Double.valueOf allows format specifier ("d" or "f") at the end
            String lowSval = sval.toLowerCase();
            if (lowSval.equals("nan"))
                return Double.NaN;
            else if (lowSval.equals("inf"))
                return Double.POSITIVE_INFINITY;
            else if (lowSval.equals("-inf"))
                return Double.NEGATIVE_INFINITY;
            return Double.valueOf(sval).doubleValue();
        } catch (NumberFormatException exc) {
            throw new RuntimeException("could not convert string to float: " + str);
        }
    }
}
