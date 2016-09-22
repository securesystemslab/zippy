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

    private int stararglen;
    private Object starargsValue;

    public ArgumentsNode(PNode[] arguments) {
        this.arguments = arguments;
        this.starargs = EmptyNode.create();
        this.stararglen = 0;
        this.starargsValue = null;
    }

    public ArgumentsNode(PNode[] arguments, PNode starargs) {
        this.arguments = arguments;
        this.starargs = (starargs == null) ? EmptyNode.create() : starargs;
        this.stararglen = 0;
        this.starargsValue = null;
    }

    public void executeStarargs(VirtualFrame frame) {
        if (!(starargs instanceof EmptyNode)) {
            this.starargsValue = starargs.execute(frame);
            if (starargsValue instanceof PTuple)
                this.stararglen = ((PTuple) this.starargsValue).len();
            if (starargsValue instanceof PKeyword[])
                this.stararglen = ((PKeyword[]) this.starargsValue).length;
        }
    }

    public PNode[] getArguments() {
        return arguments;
    }

    public String[] getArgKeywordNames() {
        ArrayList<String> names = new ArrayList<>();
        for (PNode arg : arguments)
            names.add(((KeywordLiteralNode) arg).getName());
        if (stararglen > 0)
            for (PKeyword arg : ((PKeyword[]) starargsValue))
                names.add(arg.getName());
        return names.toArray(new String[length()]);
    }

    public int length() {
        return arguments.length + stararglen;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeArguments(frame);
    }

    public final Object[] executeArguments(VirtualFrame frame) {

        final Object[] values = create(length());
        frame.materialize();

        for (int i = 0; i < arguments.length; i++) {
            values[USER_ARGUMENTS_OFFSET + i] = arguments[i].execute(frame);
        }

        for (int i = 0; i < stararglen; i++) {
            values[USER_ARGUMENTS_OFFSET + arguments.length + i] = ((PTuple) starargsValue).getArray()[i];
        }

        return values;
    }

    /**
     * Pack primary into the evaluated arguments array if passPrimary is true.
     */
    public final Object[] executeArguments(VirtualFrame frame, boolean passPrimary, Object primary) {
        final int length = passPrimary ? length() + 1 : length();
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

        for (int i = 0; i < stararglen; i++) {
            values[USER_ARGUMENTS_OFFSET + offset + arguments.length + i] = ((PTuple) starargsValue).getArray()[i];
        }

        return values;
    }

    public final Object[] executeArgumentsForJython(VirtualFrame frame) {
        final int length = length();
        final Object[] values = length == 0 ? EMPTY_ARGUMENTS : new Object[length];
        frame.materialize();
        for (int i = 0; i < arguments.length; i++) {
            values[i] = arguments[i].execute(frame);
        }

        for (int i = 0; i < stararglen; i++) {
            values[USER_ARGUMENTS_OFFSET + arguments.length + i] = ((PTuple) starargsValue).getArray()[i];
        }

        return values;
    }

    public final PKeyword[] executeKeywordArguments(VirtualFrame frame) {
        PKeyword[] keywords = length() == 0 ? PKeyword.EMPTY_KEYWORDS : new PKeyword[length()];
        frame.materialize();
        for (int i = 0; i < arguments.length; i++) {
            keywords[i] = (PKeyword) arguments[i].execute(frame);
        }

        for (int i = 0; i < stararglen; i++) {
            keywords[arguments.length + i] = ((PKeyword[]) starargsValue)[i];
        }

        return keywords;
    }

    @Override
    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitArgumentsNode(this);
    }

}
