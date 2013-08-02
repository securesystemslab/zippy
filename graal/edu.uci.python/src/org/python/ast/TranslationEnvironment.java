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
package org.python.ast;

import java.util.*;

import org.python.antlr.*;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;

import com.oracle.truffle.api.frame.*;

public class TranslationEnvironment {

    private final mod module;

    private Map<PythonTree, FrameDescriptor> ptreeToFrameDescriptor = new HashMap<>();

    private Map<PythonTree, FrameSlot> nameToFrameSlot = new HashMap<>();

    private Map<comprehension, comprehension> generatorToInnerLoop = new HashMap<>();

    private Map<comprehension, expr> generatorToLoopBody = new HashMap<>();

    public TranslationEnvironment(mod module) {
        this.module = module;
    }

    protected mod getModule() {
        return module;
    }

    protected void setFrameDescriptor(PythonTree scopeEntity, FrameDescriptor descriptor) {
        ptreeToFrameDescriptor.put(scopeEntity, descriptor);
    }

    protected FrameDescriptor getFrameDescriptor(PythonTree scopeEntity) {
        return ptreeToFrameDescriptor.get(scopeEntity);
    }

    protected void setFrameSlot(PythonTree symbol, FrameSlot slot) {
        nameToFrameSlot.put(symbol, slot);
    }

    protected FrameSlot getFrameSlot(PythonTree symbol) {
        return nameToFrameSlot.get(symbol);
    }

    protected void setInnerLoop(comprehension outer, comprehension inner) {
        generatorToInnerLoop.put(outer, inner);
    }

    protected comprehension getInnerLoop(comprehension outer) {
        return generatorToInnerLoop.get(outer);
    }

    protected void setLoopBody(comprehension outer, expr body) {
        generatorToLoopBody.put(outer, body);
    }

    protected expr getLoopBody(comprehension outer) {
        return generatorToLoopBody.get(outer);
    }

}
