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
package edu.uci.python.runtime.datatype;

import java.io.*;
import java.util.concurrent.atomic.*;

public final class SingleProducerCircularBuffer {

    private static final int INDEX_MASK = 0B1111;
    private final Object[] buffer;
    private AtomicLong readCursor;
    private AtomicLong writeCursor;

    public SingleProducerCircularBuffer() {
        this.buffer = new Object[INDEX_MASK + 1];
        this.readCursor = new PaddedAtomicLong();
        this.writeCursor = new PaddedAtomicLong();
    }

    /**
     * Writes to buffer. Advances writeCursor.
     */
    public void put(Object value) {
        final long currentWriteCursor = writeCursor.get();
        int count = 0;
        while ((currentWriteCursor - readCursor.get()) > INDEX_MASK) {
            // spin
            count++;
            if (count == 1000) {
                count = 0;
                Thread.yield();
            }
        }

        buffer[getIndex(currentWriteCursor)] = value;
        // log(value, currentWriteCursor);
        writeCursor.lazySet(currentWriteCursor + 1);
    }

    private int getIndex(long cursor) {
        int index = (int) (cursor & INDEX_MASK);
        assert index < buffer.length;
        return index;
    }

    /**
     * Reads from buffer. Advances readCursor.
     */
    public Object take() {
        final long currentReadCursor = readCursor.get();
        int count = 0;
        while (currentReadCursor >= writeCursor.get()) {
            // spin
            count++;
            if (count == 1000) {
                count = 0;
                Thread.yield();
            }
        }

        Object result = buffer[getIndex(currentReadCursor)];
        // log(result, readCursor.get());
        readCursor.lazySet(currentReadCursor + 1);
        return result;
    }

    public static void log(Object value, long cursor) {
        PrintStream out = System.out;
        long id = Thread.currentThread().getId();

        if (id == 1) {
            out.println("BUFFER Thread " + Thread.currentThread().getId() + " read " + value + " at index " + cursor);
        } else {
            out.println("BUFFER Thread " + Thread.currentThread().getId() + " write " + value + " at index " + cursor);
        }
    }

    public static final class PaddedAtomicLong extends AtomicLong {

        private static final long serialVersionUID = 4254067538639965876L;

        public PaddedAtomicLong() {
        }

        // Checkstyle: stop
        public volatile long p1, p2, p3, p4, p5, p6 = 7;
        // Checkstyle: resume
    }

}
