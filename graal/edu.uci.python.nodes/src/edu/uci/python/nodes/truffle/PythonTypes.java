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
package edu.uci.python.nodes.truffle;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.TypeCast;
import com.oracle.truffle.api.dsl.TypeCheck;
import com.oracle.truffle.api.dsl.TypeSystem;

import edu.uci.python.runtime.datatypes.*;

@TypeSystem({int.class, BigInteger.class, double.class, PComplex.class, char.class, boolean.class, String.class,

PDictionary.class, PList.class, PTuple.class, PSequence.class, PSet.class, PFrozenSet.class, PBaseSet.class, PIntegerArray.class, PDoubleArray.class, PCharArray.class, PArray.class, PSlice.class,
                PObject.class, PCallable.class})
public class PythonTypes {

    @TypeCheck
    public boolean isBigInteger(Object value) {
        return value instanceof BigInteger || value instanceof Integer;
    }

    @TypeCast
    public BigInteger asBigInteger(Object value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        } else {
            return BigInteger.valueOf((int) value);
        }
    }

    @TypeCast
    public BigInteger asBigInteger(int value) {
        return BigInteger.valueOf(value);
    }

    @TypeCheck
    public boolean isDouble(Object value) {
        return value instanceof Double || value instanceof Integer || value instanceof BigInteger;
    }

    @TypeCast
    public double asDouble(Object value) {
        if (value instanceof Double) {
            return (double) value;
        } else if (value instanceof Integer) {
            Integer integer = (Integer) value;
            return integer.doubleValue();
        } else {
            BigInteger bigInteger = (BigInteger) value;
            return bigInteger.doubleValue();
        }
    }

    @TypeCast
    public double asDouble(int value) {
        return value;
    }

    @TypeCheck
    public boolean isPComplex(Object value) {
        return value instanceof PComplex || value instanceof Integer || value instanceof Double || value instanceof BigInteger;
    }

    @TypeCast
    public PComplex asPComplex(Object value) {
        if (value instanceof PComplex) {
            PComplex complex = (PComplex) value;
            return complex;
        } else if (value instanceof Integer) {
            PComplex complex = new PComplex((Integer) value, 0);
            return complex;
        } else if (value instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) value;
            PComplex complex = new PComplex(bigInteger.doubleValue(), 0);
            return complex;
        } else if (value instanceof Double) {
            PComplex complex = new PComplex((Double) value, 0);
            return complex;
        } else {
            PComplex complex = (PComplex) value;
            return complex;
        }
    }
}
