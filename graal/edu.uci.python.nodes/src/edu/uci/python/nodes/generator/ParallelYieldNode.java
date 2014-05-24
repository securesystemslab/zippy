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
package edu.uci.python.nodes.generator;

import java.util.*;
import java.util.concurrent.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;

public abstract class ParallelYieldNode extends YieldNode {

    public ParallelYieldNode(PNode right) {
        super(right);
    }

    public static ParallelYieldNode create(PNode right) {
        switch (PParallelGenerator.QUEUE_CHOICE) {
            case 0:
                return new YieldToCirculrBufferNode(right);
            case 1:
                return new YieldToBlockingQueueNode(right);
            case 2:
                return new YieldToQueueNode(right);
            default:
                throw new IllegalStateException();
        }
    }

    protected abstract void appendValue(VirtualFrame frame, Object value);

    @Override
    public Object execute(VirtualFrame frame) {
        appendValue(frame, right.execute(frame));
        return PNone.NONE;
    }

    public static final class YieldToBlockingQueueNode extends ParallelYieldNode {

        public YieldToBlockingQueueNode(PNode right) {
            super(right);
        }

        @Override
        protected void appendValue(VirtualFrame frame, Object value) {
            try {
                BlockingQueue<Object> queue = PArguments.getParallelGeneratorArguments(frame).getBlockingQueue();
                queue.put(value);
            } catch (InterruptedException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                e.printStackTrace();
            }
        }
    }

    public static final class YieldToCirculrBufferNode extends ParallelYieldNode {

        public YieldToCirculrBufferNode(PNode right) {
            super(right);
        }

        @Override
        protected void appendValue(VirtualFrame frame, Object value) {
            SingleProducerCircularBuffer buffer = PArguments.getParallelGeneratorArguments(frame).getBuffer();
            buffer.put(value);
        }
    }

    public static final class YieldToQueueNode extends ParallelYieldNode {

        public YieldToQueueNode(PNode right) {
            super(right);
        }

        @Override
        protected void appendValue(VirtualFrame frame, Object value) {
            Queue<Object> queue = PArguments.getParallelGeneratorArguments(frame).getQueue();
            while (!queue.offer(value)) {
                // spin
            }
        }
    }

}
