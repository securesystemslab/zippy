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
package edu.uci.python.nodes.translation;

import java.util.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;

public class PythonParseResult {

    private ModuleNode module;

    private final Map<String, RootNode> functions = new HashMap<>();

    protected void setModule(ModuleNode module) {
        this.module = module;
    }

    protected void addParsedFunction(String name, FunctionRootNode function) {
        functions.put(name, function);
    }

    public ModuleNode getModuleRoot() {
        return module;
    }

    public void printAST() {
        printSeparationLine("module");
        NodeUtil.printCompactTree(System.out, module);

        for (String functionName : functions.keySet()) {
            printSeparationLine(functionName);
            RootNode root = functions.get(functionName);
            NodeUtil.printCompactTree(System.out, root);
        }
    }

    private static void printSeparationLine(String id) {
        // CheckStyle: stop system..print check
        System.out.println(" ------------- " + id + " ------------- ");
        // CheckStyle: resume system..print check
    }

    public void visualizeToNetwork() {
        new GraphPrintVisitor().beginGraph("module").visit(module).printToNetwork();

        for (String functionName : functions.keySet()) {
            RootNode root = functions.get(functionName);
            new GraphPrintVisitor().beginGraph(functionName).visit(root).printToNetwork();
        }
    }
}
