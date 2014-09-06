package edu.uci.python.profiler;

/*
 * Copyright (c) 2014, Regents of the University of California
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

import java.math.*;

import org.python.core.*;

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.datatype.PSlice.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

/**
 * @author Gulfem
 */

public class PythonWrapperNode extends PNode implements Wrapper {

    @Child protected PNode child;

    protected final Probe probe;

    public PythonWrapperNode(PythonContext context, PNode child) {
        /**
         * Don't insert the child here, because child node will be replaced with the wrapper node.
         * If child node is inserted here, it's parent (which will be wrapper's parent after
         * replacement) will be lost. Instead, wrapper is created, and the child is replaced with
         * its wrapper, and then wrapper's child is adopted by calling adoptChildren() in
         * {@link ProfilerTranslator}.
         */
        this.child = child;
        /**
         * context.getProbe will either generate a probe for this source section, or return the
         * existing probe for this section. There can be only one probe for the same source section.
         */
        this.probe = context.getProbe(child.getSourceSection());
    }

    @Override
    public Object execute(VirtualFrame frame) {
        probe.enter(child, frame);
        Object result;

        try {
            result = child.execute(frame);
            probe.leave(child, frame);
        } catch (KillException e) {
            probe.leaveExceptional(child, frame, e);
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        int result;

        try {
            result = child.executeInt(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        double result;

        try {
            result = child.executeDouble(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public char executeCharacter(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        char result;

        try {
            result = child.executeCharacter(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        boolean result;

        try {
            result = child.executeBoolean(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public BigInteger executeBigInteger(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        BigInteger result;

        try {
            result = child.executeBigInteger(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public String executeString(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        String result;

        try {
            result = child.executeString(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PString executePString(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PString result;

        try {
            result = child.executePString(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PComplex executePComplex(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PComplex result;

        try {
            result = child.executePComplex(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PBytes executeBytes(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PBytes result;

        try {
            result = child.executeBytes(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PDict executePDictionary(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PDict result;

        try {
            result = child.executePDictionary(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PList executePList(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PList result;

        try {
            result = child.executePList(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PTuple executePTuple(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PTuple result;

        try {
            result = child.executePTuple(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PRange executePRange(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PRange result;

        try {
            result = child.executePRange(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PSequence executePSequence(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PSequence result;

        try {
            result = child.executePSequence(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PSet executePSet(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PSet result;

        try {
            result = child.executePSet(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PFrozenSet executePFrozenSet(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PFrozenSet result;

        try {
            result = child.executePFrozenSet(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PBaseSet executePBaseSet(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PBaseSet result;

        try {
            result = child.executePBaseSet(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PIntArray executePIntArray(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PIntArray result;

        try {
            result = child.executePIntArray(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PDoubleArray executePDoubleArray(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PDoubleArray result;

        try {
            result = child.executePDoubleArray(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PCharArray executePCharArray(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PCharArray result;

        try {
            result = child.executePCharArray(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PArray executePArray(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PArray result;

        try {
            result = child.executePArray(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PEnumerate executePEnumerate(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PEnumerate result;

        try {
            result = child.executePEnumerate(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PZip executePZip(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PZip result;

        try {
            result = child.executePZip(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PStartSlice executePStartSlice(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PStartSlice result;

        try {
            result = child.executePStartSlice(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PStopSlice executePStopSlice(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PStopSlice result;

        try {
            result = child.executePStopSlice(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PSlice executePSlice(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PSlice result;

        try {
            result = child.executePSlice(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PGenerator executePGenerator(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PGenerator result;

        try {
            result = child.executePGenerator(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PDoubleIterator executePDoubleIterator(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PDoubleIterator result;

        try {
            result = child.executePDoubleIterator(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PIntegerIterator executePIntegerIterator(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PIntegerIterator result;

        try {
            result = child.executePIntegerIterator(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PIterator executePIterator(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PIterator result;

        try {
            result = child.executePIterator(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PIterable executePIterable(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PIterable result;

        try {
            result = child.executePIterable(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PNone executePNone(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PNone result;

        try {
            result = child.executePNone(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PythonBuiltinClass executePythonBuiltinClass(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PythonBuiltinClass result;

        try {
            result = child.executePythonBuiltinClass(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PythonBuiltinObject executePythonBuiltinObject(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PythonBuiltinObject result;

        try {
            result = child.executePythonBuiltinObject(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PythonModule executePythonModule(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PythonModule result;

        try {
            result = child.executePythonModule(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PythonClass executePythonClass(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PythonClass result;

        try {
            result = child.executePythonClass(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PythonObject executePythonObject(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PythonObject result;

        try {
            result = child.executePythonObject(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PyObject executePyObject(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PyObject result;

        try {
            result = child.executePyObject(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public Object[] executeObjectArray(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        Object[] result;

        try {
            result = child.executeObjectArray(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PRangeIterator executePRangeIterator(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PRangeIterator result;

        try {
            result = child.executePRangeIterator(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PIntegerSequenceIterator executePIntegerSequenceIterator(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PIntegerSequenceIterator result;

        try {
            result = child.executePIntegerSequenceIterator(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PSequenceIterator executePSequenceIterator(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PSequenceIterator result;

        try {
            result = child.executePSequenceIterator(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    @Override
    public PythonCallable executePythonCallable(VirtualFrame frame) throws UnexpectedResultException {
        probe.enter(child, frame);
        PythonCallable result;

        try {
            result = child.executePythonCallable(frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            throw (e);
        }

        return result;
    }

    public PNode getChild() {
        return child;
    }

    public Probe getProbe() {
        return probe;
    }

    @SlowPath
    public boolean isTaggedAs(PhylumTag tag) {
        return probe.isTaggedAs(tag);
    }

    @SlowPath
    public Iterable<PhylumTag> getPhylumTags() {
        return probe.getPhylumTags();
    }

}
