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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.datatypes.*;

public class PArguments extends Arguments {

    public static final Object[] EMPTY_ARGUMENTS_ARRAY = new Object[0];
    private final MaterializedFrame declarationFrame;
    private Object self;
    private final Object[] arguments;
    private final PKeyword[] keywords;

    public PArguments(Object self, MaterializedFrame declarationFrame, Object[] arguments, PKeyword[] keywords) {
        this.self = self;
        this.declarationFrame = declarationFrame;
        this.arguments = arguments;
        this.keywords = keywords;
    }

    public PArguments(MaterializedFrame declarationFrame) {
        this(null, declarationFrame, EMPTY_ARGUMENTS_ARRAY, PKeyword.EMPTY_KEYWORDS);
    }

    public PArguments(Object self, MaterializedFrame declarationFrame, Object[] arguments) {
        this(self, declarationFrame, arguments, PKeyword.EMPTY_KEYWORDS);
    }

    public static PArguments get(Frame frame) {
        return frame.getArguments(PArguments.class);
    }

    public MaterializedFrame getMaterializedFrame() {
        assert self != null;
        return CompilerDirectives.unsafeCast(self, MaterializedFrame.class, true);
    }

    public Object getSelf() {
        return self;
    }

    public void setSelfUnsafe(Object value) {
        self = value;
    }

    public final Object[] getArgumentsArray() {
        return CompilerDirectives.unsafeCast(arguments, Object[].class, true);
    }

    public MaterializedFrame getDeclarationFrame() {
        return CompilerDirectives.unsafeFrameCast(declarationFrame);
    }

    public final Object getArgument(int index) {
        if (index >= arguments.length) {
            return PNone.NONE;
        }

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

        // throw new RuntimeException("Does not have a keyword:" + name);
    }

    public PKeyword[] getKeywords() {
        return keywords;
    }

    public int getLength() {
        return arguments.length;
    }

}
