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

import java.util.ArrayList;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.ast.VisitorIF;
import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.KeywordLiteralNode;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.sequence.PTuple;

public class ArgumentsNode extends PNode {

    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    @Children private final PNode[] arguments;
    @Child private PNode starargs;

    public ArgumentsNode(PNode[] arguments) {
        this.arguments = arguments;
        this.starargs = EmptyNode.create();
    }

    public ArgumentsNode(PNode[] arguments, PNode starargs) {
        this.arguments = arguments;
        this.starargs = (starargs == null) ? EmptyNode.create() : starargs;
    }

    public Object[] executeStarargs(VirtualFrame frame) {
        final Object starargsVal = starargs.execute(frame);
        if (starargsVal instanceof PTuple) {
            return ((PTuple) starargsVal).getArray();
        } else {
            return new Object[0];
        }
    }

    public PKeyword[] executeKeywordStarargs(VirtualFrame frame) {
        final Object starargsVal = starargs.execute(frame);
        if (starargsVal instanceof PKeyword[]) {
            return ((PKeyword[]) starargsVal);
        } else {
            return new PKeyword[0];
        }
    }

    public PNode[] getArguments() {
        return arguments;
    }

    public String[] getArgKeywordNames(PKeyword[] keystarags) {
        ArrayList<String> names = new ArrayList<>();
        for (PNode arg : arguments)
            names.add(((KeywordLiteralNode) arg).getName());
        for (PKeyword arg : keystarags)
            names.add(arg.getName());
        return names.toArray(new String[length() + keystarags.length]);
    }

    public int length() {
        return arguments.length;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeArguments(frame, executeStarargs(frame));
    }

    public final Object[] executeArguments(VirtualFrame frame, Object[] starValues) {
        final int length = length() + starValues.length;
        final Object[] values = create(length);
        frame.materialize();

        for (int i = 0; i < arguments.length; i++) {
            values[USER_ARGUMENTS_OFFSET + i] = arguments[i].execute(frame);
        }

        for (int i = 0; i < starValues.length; i++) {
            values[USER_ARGUMENTS_OFFSET + arguments.length + i] = starValues[i];
        }

        return values;
    }

    /**
     * Pack primary into the evaluated arguments array if passPrimary is true.
     */
    public final Object[] executeArguments(VirtualFrame frame, boolean passPrimary, Object primary, Object[] starValues) {
        final int length = (passPrimary ? length() + 1 : length()) + starValues.length;
        final Object[] values = create(length);
        final int offset;
        frame.materialize();
        if (passPrimary) {
            values[USER_ARGUMENTS_OFFSET] = primary;
            offset = 1;
        } else {
            offset = 0;
        }

        for (int i = 0; i < arguments.length; i++) {
            values[USER_ARGUMENTS_OFFSET + offset + i] = arguments[i].execute(frame);
        }

        for (int i = 0; i < starValues.length; i++) {
            values[USER_ARGUMENTS_OFFSET + offset + arguments.length + i] = starValues[i];
        }

        return values;
    }

    public final Object[] executeArgumentsForJython(VirtualFrame frame, Object[] starValues) {
        final int length = length() + starValues.length;
        final Object[] values = length == 0 ? EMPTY_ARGUMENTS : new Object[length];
        frame.materialize();
        for (int i = 0; i < arguments.length; i++) {
            values[i] = arguments[i].execute(frame);
        }

        for (int i = 0; i < starValues.length; i++) {
            values[USER_ARGUMENTS_OFFSET + arguments.length + i] = starValues[i];
        }

        return values;
    }

    public final Object[] executeArgumentsForign(VirtualFrame frame) {
        final Object[] values = new Object[arguments.length];
        frame.materialize();
        for (int i = 0; i < arguments.length; i++) {
            values[i] = arguments[i].execute(frame);
        }
        return values;
    }

    private static PKeyword[] reshape(PKeyword[] keys, int reshape) {
        PKeyword[] keywords = new PKeyword[keys.length - reshape];
        int i = 0;
        for (PKeyword k : keys) {
            if (k != null) {
                keywords[i++] = k;
            }
        }

        return keywords;
    }

    public final PKeyword[] executeKeywordArguments(VirtualFrame frame, PKeyword[] starValues) {
        final int length = length() + starValues.length;
        PKeyword[] keywords = length == 0 ? PKeyword.EMPTY_KEYWORDS : new PKeyword[length];
        frame.materialize();
        int reshape = 0;
        for (int i = 0; i < arguments.length; i++) {
            Object o = arguments[i].execute(frame);
            if (o instanceof PKeyword) {
                keywords[i] = (PKeyword) o;
            } else {
                reshape++;
            }
        }

        for (int i = 0; i < starValues.length; i++) {
            keywords[arguments.length + i] = starValues[i];
        }

        if (reshape > 0)
            return reshape(keywords, reshape);

        return keywords;
    }

    @Override
    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitArgumentsNode(this);
    }

}
