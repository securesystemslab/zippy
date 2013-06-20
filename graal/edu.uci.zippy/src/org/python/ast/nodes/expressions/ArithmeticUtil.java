package org.python.ast.nodes.expressions;

import java.math.BigInteger;

import org.python.core.Py;

public class ArithmeticUtil {

    public static int leftShiftExact(int left, int right) {
        if (right >= Integer.SIZE) {
            throw new ArithmeticException("integer overflow");
        } else if (right < 0) {
            throw Py.ValueError("negative shift count");
        }

        int result = left << right;
        if (left != result >> right) {
            throw new ArithmeticException("integer overflow");
        }

        return result;
    }

    public static int rightShiftExact(int left, int right) {
        if (right >= Integer.SIZE) {
            throw new ArithmeticException("integer overflow");
        } else if (right < 0) {
            throw Py.ValueError("negative shift count");
        }

        int result = left >> right;
        if (left != result << right) {
            throw new ArithmeticException("integer overflow");
        }

        return result;
    }

    public static boolean isZero(int value) {
        return value == 0;
    }

    public static boolean isZero(BigInteger value) {
        return value.compareTo(BigInteger.ZERO) == 0;
    }

    public static boolean isZero(double value) {
        return value == 0;
    }

    public static boolean isNotZero(int value) {
        return value != 0;
    }

    public static boolean isNotZero(BigInteger value) {
        return value.compareTo(BigInteger.ZERO) != 0;
    }

    public static boolean isNotZero(double value) {
        return value != 0;
    }

    public static boolean is(Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) {
            int l = (int) left;
            int r = (int) right;
            return l == r;
        } else if (left instanceof BigInteger && right instanceof BigInteger) {
            BigInteger l = (BigInteger) left;
            BigInteger r = (BigInteger) right;
            return l.compareTo(r) == 0;
        } else if (left instanceof Double && right instanceof Double) {
            double l = (double) left;
            double r = (double) right;
            return l == r;
        } else {
            return left.equals(right);
        }
    }
}
