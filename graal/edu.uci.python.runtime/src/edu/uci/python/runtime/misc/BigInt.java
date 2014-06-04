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
package edu.uci.python.runtime.misc;

/*

 Copyright (c) 1996 Massachusetts Institute of Technology

 This material was developed by the Scheme project at the Massachusetts
 Institute of Technology, Department of Electrical Engineering and
 Computer Science.  Permission to copy this software, to redistribute
 it, and to use it for any purpose is granted, subject to the following
 restrictions and understandings.

 1. Any copy made of this software must include this copyright notice
 in full.

 2. Users of this software agree to make their best efforts (a) to
 return to the MIT Scheme project any improvements or extensions that
 they make, so that these may be included in future releases; and (b)
 to inform MIT of noteworthy uses of this software.

 3. All materials developed as a consequence of the use of this
 software shall duly acknowledge such use, in accordance with the usual
 standards of acknowledging credit in academic research.

 4. MIT has made no warrantee or representation that the operation of
 this software will be error-free, and MIT is under no obligation to
 provide any services, by way of maintenance, update, or otherwise.

 5. In conjunction with products arising from the use of this material,
 there shall be no use of the name of the Massachusetts Institute of
 Technology nor of any adaptation thereof in any advertising,
 promotional, or sales literature without prior written consent from
 MIT in each case.

 */

/**
 * Multiple precision integer arithmetic.
 * <p>
 * The BigInt class implements integers with unbounded precision. BigInts support the Classical
 * Algorithms of addition, subtraction, multiplication and division giving a quotient and remainder.
 *
 * <p>
 * BigInts are a subclass of <tt>java.lang.Number</tt> so they support the conversion operations
 * <tt>intValue()</tt>, <tt>doubleValue</tt> etc., just like the other forms of `boxed' number
 * (classes Integer, Float, Long and Double). It is a pity that `Integer' was used as the name for a
 * boxed <tt>int</tt>. `Int' would have been a more regular name, and it would have left the name
 * `Integer' open for multiple precision integers which better model the mathematical notion of
 * integer.
 *
 * <p>
 * The algorithms are taken from Knuth, Donald E., "The Art of Computer Programming", volume 2,
 * "Seminumerical Algorithms" section 4.3.1, "Multiple-Precision Arithmetic".
 *
 * The addition, subtraction and multiplication algorithms are relatively simple, but you will
 * probably want to read this text before trying to understand the division algorithm.
 *
 * <p>
 * BigInts behave like numbers in that they are never modified after creation. For example
 *
 * <pre>
 * BigInt x = BigInt.valueOf(&quot;1000000000000000&quot;);
 * BigInt y = x.add(BigInt.ONE);
 * // CheckStyle: stop system..print check
 * System.out.println(x);
 * // CheckStyle: resume system..print check
 * </pre>
 *
 * prints <tt>1000000000000000</tt>.
 * <p>
 * Operations do not always return new BigInt objects. For example, when adding zero to a thousand
 * digit number, it is more efficient to return the thousand digit number than to make a copy. You
 * should never rely on Java's pointer equality operator (<tt>==</tt>) to compare BigInts in any
 * way.
 *
 * Implementing these algorithms in Java is quite slow. Ideally, the core methods should be native
 * methods. A Just-In-Time compiler would help, but unless it could lift array bounds checking out
 * of loops, native methods would win every time.
 *
 * @version 1.0
 * @see java.lang.Number
 * @author Stephen Adams
 */

// CheckStyle: stop inner assignment check
// CheckStyle: stop parameter assignment check
// Checkstyle: stop
public final class BigInt extends Number {

    /**
     *
     */
    private static final long serialVersionUID = 1661202959937455836L;

    static final int BITS = 30;   // assumed to be at least 22 in constructor.
    // static final int BITS = 4; // useful for testing
    static final int RADIX = (1 << BITS);
    static final int MASK = (1 << BITS) - 1;

    // Representation: sign and magnitude
    // The magnitude is an array of `fat' digits in radix RADIX.
    // The magnitude = digits[0] + RADIX*digits[1] + (RADIX^2)*digits[2] + ...
    // + (RADIX^last)*digits[last].
    // We need to keep `last' because we can't always tell how many digits
    // we will generate.
    boolean negative;  // the sign bit
    int[] digits;       // fat digits
    int last;           // -1 if no digits (i.e. for zero)

    // comparison results
    static final int LESS = -1;
    static final int EQUAL = 0;
    static final int GREATER = 1;

    // useful constants
    public static final BigInt ZERO = new BigInt(0);
    public static final BigInt ONE = new BigInt(1);
    public static final BigInt NEGATIVE_ONE = new BigInt(-1);

    private BigInt(boolean negative, int[] digits, int last) {
        this.negative = negative;
        this.digits = digits;
        this.last = last;
    }

    static BigInt allocate(int ndigits, boolean negative) {
        return new BigInt(negative, new int[ndigits], ndigits - 1);
    }

    /**
     * Construct a BigInt with a given long value.
     *
     * <pre>
     * BigInt fifty = new BigInt(50);
     * </pre>
     *
     * @see #valueOf
     */
    public BigInt(long n) {
        if (n < 0) {
            negative = true;
            n = -n;
            // Warning: at this point N may still be negative if it is the
            // largest negative long. We would like to think of N as
            // a 64 bit unsigned number.
        }
        // This sequence works only for BITS>=22 because we are generating at
        // most 3 fat digits and so require 3*BITS >= 64.
        int d0 = (int) (n & MASK);
        long r1 = n >> BITS;
        int d1 = (int) (r1 & MASK);
        // MASK required because N might still be negative:
        int d2 = (int) ((r1 >> BITS) & MASK);
        if (d2 != 0) {
            int[] d = {d0, d1, d2};
            digits = d;
            last = 2;
        } else if (d1 != 0) {
            int[] d = {d0, d1};
            digits = d;
            last = 1;
        } else {
            int[] d = {d0};
            digits = d;
            last = n == 0 ? -1 : 0;
        }
    }

    static BigInt digit_to_BigInt(int d, boolean negative) {
        if (d == 0) {
            return ZERO;
        }
        int[] digits = {d};
        return new BigInt(negative, digits, 0);
    }

    @Override
    public int hashCode() {
        return last == -1 ? 0 : digits[0] + digits[last];
    }

    /**
     * Test for zero. <code>num.isZero()</code> is equivalent to
     * <code>num.equals(BigInt.ZERO)</code> but much faster.
     */
    boolean isZero() {
        return last == -1;
    }

    /**
     * Compare two BigInts for numerical equality.
     * <p>
     * Note: <tt>equals</tt> will return false if obj is not a BigNum, even if obj makes sense as an
     * integer:
     *
     * <pre>
     *   BigInt.valueOf("100").equals(BigInt.valueOf("100"))    <em>true</em>
     *   BigInt.valueOf("100").equals(Integer.valueOf("100"))   <em>false</em>
     * </pre>
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BigInt) && compare(this, (BigInt) obj) == EQUAL;
    }

    /**
     * Ordering comparison.
     */
    public boolean lessThan(BigInt comparand) {
        return compare(this, comparand) == LESS;
    }

    /**
     * Test the sign of BigInt.
     *
     * @return -1 if it is negative, 0 if it is zero, and 1 if it is positive.
     */
    public int test() {
        return negative ? LESS : (isZero() ? EQUAL : GREATER);
    }

    /**
     * Compare to another BigInt.
     *
     * @param comparand a BigInt to compare against.
     * @return -1 if less than comparand, 0 if equal to comparand, and 1 if greater than comparand.
     */
    public int compare(BigInt comparand) {
        return compare(this, comparand);
    }

    /**
     * Compare two BigInt.
     *
     * @param x first BigInt
     * @param y second BigInt
     * @return -1 if x&lt;y, 0 if x=y, and 1 if x&gt;y
     */
    public static int compare(BigInt x, BigInt y) {
        if (x.negative) {
            if (y.negative) {
                return compare_unsigned(y, x);
            } else {
                return LESS;
            }
        } else if (x.isZero()) {
            return y.negative ? GREATER : (y.isZero() ? EQUAL : LESS);
        } else { // x>0
            if (y.negative) {
                return GREATER;
            } else if (y.isZero()) {
                return GREATER;
            } else {
                return compare_unsigned(x, y);
            }
        }
    }

    /** Return the sum of two BigInts. */
    public BigInt add(BigInt addend) {
        return add(this, addend);
    }

    /** Return the sum of two BigInts. */
    public static BigInt add(BigInt x, BigInt y) {
        if (x.negative) {
            if (y.negative) {
                return add_unsigned(x, y, true);
            } else if (y.isZero()) {
                return x;
            } else {
                return subtract_unsigned(y, x);
            }
        } else if (x.isZero()) {
            return y;
        } else {
            if (y.negative) {
                return subtract_unsigned(x, y);
            } else if (y.isZero()) {
                return x;
            } else {
                return add_unsigned(x, y, false);
            }
        }
    }

    /**
     * Return the integer with the same magnitude and opposite sign.
     */
    public BigInt negate() {
        if (isZero())
            return this;
        return new BigInt(!negative, digits, last);
    }

    /**
     * Return the integer with the same magnitude and positive sign.
     */
    public BigInt abs() {
        return negative ? new BigInt(false, digits, last) : this;
    }

    BigInt withSign(boolean neg) {
        if (negative == neg)
            return this;
        else
            return new BigInt(neg, digits, last);
    }

    /** Return the difference of two BigInts. */
    public BigInt subtract(BigInt y) {
        return subtract(this, y);
    }

    /** Return the difference of two BigInts. */
    public static BigInt subtract(BigInt x, BigInt y) {
        if (x.negative) {
            if (y.negative)
                return subtract_unsigned(y, x);
            else if (y.isZero())
                return x;
            else
                return add_unsigned(x, y, true);
        } else if (x.isZero()) {
            return y.negate();
        } else { // x>0
            if (y.negative)
                return add_unsigned(x, y, false);
            else if (y.isZero())
                return x;
            else
                return subtract_unsigned(x, y);
        }
    }

    /** Return this scaled by multiplicand. */
    public BigInt multiply(BigInt multiplicand) {
        return multiply(this, multiplicand);
    }

    /** Return this scaled by multiplicand. */
    public BigInt multiply(long multiplicand) {
        long scale = (multiplicand < 0) ? -multiplicand : multiplicand;
        if (scale < RADIX) {
            boolean sign = (multiplicand < 0) != negative;
            return multiply_unsigned_small_factor(this, (int) scale, sign);
        } else
            return multiply(this, new BigInt(multiplicand));
    }

    /** Return product of two BigInts. */
    public static BigInt multiply(BigInt x, BigInt y) {
        if (x.isZero() || y.isZero())
            return ZERO;
        boolean negative = (x.negative != y.negative);
        if (x.last == 0) {
            if (x.digits[0] == 1)
                return y.withSign(negative);
            return multiply_unsigned_small_factor(y, x.digits[0], negative);
        }
        if (y.last == 0) {
            if (y.digits[0] == 1)
                return x.withSign(negative);
            return multiply_unsigned_small_factor(x, y.digits[0], negative);
        }
        return multiply_unsigned(x, y, negative);
    }

    /**
     * Divide two BigInts, producing the quotient and remainder. This is more efficient than
     * computing the values separately.
     *
     * @return an array of two BigInts, with the quotient at index 0, and the remainder at index 1.
     */

    public static BigInt[] divide(BigInt numerator, BigInt denominator) {
        if (denominator.isZero()) {
            throw new ArithmeticException("BigInt divide by zero in divide");
        }
        BigInt results[] = new BigInt[2];
        if (numerator.isZero()) {
            results[0] = results[1] = ZERO;
        } else {
            boolean r_negative_p = numerator.negative;
            boolean q_negative_p = denominator.negative ? (!r_negative_p) : r_negative_p;
            switch (compare_unsigned(numerator, denominator)) {
                case EQUAL:
                    results[0] = q_negative_p ? NEGATIVE_ONE : ONE;
                    results[1] = ZERO;
                    break;
                case LESS:
                    results[0] = ZERO;
                    results[1] = numerator;
                    break;
                case GREATER:
                    if (denominator.last == 0) {
                        int digit = denominator.digits[0];
                        if (digit == 1) {
                            results[0] = numerator.withSign(q_negative_p);
                            results[1] = ZERO;
                        } else {
                            results[0] = divide_unsigned_small_denominator(numerator, digit, results, q_negative_p, r_negative_p);
                        }
                    } else {
                        divide_unsigned_large_denominator(numerator, denominator, results, results, q_negative_p, r_negative_p);
                    }
            }
        }
        return results;
    }

    /**
     * Divide two BigInts, producing the quotient.
     *
     * @see #divide
     */
    public static BigInt quotient(BigInt numerator, BigInt denominator) {
        if (denominator.isZero())
            throw new ArithmeticException("BigInt divide by zero in quotient");
        if (numerator.isZero())
            return ZERO;
        boolean q_negative_p = denominator.negative ? (!numerator.negative) : numerator.negative;
        switch (compare_unsigned(numerator, denominator)) {
            case EQUAL:
                return ONE;
            case LESS:
                return ZERO;
            case GREATER:
                if (denominator.last == 0) {
                    int digit = denominator.digits[0];
                    if (digit == 1) {
                        return numerator.withSign(q_negative_p);
                    } else
                        return divide_unsigned_small_denominator(numerator, digit, null, q_negative_p, false);
                } else {
                    BigInt[] results = new BigInt[2];
                    divide_unsigned_large_denominator(numerator, denominator, results, null, q_negative_p, false);
                    return results[0];
                }
            default:
                throw new Error("Impossible to get here");
        }
    }

    /**
     * Divide two BigInts, returning the remainder
     *
     * @see #divide
     */
    public static BigInt remainder(BigInt numerator, BigInt denominator) {
        if (denominator.isZero())
            throw new ArithmeticException("BigInt divide by zero in remainder");
        if (numerator.isZero())
            return ZERO;
        switch (compare_unsigned(numerator, denominator)) {
            case EQUAL:
                return ZERO;
            case LESS:
                return numerator;
            case GREATER:
                if (denominator.last == 0) {
                    int digit = denominator.digits[0];
                    if (digit == 1)
                        return ZERO;
                    else
                        return numerator.remainder_unsigned_small_denominator(digit);
                } else {
                    BigInt[] results = new BigInt[2];
                    divide_unsigned_large_denominator(numerator, denominator, null, results, false, numerator.negative);
                    return results[1];
                }
            default:
                throw new Error("Impossible to get here");
        }
    }

    static int compare_unsigned(BigInt x, BigInt y) {
        int xlen = x.last;
        int ylen = y.last;
        if (xlen < ylen)
            return LESS;
        if (xlen > ylen)
            return GREATER;
        for (int i = xlen; i >= 0; i--) {
            int xdigit = x.digits[i], ydigit = y.digits[i];
            if (xdigit < ydigit)
                return LESS;
            if (xdigit > ydigit)
                return GREATER;
        }
        return EQUAL;
    }

    static BigInt add_unsigned(BigInt x, BigInt y, boolean negative) {
        if (x.last < y.last) {
            BigInt z = x;
            x = y;
            y = z;
        }
        int xlast = x.last;
        int ylast = y.last;
        int[] result = new int[xlast + 2];
        int carry = 0;
        int i = 0;
        while (i <= ylast) {
            int sum = x.digits[i] + y.digits[i] + carry;
            result[i++] = sum & MASK;
            carry = (sum < RADIX) ? 0 : 1;
        }
        if (carry != 0)
            while (i <= xlast) {
                int sum = x.digits[i] + carry;
                if (sum < RADIX) {
                    result[i++] = sum;
                    carry = 0;
                    break;
                } else {
                    result[i++] = sum - RADIX;
                    // carry = 1;
                }
            }
        for (; i <= xlast; i++)
            result[i] = x.digits[i];
        if (carry != 0) {
            result[i] = carry;
            return new BigInt(negative, result, i);
        } else
            return new BigInt(negative, result, i - 1);
    }

    static BigInt subtract_unsigned(BigInt x, BigInt y) {
        boolean negative_p = false;
        switch (compare_unsigned(x, y)) {
            case EQUAL:
                return ZERO;
            case LESS: {
                BigInt z = x;
                x = y;
                y = z;
                negative_p = true;
                break;
            }
            case GREATER:
                // negative_p = false;
        }

        int xlast = x.last;
        int ylast = y.last;
        int[] digits = new int[xlast + 1];
        int i = 0, borrow = 0;
        while (i <= ylast) {
            int difference = x.digits[i] - y.digits[i] - borrow;
            digits[i++] = difference & MASK;
            borrow = (difference < 0) ? 1 : 0;
        }
        if (borrow != 0)
            while (i <= xlast) {
                int difference = x.digits[i] - borrow;
                digits[i++] = difference & MASK;
                if (difference >= 0)
                    break;
            }
        for (; i <= xlast; i++) {
            digits[i] = x.digits[i];
        }
        return new BigInt(negative_p, digits, xlast).trim();
    }

    static BigInt multiply_unsigned_small_factor(BigInt x, int f, boolean negative) {
        int[] digits = new int[x.last + 2];
        int last = scale_up(x.digits, x.last, digits, f, 0);
        return new BigInt(negative, digits, last);
    }

    static BigInt multiply_unsigned(BigInt x, BigInt y, boolean negative) {
        int[] result = new int[x.last + y.last + 2];
        int[] ydigits = y.digits;
        int ylast = y.last;
        // for (int i = result.length; i>0; ) result[--i] = 0;
        for (int ix = 0; ix <= x.last; ix++) {
            long xdigit = x.digits[ix];
            long carry = 0;
            int ir = ix;
            for (int iy = 0; iy <= ylast; iy++) {
                long prod = xdigit * ydigits[iy] + result[ir] + carry;
                result[ir++] = (int) (prod & MASK);
                carry = prod >> BITS;
            }
            while (carry != 0) {
                long sum = result[ir] + carry;
                result[ir++] = (int) (sum & MASK);
                carry = sum >> BITS;
            }
        }
        return new BigInt(negative, result, x.last + y.last + 1).trim();
    }

    BigInt trim() {
        // we could shorten digits if most of it is unused, as happens with
        // small remainders.
        while (last >= 0 && digits[last] == 0)
            last--;
        return this;
    }

    static int scale_up(int[] from, int last, int[] to, int factor, int carry_in) {
        // to[0..] = from[0..last]*factor + carry_in
        // factor and carry_in are limited to be valid digits
        // returns last index used in `to', which is last or last+1
        // from and to may be the same array.
        long l_factor = factor;
        int carry = carry_in;
        int i = 0;
        for (; i <= last; i++) {
            long prod = l_factor * from[i] + carry;
            to[i] = (int) (prod & MASK);
            carry = (int) (prod >> BITS);
        }
        if (carry == 0)
            return last;
        to[i] = carry;
        return last + 1;
    }

    static BigInt divide_unsigned_small_denominator(BigInt numerator, int denominator, BigInt[] remainder_ref, boolean q_negative_p, boolean r_negative_p) {
        int nlast = numerator.last;
        int[] q_digits = new int[nlast + 1];
        System.arraycopy(numerator.digits, 0, q_digits, 0, nlast + 1);
        int r = scale_down(numerator.digits, nlast, q_digits, denominator);
        if (remainder_ref != null)
            remainder_ref[1] = digit_to_BigInt(r, r_negative_p);
        return new BigInt(q_negative_p, q_digits, nlast).trim();
    }

    static void divide_unsigned_large_denominator(BigInt numerator, BigInt denominator, BigInt[] quotient_ref, BigInt[] remainder_ref, boolean q_negative_p, boolean r_negative_p) {
        int last_n = numerator.last + 1;
        int last_d = denominator.last;
        BigInt q = (quotient_ref == null) ? null : allocate(last_n - last_d, q_negative_p);
        BigInt u = allocate(last_n + 1, r_negative_p);
        int shift = 0;
        for (int v1 = denominator.digits[last_d]; v1 < (RADIX / 2); v1 <<= 1)
            shift += 1;
        if (shift == 0) {
            System.arraycopy(numerator.digits, 0, u.digits, 0, u.last);
            u.digits[u.last] = 0;
            divide_unsigned_normalized(u, denominator, q);
        } else {
            BigInt v = allocate(last_d + 1, false);
            destructive_normalization(numerator, u, shift);
            destructive_normalization(denominator, v, shift);
            divide_unsigned_normalized(u, v, q);
            if (remainder_ref != null)
                destructive_unnormalization(u, shift);
        }
        if (quotient_ref != null)
            quotient_ref[0] = q.trim();
        if (remainder_ref != null)
            remainder_ref[1] = u.trim();
    }

    static void divide_unsigned_normalized(BigInt u, BigInt v, BigInt q) {
        int qi = (q == null) ? 0 : q.last;
        int v1 = v.digits[v.last];
        int v2 = v.digits[v.last - 1];
        int[] udigits = u.digits;
        int[] vdigits = v.digits;
        int vlast = v.last;
        long v1L = v1;
        long guess; // an int would do, but it needs to be casted to a long
        long comparand;

        for (int j = u.last; j > vlast; j--) {
            int uj = udigits[j];
            int uj1 = udigits[j - 1];
            int uj2 = udigits[j - 2];
            if (uj == v1) {
                guess = RADIX - 1;
                comparand = (((long) uj1) << BITS) + uj2 + v1;
            } else {
                long ujL = (((long) uj) << BITS) + uj1;
                guess = ujL / v1L;
                comparand = ((ujL % v1L) << BITS) + uj2;
            }
            while (true) {
                long product = v2 * guess;
                if (comparand >= product)
                    break;
                guess -= 1;
                comparand += v1L << BITS;
                if (comparand > RADIX * RADIX)
                    break;
            }
            int qj = divide_subtract(vdigits, vlast, guess, udigits, j - vlast - 1);
            if (q != null)
                q.digits[qi--] = qj;
        }
    }

    static int divide_subtract(int[] vdigits, int vlast, long guess, int[] udigits, int ustart) {
        if (guess == 0)
            return 0;
        int ui = ustart;
        long carry = 0;
        for (int vi = 0; vi <= vlast; vi++) {
            long prod = vdigits[vi] * guess + carry;
            int diff = udigits[ui] - (int) (prod & MASK);
            if (diff < 0) {
                udigits[ui++] = diff + RADIX;
                carry = (prod >> BITS) + 1;
            } else {
                udigits[ui++] = diff;
                carry = (prod >> BITS);
            }
        }
        if (carry == 0)
            return (int) guess;
        {
            int diff = udigits[ui] - (int) carry;
            if (diff < 0)
                udigits[ui] = diff + RADIX;
            else {
                udigits[ui] = diff;
                return (int) guess;
            }
        }
        // Subtraction generated a carry, so the guess is off by one. Add v back
        // to correct. Very rare. See Knuth.
        int icarry = 0;
        ui = ustart;
        for (int vi = 0; vi <= vlast; vi++) {
            int sum = vdigits[vi] + udigits[ui] + icarry;
            udigits[ui++] = sum & MASK;
            icarry = sum >> BITS;
        }
        if (icarry == 1)
            udigits[ui] = (udigits[ui] + 1) & MASK;
        return (int) guess - 1;
    }

    static void destructive_normalization(BigInt source, BigInt target, int shift_left) {
        int carry = 0;
        int shift_right = BITS - shift_left;
        int mask = (1 << shift_right) - 1;
        int last = source.last;
        int[] sd = source.digits, td = target.digits;
        int i = 0;
        for (; i <= last; i++) {
            int digit = sd[i];
            td[i] = ((digit & mask) << shift_left) | carry;
            carry = digit >> shift_right;
        }
        if (i <= target.last)
            td[i] = carry;
    }

    static void destructive_unnormalization(BigInt BigInt, int shift_right) {
        int carry = 0;
        int shift_left = BITS - shift_right;
        int mask = (1 << shift_right) - 1;
        int[] digits = BigInt.digits;
        for (int i = BigInt.last; i >= 0; i--) {
            int digit = digits[i];
            digits[i] = (digit >> shift_right) | carry;
            carry = (digit & mask) << shift_left;
        }
    }

    static int scale_down(int[] from, int last, int[] to, int denominator) {
        // divide `from' by denominator. return remainder.
        // store quotient in `to'.
        // It is the responsibility of the caller to reduce the `last' index
        // if the most significant digit becomes 0
        int i = last;
        long remainder = 0;
        for (; i >= 0; i--) {
            long numerator = from[i] + (remainder << BITS);
            to[i] = (int) (numerator / denominator);
            remainder = numerator % denominator;
        }
        return (int) remainder;
    }

    BigInt remainder_unsigned_small_denominator(int denominator) {
        int i = last;
        long remainder = 0;
        for (; i >= 0; i--) {
            long numerator = digits[i] + (remainder << BITS);
            remainder = numerator % denominator;
        }
        return digit_to_BigInt((int) remainder, negative);
    }

    /**
     * Convert the String into a BigInt integer. The radix is assumed to be 10.
     *
     * @param s the String containing the number
     * @exception NumberFormatException The String cannot be parsed as an integer.
     */
    public static BigInt valueOf(String s) throws NumberFormatException {
        return valueOf(s, 10);
    }

    /**
     * Convert the String into a BigInt integer.
     *
     * @param s the String containing the number
     * @param radix the radix to be used
     * @exception NumberFormatException The String cannot be parsed as an integer.
     */
    public static BigInt valueOf(String s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        int i = 0;
        int max = s.length();
        if (max == 0) {
            throw new NumberFormatException("empty string");
        }
        boolean negative = false;
        if (s.charAt(0) == '-') {
            negative = true;
            i = 1;
        }
        if (i >= max) {
            throw new NumberFormatException(s);
        }
        double logradix = Math.log(radix) * 1.46; // log_2
        int len = (int) ((max - i) * (logradix / BITS) + 1);
        int[] digits = new int[len];
        int last = -1;
        for (; i < max; i++) {
            int digit = Character.digit(s.charAt(i), radix);
            if (digit < 0) {
                throw new NumberFormatException(s);
            }
            last = scale_up(digits, last, digits, radix, digit);
        }
        return new BigInt(negative, digits, last).trim();
    }

    /** Convert to a radix-10 String. */
    @Override
    public String toString() {
        return toString(10);
    }

    /**
     * Convert to a String.
     *
     * @param radix the radix to be used
     */
    public String toString(int radix) {
        if (isZero()) {
            return "0";
        }
        double logradix = Math.log(radix) * 1.44; // log_2
        int len = (int) ((last + 1) * (BITS / logradix) + 2);
        int i = len;
        char[] buf = new char[len];
        int[] tdigits = new int[last + 1];
        System.arraycopy(digits, 0, tdigits, 0, last + 1);
        for (int tlast = last; tlast >= 0;) {
            if (tdigits[tlast] == 0) {
                tlast--;
            } else {
                int digit = scale_down(tdigits, tlast, tdigits, radix);
                buf[--i] = Character.forDigit(digit, radix);
            }
        }
        if (negative) {
            buf[--i] = '-';
        }
        return new String(buf, i, len - i);
    }

    public String dbg() {
        String s = "";
        for (int i = digits.length - 1; i >= 0; i--) {
            String prefix = (i == last) ? " . " : " ";
            s = s + prefix + Integer.toString(digits[i]);
        }
        return "[" + (negative ? "-" : "+") + " <" + last + ">" + s + "]";
    }

    /**
     * Returns the value of the number as an int.
     *
     * @exception ArithmeticException The number is too great to be represented as an int.
     */
    @Override
    public int intValue() {
        long n = 0;
        long limit = negative ? 0x7fffffff : 0x80000000;
        for (int i = last; i >= 0; i--) {
            n = n << BITS + digits[i];
            if (n > limit) {
                throw new ArithmeticException("too big to convert to int" + this);
            }
        }
        return (int) (negative ? -n : n);
    }

    /**
     * Returns the value of the number as a long.
     *
     * @exception ArithmeticException The number is too big to be represented as a long.
     */
    @Override
    public long longValue() {
        long n = 0;
        for (int i = last; i >= 0; i--) {
            long ov = (n >> (63 - BITS));
            n = (n << BITS) + digits[i];
            if (ov != 0) {
                if (negative && ov == 1 && i == 0 && n == 0x8000000000000000L) {
                    return n;
                } else {
                    throw new ArithmeticException("Too big to convert to long " + this);
                }
            }
        }
        return negative ? -n : n;
    }

    /**
     * Returns the value of the number as a float. This may involve loss of precision or overflow.
     */
    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    /**
     * Returns the value of the number as a double. This may involve loss of precision or overflow.
     */
    @Override
    public double doubleValue() {
        // We need only look at sufficient bits to fill the mantissa of a
        // double.
        double n = 0.0;
        int limit = last > DOUBLE_USEFUL_WORDS ? last - DOUBLE_USEFUL_WORDS : 0;
        for (int i = last; i >= limit; i--) {
            n = n * RADIX + digits[i];
        }
        if (limit > 0) {
            n = n * Math.pow(RADIX, limit);
        }
        return negative ? -n : n;
    }

    static final int DOUBLE_MANTISSA_BITS = 53;
    static final int DOUBLE_USEFUL_WORDS = 2 + DOUBLE_MANTISSA_BITS / BITS;

    // Why 2: +1 because division truncates, +1 because the most significant
    // digit might be very small meaning that we must look at an extra word
    // to get enough precision.

    /**
     * Generate a random BigInt.
     *
     * @param rng the random number generator to use. It is called an unspecified number of times.
     * @param limit specifies the range of the result.
     * @return a BigInt in the range 0..limit-1 inclusive.
     */
    public static BigInt random(java.util.Random rng, BigInt limit) {
        if (limit.negative || limit.isZero()) {
            throw new ArithmeticException("Random limit must be positive");
        }
        int[] digits = new int[limit.last + 1];

        while (true) {
            // Generate most significant digit(s) carefully until one of the
            // digits is less than the corresponding digit in `limit'
            int i = limit.last;
            while (i >= 0) {
                int digit = limit.digits[i];
                int mask = 1;
                int trial;
                while (mask < digit) {
                    mask = (mask << 1) + 1;
                }
                do {
                    trial = rng.nextInt() & mask;
                } while (trial > digit);
                digits[i--] = trial;
                if (trial < digit) {
                    // Now the number must be less than limit, so rest of the
                    // digits can be anything.
                    while (i >= 0) {
                        digits[i--] = rng.nextInt() & MASK;
                    }
                    return new BigInt(false, digits, limit.last).trim();
                }
            }
            // We get here only if we generated a number equal to limit.
        }
    }

    /**
     * Used for testing divide. Generates numbers with an exponential-like distribution. Not
     * otherwise useful.
     */
    /*
     * static public BigInt random_digit_exponential (java.util.Random rng, BigInt limit) { if
     * (limit.negative || limit.isZero()) throw new ArithmeticException ("Random limit must be >0");
     * int[] digits = new int[limit.last+1]; int i = limit.last; while (i>=0) { // generate most
     * significant digits int lead = limit.digits[i], mask = 1, trial; while (mask<lead)
     * mask=(mask<<1)+1; do { trial = mask; for (int j = i; j>=0; j--) trial &= rng.nextInt(); }
     * while (trial>lead); digits[i--] = trial; if (trial<lead) break; } for ( ; i>=0; ) { int trial
     * = MASK; for (int j = i; j>=0; j--) trial &= rng.nextInt(); digits[i--] = trial; } return new
     * BigInt (false, digits, limit.last).trim(); }
     */
}
