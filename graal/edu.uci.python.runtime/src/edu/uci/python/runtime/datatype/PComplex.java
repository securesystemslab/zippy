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
package edu.uci.python.runtime.datatype;

import edu.uci.python.runtime.standardtype.*;

public class PComplex extends PythonBuiltinObject {

    private final double real;
    private final double imag;

    public PComplex() {
        this.real = 0;
        this.imag = 0;
    }

    public PComplex(double real, double imaginary) {
        this.real = real;
        this.imag = imaginary;
    }

    public PComplex(PComplex c) {
        this.real = c.getReal();
        this.imag = c.getImag();
    }

    public PComplex add(PComplex c) {
        PComplex result = new PComplex(real + c.getReal(), imag + c.getImag());
        return result;
    }

    public PComplex sub(PComplex c) {
        PComplex result = new PComplex(real - c.getReal(), imag - c.getImag());
        return result;
    }

    public PComplex mul(PComplex c) {
        double newReal = real * c.getReal() - imag * c.getImag();
        double newImage = real * c.getImag() + imag * c.getReal();
        return new PComplex(newReal, newImage);
    }

    public PComplex div(PComplex c) {
        double opNormSq = c.getReal() * c.getReal() + c.getImag() * c.getImag();
        PComplex conjugate = c.getConjugate();
        double realPart = real * conjugate.getReal() - imag * conjugate.getImag();
        double imagPart = real * conjugate.getImag() + imag * conjugate.getReal();
        return new PComplex(realPart / opNormSq, imagPart / opNormSq);
    }

    public PComplex getConjugate() {
        return new PComplex(real, imag * (-1));
    }

    public boolean equals(PComplex c) {
        return (real == c.real && imag == c.imag);
    }

    public boolean notEqual(PComplex c) {
        return (real != c.real || imag != c.imag);
    }

    @SuppressWarnings("unused")
    public boolean greaterEqual(PComplex c) {
        throw new RuntimeException("cannot compare complex numbers using <, <=, >, >=");
    }

    @SuppressWarnings("unused")
    public boolean greaterThan(PComplex c) {
        throw new RuntimeException("cannot compare complex numbers using <, <=, >, >=");
    }

    @SuppressWarnings("unused")
    public boolean lessEqual(PComplex c) {
        throw new RuntimeException("cannot compare complex numbers using <, <=, >, >=");
    }

    @SuppressWarnings("unused")
    public boolean lessThan(PComplex c) {
        throw new RuntimeException("cannot compare complex numbers using <, <=, >, >=");
    }

    public final double getReal() {
        return real;
    }

    public final double getImag() {
        return imag;
    }

    @Override
    public String toString() {
        if (real == 0.) {
            return toString(imag) + "j";
        } else {
            if (imag >= 0) {
                return String.format("(%s+%sj)", toString(real), toString(imag));
            } else {
                return String.format("(%s-%sj)", toString(real), toString(-imag));
            }
        }
    }

    private static String toString(double value) {
        if (value == Math.floor(value) && value <= Long.MAX_VALUE && value >= Long.MIN_VALUE) {
            return Long.toString((long) value);
        } else {
            return Double.toString(value);
        }
    }

}
