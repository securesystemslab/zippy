package edu.uci.python.profiler;

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

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.instrument.*;
import com.oracle.truffle.api.nodes.*;

/**
 * @author Gulfem
 */

public final class TimeProfilerInstrument extends Instrument {

    private final Node node;
    private long counter;
    private long time;
    private long startTime;
    private long excludedTime;
    private boolean isVisited;

    public TimeProfilerInstrument(Node node) {
        this.node = node;
        this.counter = 0;
        this.time = 0;
        this.startTime = 0;
        this.excludedTime = 0;
        isVisited = false;
    }

    @Override
    public void enter(Node astNode, VirtualFrame frame) {
        this.counter++;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void leave(Node astNode, VirtualFrame frame) {
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        this.time = this.time + elapsedTime;
    }

    @Override
    public void leave(Node astNode, VirtualFrame frame, boolean result) {
        leave(astNode, frame);
    }

    @Override
    public void leave(Node astNode, VirtualFrame frame, int result) {
        leave(astNode, frame);
    }

    @Override
    public void leave(Node astNode, VirtualFrame frame, double result) {
        leave(astNode, frame);
    }

    @Override
    public void leave(Node astNode, VirtualFrame frame, Object result) {
        leave(astNode, frame);
    }

    @Override
    public void leaveExceptional(Node astNode, VirtualFrame frame, Exception e) {
        leave(astNode, frame);
    }

    public Node getNode() {
        return node;
    }

    public long getCounter() {
        return counter;
    }

    public long getTime() {
        return time;
    }

    public void subtractSubFunctionTime(long subFunctiontime) {
        if (this.excludedTime == 0) {
            this.excludedTime = this.time;
        }

        this.excludedTime = this.excludedTime - subFunctiontime;
    }

    public long getExcludedTime() {
        assert (excludedTime >= 0);
        if (excludedTime == 0) {
            return time;
        }
        return excludedTime;
    }

    public void setVisited() {
        isVisited = true;
    }

    public boolean isVisited() {
        return isVisited;
    }
}
