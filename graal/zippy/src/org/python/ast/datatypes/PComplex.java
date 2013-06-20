package org.python.ast.datatypes;

public class PComplex extends PObject {
    private double real;
    private double imag;

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
        PComplex result = new PComplex(this.real + c.getReal(), this.imag + c.getImag());
        //result.setReal(this.real + c.getReal());
        //result.setImag(this.imag + c.getImag());
        return result;
    }

    public PComplex sub(PComplex c) {
        PComplex result = new PComplex(this.real - c.getReal(), this.imag - c.getImag());
        //result.setReal(this.real - c.getReal());
        //result.setImag(this.imag - c.getImag());
        return result;
    }

    public PComplex mul(PComplex c) {
        PComplex result = new PComplex(this.real * c.getReal() - this.imag * c.getImag(), this.real * c.getImag() + this.imag * c.getReal());
        //result.setReal(this.real * c.getReal() - this.imag * c.getImag());
        //result.setImag(this.real * c.getImag() + this.imag * c.getReal());
        return result;
    }

    public PComplex div(PComplex c) {
        //PComplex result = new PComplex(this);
        //result = result.mul(c.getConjugate());
        double opNormSq = c.getReal() * c.getReal() + c.getImag() * c.getImag();
        //result.setReal(result.getReal() / opNormSq);
        //result.setImag(result.getImag() / opNormSq);
        PComplex conjugate = c.getConjugate();
        double realPart = this.real * conjugate.getReal() - this.imag * conjugate.getImag();
        double imagPart = this.real * conjugate.getImag() + this.imag * conjugate.getReal();
        return new PComplex(realPart / opNormSq, imagPart / opNormSq);
    }

    public PComplex getConjugate() {
        return new PComplex(this.real, this.imag * (-1));
    }

    public boolean equals(PComplex c) {
        return (real == c.real && imag == c.imag);
    }

    public boolean notEqual(PComplex c) {
        return (real != c.real || imag != c.imag);
    }

    public boolean greaterEqual(PComplex c) {
        throw new RuntimeException("cannot compare complex numbers using <, <=, >, >=");
    }

    public boolean greaterThan(PComplex c) {
        throw new RuntimeException("cannot compare complex numbers using <, <=, >, >=");
    }

    public boolean lessEqual(PComplex c) {
        throw new RuntimeException("cannot compare complex numbers using <, <=, >, >=");
    }

    public boolean lessThan(PComplex c) {
        throw new RuntimeException("cannot compare complex numbers using <, <=, >, >=");
    }

    public void setReal(double real) {
        this.real = real;
    }

    public void setImag(double imag) {
        this.imag = imag;
    }

    public double getReal() {
        return this.real;
    }

    public double getImag() {
        return this.imag;
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
        if (value == Math.floor(value) && value <= Long.MAX_VALUE
                && value >= Long.MIN_VALUE) {
            return Long.toString((long) value);
        } else {
            return Double.toString(value);
        }
    }

    @Override
    public Object getMin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getMax() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int len() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object multiply(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PCallable findAttribute(String name) {
        throw new UnsupportedOperationException();
    }

}

