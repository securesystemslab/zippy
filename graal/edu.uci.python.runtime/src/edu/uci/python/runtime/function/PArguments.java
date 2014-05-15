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
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.object.*;

public class PArguments {

    public static final Object[] EMPTY_ARGUMENTS_ARRAY = new Object[0];
    public static final PArguments EMPTY_ARGUMENT = new PArguments(null, EMPTY_ARGUMENTS_ARRAY, PKeyword.EMPTY_KEYWORDS);

    public static final int INDEX_DECLARATION_FRAME = 0;
    public static final int INDEX_KEYWORD_ARGUMENTS = 1;
    public static final int INDEX_SPECIAL_ARGUMENTS = 2;
    public static final int USER_ARGUMENTS_OFFSET = 3;

    public static Object[] EMPTY = new Object[]{null, null, PKeyword.EMPTY_KEYWORDS};

    public static Object[] create() {
        return new Object[]{null, PKeyword.EMPTY_KEYWORDS, null};
    }

    public static Object[] create(int userArgumentCount) {
        Object[] initialArguments = new Object[USER_ARGUMENTS_OFFSET + userArgumentCount];
        initialArguments[INDEX_KEYWORD_ARGUMENTS] = PKeyword.EMPTY_KEYWORDS;
        return initialArguments;
    }

    public static Object[] createWithUserArguments(Object... userArguments) {
        Object[] arguments = create(userArguments.length);

        for (int i = 0; i < userArguments.length; i++) {
            arguments[USER_ARGUMENTS_OFFSET + i] = userArguments[i];
        }

        return arguments;
    }

    public static void setDeclarationFrame(Object[] arguments, MaterializedFrame declarationFrame) {
        arguments[INDEX_DECLARATION_FRAME] = declarationFrame;
    }

    public static MaterializedFrame getDeclarationFrame(Object[] arguments) {
        return (MaterializedFrame) arguments[INDEX_DECLARATION_FRAME];
    }

    public static void setKeywordArguments(Object[] arguments, PKeyword[] keywordArguments) {
        arguments[INDEX_KEYWORD_ARGUMENTS] = keywordArguments;
    }

    public static PKeyword[] getKeywordArguments(Object[] arguments) {
        return (PKeyword[]) arguments[INDEX_KEYWORD_ARGUMENTS];
    }

    public static void setArgument(Object[] arguments, int index, Object value) {
        arguments[USER_ARGUMENTS_OFFSET + index] = value;
    }

    public static Object getArgument(Object[] arguments, int index) {
        return arguments[USER_ARGUMENTS_OFFSET + index];
    }

    public static int getUserArgumentLength(VirtualFrame frame) {
        return frame.getArguments().length - USER_ARGUMENTS_OFFSET;
    }

    public static Object[] insertSelf(Object[] arguments, PythonObject self) {
        final int userArgumentLength = arguments.length - USER_ARGUMENTS_OFFSET;
        Object[] results = create(userArgumentLength + 1);
        results[USER_ARGUMENTS_OFFSET] = self;

        for (int i = 0; i < userArgumentLength; i++) {
            results[USER_ARGUMENTS_OFFSET + 1 + i] = arguments[USER_ARGUMENTS_OFFSET + i];
        }

        return results;
    }

    public static Object[] extractUserArguments(Object[] arguments) {
        int userArgumentLength = arguments.length - USER_ARGUMENTS_OFFSET;
        Object[] userArguments = new Object[userArgumentLength];
        System.arraycopy(arguments, USER_ARGUMENTS_OFFSET, userArguments, 0, userArgumentLength);
        return userArguments;
    }

    public static Object[] applyKeywordArgs(Arity calleeArity, Object[] arguments, PKeyword[] keywords) {
        List<String> parameters = calleeArity.getParameterIds();
        Object[] combined = create(parameters.size());
        assert combined.length >= arguments.length : "Parameters size does not match";
        System.arraycopy(arguments, 0, combined, 0, arguments.length);

        for (int i = 0; i < keywords.length; i++) {
            PKeyword keyarg = keywords[i];
            int keywordIdx = parameters.indexOf(keyarg.getName());

            if (keywordIdx < -1) {
                /**
                 * TODO can throw a type error for wrong keyword name // TypeError: foo() got an
                 * unexpected keyword argument 'c'
                 */
            }

            combined[USER_ARGUMENTS_OFFSET + keywordIdx] = keyarg.getValue();
        }

        return combined;
    }

    @ExplodeLoop
    public static PKeyword getKeyword(Object[] arguments, String name) {
        PKeyword[] keywordArguments = getKeywordArguments(arguments);

        for (int i = 0; i < keywordArguments.length; i++) {
            PKeyword keyword = keywordArguments[i];

            if (keyword.getName().equals(name)) {
                return keyword;
            }
        }

        return null;
    }

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
        return CompilerDirectives.unsafeCast(frame.getArguments()[INDEX_SPECIAL_ARGUMENTS], VirtualFrameCargoArguments.class, true);
    }

    public static GeneratorArguments getGeneratorArguments(Frame frame) {
        return CompilerDirectives.unsafeCast(frame.getArguments()[INDEX_SPECIAL_ARGUMENTS], GeneratorArguments.class, true);
    }

    public static ParallelGeneratorArguments getParallelGeneratorArguments(Frame frame) {
        return CompilerDirectives.unsafeCast(frame.getArguments()[INDEX_SPECIAL_ARGUMENTS], ParallelGeneratorArguments.class, true);
    }

    public final int getArgumentsLength() {
        return arguments.length;
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

    public static void setGeneratorArguments(Object[] arguments, GeneratorArguments generatorArguments) {
        arguments[INDEX_SPECIAL_ARGUMENTS] = generatorArguments;
    }

    public static void setParallelGeneratorArguments(Object[] arguments, ParallelGeneratorArguments generatorArguments) {
        arguments[INDEX_SPECIAL_ARGUMENTS] = generatorArguments;
    }

    public static final class GeneratorArguments {

        private final MaterializedFrame generatorFrame;
        // See GeneratorReturnTargetNode, GeneratorIfNode, GeneratorWhileNode.
        private final boolean[] activeFlags;
        private final int[] generatorBlockNodeIndices;       // See {@link GeneratorBlockNode}
        private final PIterator[] generatorForNodeIterators; // See {@link GeneratorForNode}

        public GeneratorArguments(MaterializedFrame generatorFrame, int numOfActiveFlags, int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
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

    public static final class ParallelGeneratorArguments {

        private final BlockingQueue<Object> blockingQueue;
        private final SingleProducerCircularBuffer buffer;
        private final Queue<Object> queue;
        private final DisruptorRingBufferHandler ringBuffer;

        public ParallelGeneratorArguments(BlockingQueue<Object> queue) {
            this.blockingQueue = queue;
            this.buffer = null;
            this.queue = null;
            this.ringBuffer = null;
        }

        public ParallelGeneratorArguments(SingleProducerCircularBuffer buffer) {
            this.blockingQueue = null;
            this.buffer = buffer;
            this.queue = null;
            this.ringBuffer = null;
        }

        public ParallelGeneratorArguments(Queue<Object> queue) {
            this.blockingQueue = null;
            this.buffer = null;
            this.queue = queue;
            this.ringBuffer = null;
        }

        public ParallelGeneratorArguments(DisruptorRingBufferHandler ringBuffer) {
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
