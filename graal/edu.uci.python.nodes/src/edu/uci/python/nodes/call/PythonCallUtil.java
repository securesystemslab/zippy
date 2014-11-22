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

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public class PythonCallUtil {

    protected static void logJythonRuntime(PyObject callee) {
        if (PythonOptions.TraceJythonRuntime) {
            PrintStream ps = System.out;
            ps.println("[ZipPy]: calling jython runtime function " + callee);
        }
    }

    protected static boolean isPrimaryBoxed(Object primary) {
        return primary instanceof PythonObject;
    }

    protected static boolean isPrimaryNone(Object primary, PythonCallNode node) {
        return EmptyNode.isEmpty(node.primaryNode) && primary == PNone.NONE;
    }

    protected static boolean isConstructorCall(Object primary, PythonCallable callee) {
        return PythonCallUtil.isPrimaryBoxed(primary) && callee instanceof PythonClass && !(callee instanceof PythonBuiltinClass);
    }

    protected static boolean isClassMethodCall(Object primary, PythonCallable callee) {
        return PythonCallUtil.isPrimaryBoxed(primary) && callee.isClassMethod() && !(primary instanceof PythonModule);
    }

    protected static boolean isStaticMethodCall(Object primary, PythonCallable callee) {
        return PythonCallUtil.isPrimaryBoxed(primary) && callee.isStaticMethod() && !(primary instanceof PythonModule);
    }

    protected static boolean haveToPassPrimary(Object primary, PythonCallable callee, PythonCallNode node) {
        return !isPrimaryNone(primary, node) && //
                        !(primary instanceof PythonClass) && //
                        !(primary instanceof PythonModule) && //
                        !isStaticMethodCall(primary, callee) && //
                        !(primary instanceof PyObject) || //
                        isConstructorCall(primary, callee) || //
                        isClassMethodCall(primary, callee);
    }

    @ExplodeLoop
    protected static String[] getKeywordNames(PythonCallNode node) {
        String[] keywordNames = new String[node.keywordsNode.length()];

        for (int i = 0; i < node.keywordsNode.length(); i++) {
            KeywordLiteralNode keywordLiteral = (KeywordLiteralNode) node.keywordsNode.getArguments()[i];
            keywordNames[i] = keywordLiteral.getName();
        }

        return keywordNames;
    }

    protected static PythonCallable resolveSpecialMethod(Object operand, String specialMethodId) {
        PythonObject primary;
        try {
            primary = PythonTypesGen.PYTHONTYPES.expectPythonObject(operand);
        } catch (UnexpectedResultException e1) {
            return null;
        }

        PythonCallable callee;

        try {
            callee = PythonTypesGen.PYTHONTYPES.expectPythonCallable(primary.getAttribute(specialMethodId));
        } catch (UnexpectedResultException e) {
            return null;
        }

        return callee;
    }

}
