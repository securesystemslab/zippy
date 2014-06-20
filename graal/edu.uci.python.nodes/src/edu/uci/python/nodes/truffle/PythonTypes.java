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
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.datatype.PSlice.PStartSlice;
import edu.uci.python.runtime.datatype.PSlice.PStopSlice;
import edu.uci.python.runtime.function.*;
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
                String.class, //
                PyObject.class, //
                PString.class, //
                PythonBuiltinClass.class, //
                PythonClass.class, //
                PDict.class, //
                PBytes.class, //
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
                PStartSlice.class, //
                PStopSlice.class, //
                PSlice.class, //
                PGenerator.class, //
                PRangeIterator.class, //
                PIntegerSequenceIterator.class, //
                PSequenceIterator.class, //
                PDoubleIterator.class, //
                PIntegerIterator.class, //
                PIterator.class, //
                PIterable.class, //
                PythonModule.class, //
                PNone.class, //
                PythonBuiltinObject.class, //
                PythonObject.class, //
                PythonCallable.class, //
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
    public BigInteger booleanToBigInteger(boolean value) {
        return value ? BigInteger.valueOf(1) : BigInteger.valueOf(0);
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
    public double booleanToDouble(boolean value) {
        return value ? 1.0D : 0.0D;
    }

    @ImplicitCast
    public double intToDouble(int value) {
        return value;
    }

    public double bigIntegerToDouble(BigInteger value) {
        return value.doubleValue();
    }

    /**
     * Type coercion: <br>
     * Python bool to Python complex; <br>
     * Python int to Python complex (Integer or BigInteger to PComplex); <br>
     * Python float to Python complex (double to PComplex).
     */
    public PComplex booleanToPComplex(boolean value) {
        return value ? new PComplex(1, 0) : new PComplex(0, 0);
    }

    public PComplex intToPComplex(int value) {
        return new PComplex(value, 0);
    }

    public PComplex bigIntegerToPComplex(BigInteger value) {
        return new PComplex(value.doubleValue(), 0);
    }

    @ImplicitCast
    public PComplex doubleToPComplex(double value) {
        return new PComplex(value, 0);
    }

    @ImplicitCast
    public String unboxPString(PString value) {
        return value.getValue();
    }

}
