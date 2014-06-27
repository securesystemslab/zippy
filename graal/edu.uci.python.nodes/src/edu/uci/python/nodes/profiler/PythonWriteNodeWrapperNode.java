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

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.instrument.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem <br>
 *         {@link ForNode} has a child PNode target. Instead of calling execute method on child, it
 *         calls ((WriteNode) target).executeWrite(frame, i); <br>
 *         Therefore, special PythonWriteWrapperNode is generated for this special case.
 */

public class PythonWriteNodeWrapperNode extends PythonWrapperNode implements WriteNode {

    public PythonWriteNodeWrapperNode(PythonContext context, PNode child) {
        super(context, child);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        probe.enter(child, frame);
        Object result;

        try {
            result = child.execute(frame);
            probe.leave(child, frame, result);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
            throw (e);
        }

        return result;
    }

    public Object executeWrite(VirtualFrame frame, Object value) {
        probe.enter(child, frame);
        Object result;

        try {
            result = ((WriteNode) child).executeWrite(frame, value);
            probe.leave(child, frame, result);
        } catch (KillException e) {
            throw (e);
        } catch (Exception e) {
            probe.leaveExceptional(child, frame, e);
            throw (e);
        }

        return result;
    }

    public PNode makeReadNode() {
        throw new RuntimeException("makeReadNode() is not implemented");
    }

    public PNode getRhs() {
        throw new RuntimeException("getRhs() is not implemented");
    }
}
