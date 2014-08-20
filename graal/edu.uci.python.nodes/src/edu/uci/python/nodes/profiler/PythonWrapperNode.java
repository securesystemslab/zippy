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
package edu.uci.python.nodes.profiler;

import java.math.*;

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;

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
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
            probe.leave(child, frame);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
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
