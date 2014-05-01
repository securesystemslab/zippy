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
package edu.uci.python.nodes.call;

import java.io.*;

import org.python.core.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public class PythonCallUtil {

    public static final Object[] EMPTY_ARGUMENTS = new Object[0];

    protected static void logJythonRuntime(PyObject callee) {
        if (PythonOptions.TraceJythonRuntime) {
            PrintStream ps = System.out;
            ps.println("[ZipPy]: calling jython runtime function " + callee);
        }
    }

    /**
     * Pack primary into the evaluated arguments array if passPrimary is true.
     *
     */
    @ExplodeLoop
    protected static final Object[] executeArguments(VirtualFrame frame, boolean passPrimary, Object primary, PNode[] arguments) {
        final int length = passPrimary ? arguments.length + 1 : arguments.length;
        final Object[] evaluated = length == 0 ? EMPTY_ARGUMENTS : new Object[length];
        final int offset;

        if (passPrimary) {
            evaluated[0] = primary;
            offset = 1;
        } else {
            offset = 0;
        }

        for (int i = 0; i < arguments.length; i++) {
            evaluated[i + offset] = arguments[i].execute(frame);
        }

        return evaluated;
    }

    @ExplodeLoop
    public static final Object[] executeArguments(VirtualFrame frame, PNode[] arguments) {
        final int length = arguments.length;
        final Object[] evaluated = length == 0 ? EMPTY_ARGUMENTS : new Object[length];

        for (int i = 0; i < arguments.length; i++) {
            evaluated[i] = arguments[i].execute(frame);
        }

        return evaluated;
    }

    @ExplodeLoop
    protected static final PKeyword[] executeKeywordArguments(VirtualFrame frame, PNode[] arguments) {
        PKeyword[] evaluated = arguments.length == 0 ? PKeyword.EMPTY_KEYWORDS : new PKeyword[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            evaluated[i] = (PKeyword) arguments[i].execute(frame);
        }

        return evaluated;
    }

    protected static boolean isPrimaryBoxed(Object primary) {
        return primary instanceof PythonObject;
    }

    protected static boolean isPrimaryNone(Object primary, PythonCallNode node) {
        return node.primaryNode == EmptyNode.INSTANCE && primary == PNone.NONE;
    }

    protected static boolean haveToPassPrimary(Object primary, PythonCallNode node) {
        return !isPrimaryNone(primary, node) && !(primary instanceof PythonClass) && !(primary instanceof PythonModule) && !(primary instanceof PyObject);
    }

    @ExplodeLoop
    protected static String[] getKeywordNames(PythonCallNode node) {
        String[] keywordNames = new String[node.keywordNodes.length];

        for (int i = 0; i < node.keywordNodes.length; i++) {
            KeywordLiteralNode keywordLiteral = (KeywordLiteralNode) node.keywordNodes[i];
            keywordNames[i] = keywordLiteral.getName();
        }

        return keywordNames;
    }

}
