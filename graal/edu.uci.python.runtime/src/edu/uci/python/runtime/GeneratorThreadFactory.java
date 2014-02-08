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
package edu.uci.python.runtime;

import static com.higherfrequencytrading.affinity.AffinityStrategies.*;

import java.io.*;
import java.util.concurrent.*;

import com.higherfrequencytrading.affinity.*;

public class GeneratorThreadFactory implements ThreadFactory {

    private final String name;
    private final boolean daemon = true;
    private final AffinityStrategy[] strategies;
    private AffinityLock lastAffinityLock = null;
    private int id = 1;

    public GeneratorThreadFactory() {
        this.name = "generator";
        this.strategies = new AffinityStrategy[]{SAME_CORE, SAME_SOCKET, ANY};
        this.lastAffinityLock = AffinityLock.acquireLock();
    }

    @Override
    public synchronized Thread newThread(final Runnable r) {
        String name2 = id <= 1 ? name : (name + '-' + id);
        id++;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AffinityLock al = lastAffinityLock == null ? AffinityLock.acquireLock() : lastAffinityLock.acquireLock(strategies);
                try {
                    if (al.cpuId() >= 0) {
                        lastAffinityLock = al;
                    }
                    al.bind();
                    r.run();
                } finally {
                    al.release();
                }
            }
        }, name2);
        t.setDaemon(daemon);

        PrintStream out = System.out;
        out.println("\nThe assignment of CPUs is\n" + AffinityLock.dumpLocks());

        return t;
    }

}
