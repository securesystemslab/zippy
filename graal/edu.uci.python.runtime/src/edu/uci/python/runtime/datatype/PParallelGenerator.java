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

import java.util.concurrent.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.exception.*;

public class PParallelGenerator extends PGenerator {

    private final PythonContext context;
    private ConcurrentLinkedQueue<Object> queue;
    private LinkedBlockingQueue<Object> blockingQueue;
    private SingleProducerCircularBuffer buffer;
    private static final boolean useCircularBuffer = true;
    @SuppressWarnings("unused") private static final boolean useBlockingQueue = true;

    public PParallelGenerator(String name, PythonContext context, CallTarget callTarget, FrameDescriptor frameDescriptor, MaterializedFrame declarationFrame, Object[] arguments,
                    int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
        super(name, callTarget, frameDescriptor, declarationFrame, arguments, numOfGeneratorBlockNode, numOfGeneratorForNode);
        this.context = context;
    }

    @Override
    public Object __next__() throws StopIterationException {
        if (useCircularBuffer) {
            return doWithCircularBuffer();
            // } else if (useBlockingQueue) {
            // return doWithLinkedBlockingQueue();
        } else {
            return doWithConcurrentLinkedQueue();
            // return doWithConcurrentLinkedQueueNewThread();
        }
    }

    private Object doWithConcurrentLinkedQueue() {
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<>();
            context.getExecutorService().execute(new Runnable() {

                public void run() {
                    try {
                        while (true) {
                            queue.offer(callTarget.call(null, arguments));
                        }
                    } catch (StopIterationException e) {
                        queue.offer(StopIterationException.INSTANCE);
                    }
                }

            });
        }

        Object result = null;
        while (result == null) {
            result = queue.poll();
        }

        if (result == StopIterationException.INSTANCE) {
            throw StopIterationException.INSTANCE;
        } else {
            return result;
        }
    }

    @SuppressWarnings("unused")
    private Object doWithConcurrentLinkedQueueNewThread() {
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<>();
            new Thread(new Runnable() {

                public void run() {
                    try {
                        while (true) {
                            queue.offer(callTarget.call(null, arguments));
                        }
                    } catch (StopIterationException e) {
                        queue.offer(StopIterationException.INSTANCE);
                    }
                }

            }).start();
        }

        Object result = null;
        while (result == null) {
            result = queue.poll();
        }

        if (result == StopIterationException.INSTANCE) {
            throw StopIterationException.INSTANCE;
        } else {
            return result;
        }
    }

    @SuppressWarnings("unused")
    private Object doWithLinkedBlockingQueue() {
        if (blockingQueue == null) {
            blockingQueue = new LinkedBlockingQueue<>();
            context.getExecutorService().execute(new Runnable() {

                public void run() {
                    try {
                        try {
                            while (true) {
                                blockingQueue.put(callTarget.call(null, arguments));
                            }
                        } catch (StopIterationException e) {
                            blockingQueue.put(StopIterationException.INSTANCE);
                        }
                    } catch (InterruptedException e) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        e.printStackTrace();
                    }
                }

            });
        }

        Object result = null;
        try {
            result = blockingQueue.take();
        } catch (InterruptedException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            e.printStackTrace();
        }

        if (result == StopIterationException.INSTANCE) {
            throw StopIterationException.INSTANCE;
        } else {
            return result;
        }
    }

    private Object doWithCircularBuffer() {
        if (buffer == null) {
            buffer = new SingleProducerCircularBuffer();
            context.getExecutorService().execute(new Runnable() {

                public void run() {
                    try {
                        while (true) {
                            buffer.put(callTarget.call(null, arguments));
                        }
                    } catch (StopIterationException e) {
                        buffer.put(StopIterationException.INSTANCE);
                    }
                }

            });
        }

        Object result = null;
        while (result == null) {
            result = buffer.take();
        }

        if (result == StopIterationException.INSTANCE) {
            throw StopIterationException.INSTANCE;
        } else {
            return result;
        }
    }

}
