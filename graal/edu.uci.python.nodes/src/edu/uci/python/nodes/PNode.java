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
package edu.uci.python.nodes;

import java.math.BigInteger;

import org.python.core.*;

import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;
import edu.uci.python.runtime.standardtype.*;

@TypeSystemReference(PythonTypes.class)
public abstract class PNode extends Node {

    public abstract Object execute(VirtualFrame frame);

    public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
        Object o = execute(frame);
        if (o instanceof Integer) {
            return (int) o;
        } else {
            throw new UnexpectedResultException(o);
        }
    }

    public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
        Object o = execute(frame);
        if (o instanceof Double) {
            return (double) o;
        } else if (o instanceof Integer) {
            return (int) o;
        } else {
            throw new UnexpectedResultException(o);
        }
    }

    public char executeCharacter(VirtualFrame frame) throws UnexpectedResultException {
        Object o = execute(frame);
        if (o instanceof Character) {
            return (char) o;
        } else {
            throw new UnexpectedResultException(o);
        }
    }

    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        Object o = execute(frame);

        if (o == Boolean.TRUE) {
            return true;
        } else if (o == Boolean.FALSE) {
            return false;
        } else {
            throw new UnexpectedResultException(o);
        }
    }

    public BigInteger executeBigInteger(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectBigInteger(execute(frame));
    }

    public String executeString(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectString(execute(frame));
    }

    public PString executePString(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPString(execute(frame));
    }

    public PComplex executePComplex(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPComplex(execute(frame));
    }

    public PDict executePDictionary(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPDict(execute(frame));
    }

    public PList executePList(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPList(execute(frame));
    }

    public PTuple executePTuple(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPTuple(execute(frame));
    }

    public PRange executePRange(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPRange(execute(frame));
    }

    public PSequence executePSequence(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPSequence(execute(frame));
    }

    public PSet executePSet(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPSet(execute(frame));
    }

    public PFrozenSet executePFrozenSet(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPFrozenSet(execute(frame));
    }

    public PBaseSet executePBaseSet(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPBaseSet(execute(frame));
    }

    public PIntArray executePIntArray(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPIntArray(execute(frame));
    }

    public PDoubleArray executePDoubleArray(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPDoubleArray(execute(frame));
    }

    public PCharArray executePCharArray(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPCharArray(execute(frame));
    }

    public PArray executePArray(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPArray(execute(frame));
    }

    public PEnumerate executePEnumerate(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPEnumerate(execute(frame));
    }

    public PZip executePZip(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPZip(execute(frame));
    }

    public PSlice executePSlice(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPSlice(execute(frame));
    }

    public PDoubleIterator executePDoubleIterator(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPDoubleIterator(execute(frame));
    }

    public PIntegerIterator executePIntegerIterator(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPIntegerIterator(execute(frame));
    }

    public PIterator executePIterator(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPIterator(execute(frame));
    }

    public PIterable executePIterable(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPIterable(execute(frame));
    }

    public PNone executePNone(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPNone(execute(frame));
    }

    public PythonBuiltinObject executePythonBuiltinObject(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPythonBuiltinObject(execute(frame));
    }

    public PythonModule executePythonModule(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPythonModule(execute(frame));
    }

    public PythonClass executePythonClass(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPythonClass(execute(frame));
    }

    public PythonObject executePythonObject(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPythonObject(execute(frame));
    }

    public PyObject executePyObject(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPyObject(execute(frame));
    }

    public Object[] executeObjectArray(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectObjectArray(execute(frame));
    }

    public PRangeIterator executePRangeIterator(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPRangeIterator(execute(frame));
    }

    public PIntegerSequenceIterator executePIntegerSequenceIterator(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPIntegerSequenceIterator(execute(frame));
    }

    public PSequenceIterator executePSequenceIterator(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPSequenceIterator(execute(frame));
    }

    public PythonCallable executePythonCallable(VirtualFrame frame) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectPythonCallable(execute(frame));
    }

    public void executeVoid(VirtualFrame frame) {
        execute(frame);
    }

    /**
     * Specialization guards.
     */
    protected static boolean isEmptyStorage(PList list) {
        return list.getStorage() instanceof EmptySequenceStorage;
    }

    protected static boolean isIntStorage(PList list) {
        return list.getStorage() instanceof IntSequenceStorage;
    }

    protected static boolean isDoubleStorage(PList list) {
        return list.getStorage() instanceof DoubleSequenceStorage;
    }

}
