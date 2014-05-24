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
import edu.uci.python.runtime.object.*;

//@formatter:off
/**
 * The layout of an argument array.
 *
 *                            +-------------------+
 * INDEX_DECLARATION_FRAME -> | MaterializedFrame |
 *                            +-------------------+
 * INDEX_KEYWORD_ARGUMENTS -> | PKeyword[]        |
 *                            +-------------------+
 * INDEX_GENERATOR_FRAME   -> | MaterializedFrame |
 *                            +-------------------+
 * USER_ARGUMENTS          -> | arg_0             |
 *                            | arg_1             |
 *                            | ...               |
 *                            | arg_(nArgs-1)     |
 *                            +-------------------+
 *
 * The layout of a generator frame expanded from the figure above.
 *
 * MaterializedFrame
 *       |
 *       |
 *       |---- arguments: +-------------------+
 *                        | MaterializedFrame |
 *                        +-------------------+
 *                        | PKeyword[]        |
 *                        +-------------------+
 *                        | MaterializedFrame | <- GeneratorArguments
 *                        +-------------------+
 *
 */
//@formatter:on
public class PArguments {

    public static final int INDEX_DECLARATION_FRAME = 0;
    public static final int INDEX_KEYWORD_ARGUMENTS = 1;
    public static final int INDEX_GENERATOR_FRAME = 2;
    public static final int USER_ARGUMENTS_OFFSET = 3;

    private static final Object[] EMPTY_ARGUMENTS = new Object[]{null, PKeyword.EMPTY_KEYWORDS, null};

    public static Object[] empty() {
        return EMPTY_ARGUMENTS;
    }

    public static Object[] create() {
        return new Object[]{null, PKeyword.EMPTY_KEYWORDS, null};
    }

    public static Object[] create(int userArgumentLength) {
        Object[] initialArguments = new Object[USER_ARGUMENTS_OFFSET + userArgumentLength];
        initialArguments[INDEX_KEYWORD_ARGUMENTS] = PKeyword.EMPTY_KEYWORDS;
        return initialArguments;
    }

    @ExplodeLoop
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

    public static MaterializedFrame getDeclarationFrame(Frame frame) {
        return (MaterializedFrame) frame.getArguments()[INDEX_DECLARATION_FRAME];
    }

    public static void setKeywordArguments(Object[] arguments, PKeyword[] keywordArguments) {
        arguments[INDEX_KEYWORD_ARGUMENTS] = keywordArguments;
    }

    public static PKeyword[] getKeywordArguments(Frame frame) {
        return (PKeyword[]) frame.getArguments()[INDEX_KEYWORD_ARGUMENTS];
    }

    public static void setArgument(Object[] arguments, int index, Object value) {
        arguments[USER_ARGUMENTS_OFFSET + index] = value;
    }

    public static Object getArgumentAt(Frame frame, int index) {
        return frame.getArguments()[USER_ARGUMENTS_OFFSET + index];
    }

    public static int getUserArgumentLength(VirtualFrame frame) {
        return frame.getArguments().length - USER_ARGUMENTS_OFFSET;
    }

    @ExplodeLoop
    public static Object[] insertSelf(Object[] arguments, PythonObject self) {
        final int userArgumentLength = arguments.length - USER_ARGUMENTS_OFFSET;
        Object[] results = create(userArgumentLength + 1);
        results[USER_ARGUMENTS_OFFSET] = self;

        for (int i = 0; i < userArgumentLength; i++) {
            results[USER_ARGUMENTS_OFFSET + 1 + i] = arguments[USER_ARGUMENTS_OFFSET + i];
        }

        return results;
    }

    @ExplodeLoop
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
    public static PKeyword getKeyword(Frame frame, String name) {
        PKeyword[] keywordArguments = getKeywordArguments(frame);

        for (int i = 0; i < keywordArguments.length; i++) {
            PKeyword keyword = keywordArguments[i];

            if (keyword.getName().equals(name)) {
                return keyword;
            }
        }

        return null;
    }

    public static VirtualFrame getVirtualFrameCargoArguments(Frame frame) {
        return CompilerDirectives.unsafeCast(frame.getArguments()[INDEX_GENERATOR_FRAME], VirtualFrame.class, true);
    }

    public static MaterializedFrame getGeneratorFrame(Frame frame) {
        return (MaterializedFrame) frame.getArguments()[INDEX_GENERATOR_FRAME];
    }

    public static GeneratorControlData getControlData(Frame frame) {
        MaterializedFrame generatorFrame = getGeneratorFrame(frame);
        return CompilerDirectives.unsafeCast(generatorFrame.getArguments()[INDEX_GENERATOR_FRAME], GeneratorControlData.class, true);
    }

    public static ParallelGeneratorArguments getParallelGeneratorArguments(Frame frame) {
        return CompilerDirectives.unsafeCast(frame.getArguments()[INDEX_GENERATOR_FRAME], ParallelGeneratorArguments.class, true);
    }

    public static void setGeneratorFrame(Object[] arguments, MaterializedFrame generatorFrame) {
        arguments[INDEX_GENERATOR_FRAME] = generatorFrame;
    }

    public static void setControlData(Object[] arguments, GeneratorControlData generatorArguments) {
        MaterializedFrame generatorFrame = (MaterializedFrame) arguments[INDEX_GENERATOR_FRAME];
        generatorFrame.getArguments()[INDEX_GENERATOR_FRAME] = generatorArguments;
    }

    public static void setVirtualFrameCargoArguments(Object[] arguments, Frame cargoFrame) {
        arguments[INDEX_GENERATOR_FRAME] = cargoFrame;
    }

    public static void setParallelGeneratorArguments(Object[] arguments, ParallelGeneratorArguments generatorArguments) {
        arguments[INDEX_GENERATOR_FRAME] = generatorArguments;
    }

    /**
     * TODO: (zwei) to be removed.
     */
    @SuppressWarnings("unused")
    public PArguments(MaterializedFrame declarationFrame, Object[] arguments, PKeyword[] keywords) {
        assert arguments != null;
    }

    public PArguments(MaterializedFrame declarationFrame, Object[] arguments) {
        this(declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
    }

    public final Object[] packAsObjectArray() {
        return new Object[]{this};
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

        public ParallelGeneratorArguments(BlockingQueue<Object> queue) {
            this.blockingQueue = queue;
            this.buffer = null;
            this.queue = null;
        }

        public ParallelGeneratorArguments(SingleProducerCircularBuffer buffer) {
            this.blockingQueue = null;
            this.buffer = buffer;
            this.queue = null;
        }

        public ParallelGeneratorArguments(Queue<Object> queue) {
            this.blockingQueue = null;
            this.buffer = null;
            this.queue = queue;
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
    }

}
