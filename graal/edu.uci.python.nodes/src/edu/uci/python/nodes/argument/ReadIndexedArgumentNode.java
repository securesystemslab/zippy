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
package edu.uci.python.nodes.argument;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;

/**
 * This is the right hand side of Parameters' WriteLocalNode.
 *
 * @author zwei
 *
 */
public abstract class ReadIndexedArgumentNode extends PNode {

    protected final int index;

    public ReadIndexedArgumentNode(int index) {
        this.index = index;
    }

    public final int getIndex() {
        return index;
    }

    public static ReadIndexedArgumentNode create(int idx) {
        return new UninitializedReadArgumentNode(idx);
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    public static final class InBoundReadArgumentNode extends ReadIndexedArgumentNode {

        public InBoundReadArgumentNode(int index) {
            super(index);
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            return PythonTypesGen.PYTHONTYPES.expectBoolean(execute(frame));
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            return PythonTypesGen.PYTHONTYPES.expectInteger(execute(frame));
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            return PythonTypesGen.PYTHONTYPES.expectDouble(execute(frame));
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PArguments args = frame.getArguments(PArguments.class);
            return args.getArgument(index);
        }
    }

    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    public static final class OffBoundReadArgumentNode extends ReadIndexedArgumentNode {

        public OffBoundReadArgumentNode(int index) {
            super(index);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return PNone.NONE;
        }
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static final class UninitializedReadArgumentNode extends ReadIndexedArgumentNode {

        public UninitializedReadArgumentNode(int index) {
            super(index);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PArguments args = frame.getArguments(PArguments.class);

            if (index >= args.getActualArgumentsLength()) {
                replace(new OffBoundReadArgumentNode(index));
                return PNone.NONE;
            } else {
                replace(new InBoundReadArgumentNode(index));
                return args.getArgument(index);
            }
        }
    }

}
