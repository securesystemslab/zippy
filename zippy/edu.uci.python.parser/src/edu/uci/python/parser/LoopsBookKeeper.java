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
package edu.uci.python.parser;

import java.util.*;

import org.python.antlr.base.*;

public class LoopsBookKeeper {

    private final Stack<stmt> loops;
    private final Map<stmt, LoopInfo> infos;

    public LoopsBookKeeper() {
        loops = new Stack<>();
        infos = new HashMap<>();
    }

    public void beginLoop(stmt loop) {
        loops.push(loop);
        infos.put(loop, new LoopInfo(loop));
    }

    public LoopInfo endLoop() {
        stmt loop = loops.pop();
        return infos.remove(loop);
    }

    public void addBreak() {
        stmt currentLoop = loops.peek();
        LoopInfo info = infos.get(currentLoop);
        info.addBreak();
    }

    public void addContinue() {
        stmt currentLoop = loops.peek();
        LoopInfo info = infos.get(currentLoop);
        info.addContinue();
    }

    public void hasBreak() {
        stmt currentLoop = loops.peek();
        LoopInfo info = infos.get(currentLoop);
        info.hasBreak();
    }

    public void hasContinue() {
        stmt currentLoop = loops.peek();
        LoopInfo info = infos.get(currentLoop);
        info.hasContinue();
    }
}
