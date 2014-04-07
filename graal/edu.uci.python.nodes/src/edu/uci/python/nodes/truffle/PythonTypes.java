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

import org.python.core.*;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.function.PArguments.GeneratorArguments;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

@TypeSystem({boolean.class, //
                int.class, //
                BigInteger.class, //
                double.class, //
                PComplex.class, //
                char.class, //
                String.class,

                PyObject.class, //
                PString.class, //
                PythonClass.class, //
                PDict.class, //
                PList.class, //
                PTuple.class, //
                PRange.class, //
                PIntArray.class, //
                PDoubleArray.class, //
                PCharArray.class, //
                PArray.class, //
                PSequence.class, //
                PSet.class, //
                PFrozenSet.class, //
                PBaseSet.class, //
                PEnumerate.class, //
                PZip.class, //
                PSlice.class, //
                PRangeIterator.class, //
                PDoubleIterator.class, //
                PIntegerIterator.class, //
                PIterator.class, //
                PIterable.class, //
                PythonModule.class, //
                PNone.class, //
                PythonBuiltinObject.class, //
                PythonObject.class, //
                PythonBasicObject.class, //
                PythonCallable.class, //
                GeneratorArguments.class, //
                Object[].class})
public class PythonTypes {

    /**
     * Type coercion: Python bool to Python int (Integer).
     */
    @ImplicitCast
    public int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * Type coercion: <br>
     * Python bool to Python int (BigInteger); <br>
     * Python int to int (Integer to BigInteger).
     */
    @TypeCheck
    public boolean isBigInteger(Object value) {
        return value instanceof BigInteger || value instanceof Integer || value instanceof Boolean;
    }

    @TypeCast
    public BigInteger asBigInteger(boolean value) {
        return value ? BigInteger.valueOf(1) : BigInteger.valueOf(0);
    }

    @TypeCast
    public BigInteger asBigInteger(int value) {
        return BigInteger.valueOf(value);
    }

    @TypeCast
    public BigInteger asBigInteger(Object value) {
        if (value instanceof Integer) {
            return BigInteger.valueOf((int) value);
        } else if (value instanceof Boolean) {
            int intValue = (boolean) value ? 1 : 0;
            return BigInteger.valueOf(intValue);
        }

        return (BigInteger) value;
    }

    @ImplicitCast
    public BigInteger intToBigInteger(int value) {
        return BigInteger.valueOf(value);
    }

    /**
     * Type coercion: <br>
     * Python bool to Python float (double); <br>
     * Python int to float (Integer or BigInteger to double).
     */
    @TypeCheck
    public boolean isDouble(Object value) {
        return value instanceof Double || value instanceof Integer || value instanceof BigInteger || value instanceof Boolean;
    }

    @TypeCast
    public double asDouble(boolean value) {
        return value ? 1.0D : 0.0D;
    }

    @TypeCast
    public double asDouble(int value) {
        return value;
    }

    @TypeCast
    public double asDouble(Object value) {
        if (value instanceof Integer) {
            Integer integer = (Integer) value;
            return integer.doubleValue();
        } else if (value instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) value;
            return bigInteger.doubleValue();
        } else if (value instanceof Boolean) {
            return (boolean) value ? 1.0D : 0.0D;
        }

        return (double) value;
    }

    // @ImplicitCast
    public double booleanToDouble(boolean value) {
        return value ? 1.0D : 0.0D;
    }

    // @ImplicitCast
    public double intToDouble(int value) {
        return value;
    }

    // @ImplicitCast
    public double bigIntegerToDouble(BigInteger value) {
        return value.doubleValue();
    }

    /**
     * Type coercion: <br>
     * Python bool to Python complex; <br>
     * Python int to Python complex (Integer or BigInteger to PComplex); <br>
     * Python float to Python complex (double to PComplex).
     */
    @TypeCheck
    public boolean isPComplex(Object value) {
        return value instanceof PComplex || value instanceof Integer || value instanceof Double || value instanceof BigInteger || value instanceof Boolean;
    }

    @TypeCast
    public PComplex asPComplex(Object value) {
        if (value instanceof Integer) {
            PComplex complex = new PComplex((Integer) value, 0);
            return complex;
        } else if (value instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) value;
            PComplex complex = new PComplex(bigInteger.doubleValue(), 0);
            return complex;
        } else if (value instanceof Double) {
            PComplex complex = new PComplex((Double) value, 0);
            return complex;
        } else if (value instanceof Boolean) {
            return (boolean) value ? new PComplex(1, 0) : new PComplex(0, 0);
        }

        return (PComplex) value;
    }

    // @ImplicitCast
    public PComplex intToPComplex(int value) {
        return new PComplex(value, 0);
    }

    // @ImplicitCast
    public PComplex bigIntegerToPComplex(BigInteger value) {
        return new PComplex(value.doubleValue(), 0);
    }

    // @ImplicitCast
    public PComplex doubleToPComplex(double value) {
        return new PComplex(value, 0);
    }

    @ImplicitCast
    public String unboxPString(PString value) {
        return value.getValue();
    }

}
