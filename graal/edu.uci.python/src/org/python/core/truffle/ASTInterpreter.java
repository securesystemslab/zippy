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
package org.python.core.truffle;

import org.python.ast.datatypes.PArguments;
import org.python.ast.nodes.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

public class ASTInterpreter {

    public static boolean debug;

    @SuppressWarnings("hiding")
    public static void init(PyStringMap globals, boolean debug) {
        GlobalScope.create(globals);
        ASTInterpreter.debug = debug;
    }

    public static void interpret(RootNode rootNode, boolean log) {
        CallTarget module;

        ModuleNode root = (ModuleNode) rootNode;
        module = Truffle.getRuntime().createCallTarget(root, root.getFrameDescriptor());

        Arguments arguments = new PArguments();

        long start = System.nanoTime();
        module.call(null, arguments);
        long end = System.nanoTime();

        if (log) {
            // CheckStyle: stop system..print check
            System.out.printf("== iteration %d: %.3f ms\n", (0), (end - start) / 1000000.0);
            // CheckStyle: resume system..print check
        }
    }

    public static void trace(String message) {
        // CheckStyle: stop system..print check
        System.out.println(message);
        // CheckStyle: resume system..print check
    }

    public static void trace(Object entity) {
        // CheckStyle: stop system..print check
        System.out.println(entity);
        // CheckStyle: resume system..print check
    }

}
