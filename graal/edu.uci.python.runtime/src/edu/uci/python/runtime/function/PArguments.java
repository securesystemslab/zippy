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
package edu.uci.python.runtime.function;

import java.util.*;
import java.util.concurrent.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.iterator.*;

public class PArguments {

    public static final Object[] EMPTY_ARGUMENTS_ARRAY = new Object[0];
    public static final PArguments EMPTY_ARGUMENT = new PArguments(null, EMPTY_ARGUMENTS_ARRAY, PKeyword.EMPTY_KEYWORDS);

    private final MaterializedFrame declarationFrame;
    private final Object[] arguments;
    private final PKeyword[] keywords;

    public PArguments(MaterializedFrame declarationFrame, Object[] arguments, PKeyword[] keywords) {
        this.declarationFrame = declarationFrame;
        this.arguments = arguments;
        assert arguments != null;
        this.keywords = keywords;
    }

    public PArguments(MaterializedFrame declarationFrame, Object[] arguments) {
        this(declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
    }

    public final Object[] packAsObjectArray() {
        return new Object[]{this};
    }

    public static PArguments get(Frame frame) {
        return CompilerDirectives.unsafeCast(frame.getArguments()[0], PArguments.class, true);
    }

    public static VirtualFrameCargoArguments getVirtualFrameCargoArguments(Frame frame) {
        return CompilerDirectives.unsafeCast(frame.getArguments()[0], VirtualFrameCargoArguments.class, true);
    }

    public static GeneratorArguments getGeneratorArguments(Frame frame) {
        return CompilerDirectives.unsafeCast(frame.getArguments()[0], GeneratorArguments.class, true);
    }

    public static ParallelGeneratorArguments getParallelGeneratorArguments(Frame frame) {
        return CompilerDirectives.unsafeCast(frame.getArguments()[0], ParallelGeneratorArguments.class, true);
    }

    public final int getArgumentsLength() {
        return arguments.length;
    }

    public final Object[] getArgumentsArray() {
        return CompilerDirectives.unsafeCast(arguments, Object[].class, true);
    }

    public MaterializedFrame getDeclarationFrame() {
        return CompilerDirectives.unsafeFrameCast(declarationFrame);
    }

    public final Object getArgument(int index) {
        assert index < arguments.length;
        return arguments[index];
    }

    public PKeyword getKeyword(String name) {
        for (int i = 0; i < keywords.length; i++) {
            PKeyword keyword = keywords[i];
            if (keyword.getName().equals(name)) {
                return keyword;
            }
        }

        return null;
    }

    public PKeyword[] getKeywords() {
        return keywords;
    }

    public int getLength() {
        return arguments.length;
    }

    public static final class GeneratorArguments extends PArguments {

        private final MaterializedFrame generatorFrame;
        // See GeneratorReturnTargetNode, GeneratorIfNode, GeneratorWhileNode.
        private final boolean[] activeFlags;
        private final int[] generatorBlockNodeIndices;       // See {@link GeneratorBlockNode}
        private final PIterator[] generatorForNodeIterators; // See {@link GeneratorForNode}

        public GeneratorArguments(MaterializedFrame declarationFrame, MaterializedFrame generatorFrame, Object[] arguments, int numOfActiveFlags, int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
            super(declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
            this.generatorFrame = generatorFrame;
            this.activeFlags = new boolean[numOfActiveFlags];
            this.generatorBlockNodeIndices = new int[numOfGeneratorBlockNode];
            this.generatorForNodeIterators = new PIterator[numOfGeneratorForNode];
        }

        public MaterializedFrame getGeneratorFrame() {
            return CompilerDirectives.unsafeFrameCast(generatorFrame);
        }

        public boolean getActive(int slot) {
            return activeFlags[slot];
        }

        public void setActive(int slot, boolean flag) {
            activeFlags[slot] = flag;
        }

        public int getBlockIndexAt(int slot) {
            return generatorBlockNodeIndices[slot];
        }

        public void setBlockIndexAt(int slot, int value) {
            generatorBlockNodeIndices[slot] = value;
        }

        public PIterator getIteratorAt(int slot) {
            return generatorForNodeIterators[slot];
        }

        public void setIteratorAt(int slot, PIterator value) {
            generatorForNodeIterators[slot] = value;
        }
    }

    /**
     * Carry the {@link VirtualFrame} into a inlined Python function.<br>
     * Should only be used within a complete Truffle compilation unit and never escape it.
     */
    public static final class VirtualFrameCargoArguments extends PArguments {

        private final VirtualFrame cargoFrame;

        public VirtualFrameCargoArguments(MaterializedFrame declarationFrame, VirtualFrame cargoFrame, Object[] arguments) {
            super(declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
            this.cargoFrame = cargoFrame;
        }

        public VirtualFrame getCargoFrame() {
            return cargoFrame;
        }
    }

    public static final class ParallelGeneratorArguments extends PArguments {

        private final BlockingQueue<Object> blockingQueue;
        private final SingleProducerCircularBuffer buffer;
        private final Queue<Object> queue;
        private final DisruptorRingBufferHandler ringBuffer;

        public ParallelGeneratorArguments(MaterializedFrame declarationFrame, BlockingQueue<Object> queue, Object[] arguments) {
            super(declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
            this.blockingQueue = queue;
            this.buffer = null;
            this.queue = null;
            this.ringBuffer = null;
        }

        public ParallelGeneratorArguments(MaterializedFrame declarationFrame, SingleProducerCircularBuffer buffer, Object[] arguments) {
            super(declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
            this.blockingQueue = null;
            this.buffer = buffer;
            this.queue = null;
            this.ringBuffer = null;
        }

        public ParallelGeneratorArguments(MaterializedFrame declarationFrame, Queue<Object> queue, Object[] arguments) {
            super(declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
            this.blockingQueue = null;
            this.buffer = null;
            this.queue = queue;
            this.ringBuffer = null;
        }

        public ParallelGeneratorArguments(MaterializedFrame declarationFrame, DisruptorRingBufferHandler ringBuffer, Object[] arguments) {
            super(declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
            this.blockingQueue = null;
            this.buffer = null;
            this.queue = null;
            this.ringBuffer = ringBuffer;
        }

        public BlockingQueue<Object> getBlockingQueue() {
            assert blockingQueue != null;
            return blockingQueue;
        }

        public SingleProducerCircularBuffer getBuffer() {
            assert buffer != null;
            return buffer;
        }

        public Queue<Object> getQueue() {
            return queue;
        }

        public DisruptorRingBufferHandler getRingBuffer() {
            return ringBuffer;
        }
    }

}
