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

import java.util.concurrent.atomic.*;

public class SingleProducerCircularBuffer {

    private final Object[] buffer;
    private int readIndex;
    private AtomicInteger writeIndex;
    private AtomicBoolean isWriteBehindRead;

    public SingleProducerCircularBuffer() {
        this.buffer = new Object[10];
        this.writeIndex = new AtomicInteger();
        this.isWriteBehindRead = new AtomicBoolean();
    }

    public void put(Object value) {
        int localWriteIndex = writeIndex.get();
        buffer[localWriteIndex] = value;
        advanceWriteIndex(localWriteIndex);
    }

    private void advanceWriteIndex(int currentWriteIndex) {
        if (currentWriteIndex == buffer.length - 1) {
            writeIndex.set(0);
            isWriteBehindRead.set(true);
        } else {
            writeIndex.incrementAndGet();
        }
    }

    public Object take() {
        Object result;

        if (!isWriteBehindRead.get()) {
            while (readIndex >= writeIndex.get()) {
                // Wait
            }

            result = buffer[readIndex];
        } else {
            result = buffer[readIndex];
        }

        advanceReadIndex();
        return result;
    }

    private void advanceReadIndex() {
        if (readIndex == buffer.length - 1) {
            readIndex = 0;
            isWriteBehindRead.set(false);
        } else {
            readIndex++;
        }
    }

}
