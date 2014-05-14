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
package edu.uci.python.shell;

import com.oracle.truffle.api.*;

import edu.uci.python.nodes.*;
import edu.uci.python.parser.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;

public class ASTInterpreter {

    public static void interpret(PythonParseResult result) {
        ModuleNode root = (ModuleNode) result.getModuleRoot();
        RootCallTarget module = Truffle.getRuntime().createCallTarget(root);
        // Added here because createCallTarget adopts all children, i.e. adds all parent
        // relationships. In order to be able create wrapper nodes, and replace nodes with wrapper
        // nodes, we need parent relationship

        if (PythonOptions.AddProfilingInstrumentation) {
            ProfilerTranslator pt = new ProfilerTranslator(result.getContext());
            pt.translate(result, root);

            if (PythonOptions.PrintAST) {
                System.out.println("============= " + "After Adding Wrapper Nodes" + " ============= ");
                result.printAST();
            }
        }

	   module.call(PArguments.EMPTY_ARGUMENTS_ARRAY);
    }

}
