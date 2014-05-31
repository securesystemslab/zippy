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
package edu.uci.python.nodes.argument;

import static edu.uci.python.runtime.function.PArguments.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.function.*;

public class ArgumentsNode extends PNode {

    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    @Children private final PNode[] arguments;

    public ArgumentsNode(PNode[] arguments) {
        this.arguments = arguments;
    }

    public PNode[] getArguments() {
        return arguments;
    }

    public int length() {
        return arguments.length;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeArguments(frame);
    }

    @ExplodeLoop
    public final Object[] executeArguments(VirtualFrame frame) {
        final Object[] values = create(arguments.length);

        for (int i = 0; i < arguments.length; i++) {
            values[USER_ARGUMENTS_OFFSET + i] = arguments[i].execute(frame);
        }

        return values;
    }

    /**
     * Pack primary into the evaluated arguments array if passPrimary is true.
     */
    @ExplodeLoop
    public final Object[] executeArguments(VirtualFrame frame, boolean passPrimary, Object primary) {
        final int length = passPrimary ? arguments.length + 1 : arguments.length;
        final Object[] values = create(length);
        final int offset;

        if (passPrimary) {
            values[USER_ARGUMENTS_OFFSET] = primary;
            offset = 1;
        } else {
            offset = 0;
        }

        for (int i = 0; i < arguments.length; i++) {
            values[USER_ARGUMENTS_OFFSET + i + offset] = arguments[i].execute(frame);
        }

        return values;
    }

    @ExplodeLoop
    public final Object[] executeArgumentsForJython(VirtualFrame frame) {
        final int length = arguments.length;
        final Object[] values = length == 0 ? EMPTY_ARGUMENTS : new Object[length];

        for (int i = 0; i < arguments.length; i++) {
            values[i] = arguments[i].execute(frame);
        }

        return values;
    }

    @ExplodeLoop
    public final PKeyword[] executeKeywordArguments(VirtualFrame frame) {
        PKeyword[] keywords = arguments.length == 0 ? PKeyword.EMPTY_KEYWORDS : new PKeyword[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            keywords[i] = (PKeyword) arguments[i].execute(frame);
        }

        return keywords;
    }

}
