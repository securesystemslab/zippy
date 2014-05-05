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

import java.util.*;

import org.python.core.*;
import org.python.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.profiler.*;
import edu.uci.python.parser.*;
import edu.uci.python.profiler.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

public class CustomConsole extends JLineConsole {

    @Override
    public void execfile(java.io.InputStream s, String name) {
        PythonParser parser = new PythonParserImpl();
        PythonContext context = new PythonContext(new PythonOptions(), new PythonDefaultBuiltinsLookup(), parser);

        try {
            Source source = context.getSourceManager().get(name);
            execfile(context, source);
        } finally {
            /**
             * look at shutdown
             */
            // context.shutdown();
        }
    }

    public PythonParseResult execfile(PythonContext context, Source source) {
        setSystemState();

        PythonModule module = context.createMainModule(source.getPath());
        PythonParseResult result = context.getParser().parse(context, module, source);

        if (PythonOptions.PrintAST) {
            printBanner("Before Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        ASTInterpreter.interpret(result);

        if (PythonOptions.PrintAST) {
            printBanner("After Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        if (PythonOptions.ProfileGeneratorCalls) {
            context.printGeneratorProfilingInfo();
        }

        if (PythonOptions.ProfileNodes) {
            printProfilerResults();
        }

        if (PythonOptions.ProfileFunctionCalls) {
            printBanner("Function Invocation Count Results");
            Profiler.getInstance().printProfilerResults();
        }

        if (PythonOptions.ProfileLists) {
            printBanner("List Count Results");
            PList.printProfilerResults();
        }

        Py.flushLine();
        return result;
    }

    public void parseFile(PythonContext context, Source source) {
        PythonModule module = context.createMainModule(source.getPath());
        PythonParseResult result = context.getParser().parse(context, module, source);

        if (PythonOptions.PrintAST) {
            printBanner("After Parsing");
            result.printAST();
        }
    }

    public static void printBanner(String phase) {
        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check
    }

    // "FUNCTION NAME"(13 char) + "10 space" + "INVOCATION COUNTS"(27 char) + "10 space" +
// "CALL SITES"(11 char)
    public static void printProfilerResults() {
        List<ProfilerNode> profiledNodes = ProfilerNode.getProfiledNodes();
        if (PythonOptions.SortProfilerCounts) {
            sortTheProfilerList(profiledNodes);
        }

        // CheckStyle: stop system..print check
        System.out.format("%-23s", "FUNCTION NAME");
        System.out.format("%-27s", "INVOCATION COUNTS");
        if (PythonOptions.ProfileCallSites) {
            System.out.format("%-11s%n", "CALL SITES");
            System.out.println("=============          =================          ==========");
        } else {
            System.out.println();
            System.out.println("=============          =================");
        }
        // CheckStyle: resume system..print check

        for (int i = 0; i < profiledNodes.size(); i++) {
            ProfilerNode profilerNode = profiledNodes.get(i);
            Node profiledNode = profilerNode.getProfiledNode();
            if (profiledNode instanceof RootNode) {
                printInvocationCountOfRootNode((RootNode) profiledNode, profilerNode.getProfilerResult());
            }
        }
    }

    private static void printInvocationCountOfRootNode(RootNode profiledRootNode, long profilerResult) {
        if (profilerResult > 0) {
            String functionName = null;

            if (profiledRootNode instanceof FunctionRootNode) {
                FunctionRootNode functionRootNode = (FunctionRootNode) profiledRootNode;
                functionName = functionRootNode.getFunctionName();
            } else if (profiledRootNode instanceof BuiltinFunctionRootNode) {
                /**
                 * For better performance and specialization in builtins, we do create a different
                 * PBuiltinFunction, and BuiltinFrunctionRootNode, and CallTarget for the same
                 * builtin function. For ex, abs might appear multiple times in this list with
                 * different invocation counts.
                 */
                BuiltinFunctionRootNode builtinFunctionRootNode = (BuiltinFunctionRootNode) profiledRootNode;
                functionName = builtinFunctionRootNode.getFunctionName();
            } else {
                throw new RuntimeException("Unknown root node type " + profiledRootNode + " " + profiledRootNode.getClass());
            }

            printInvocationCount(functionName, profilerResult);

            if (PythonOptions.ProfileCallSites) {
                printCallSitesOfInvocation(profiledRootNode);
            }

            System.out.println();
        } else if (profilerResult < 0) {
            throw new RuntimeException("Profiler result can't be less than 0: " + profilerResult + " for " + profiledRootNode);
        }
    }

    private static void printInvocationCount(String functionName, long invocationCount) {
        // CheckStyle: stop system..print check
        System.out.format("%-23s", functionName);
        System.out.format("%17s", invocationCount);
        // CheckStyle: resume system..print check
    }

    private static void printCallSitesOfInvocation(RootNode calleeRootNode) {
        List<CallSiteProfilerNode> profiledCallSites = CallSiteProfilerNode.getProfiledCallSites();
        int index = 0;

        for (int i = 0; i < profiledCallSites.size(); i++) {
            CallSiteProfilerNode profiledCallSite = profiledCallSites.get(i);

            if (profiledCallSite.getProfilerResult() > 0) {
                if (profiledCallSite.doesCallRootNode(calleeRootNode)) {
                    if (index == 0) {
                        System.out.format("%" + 10 + "s", "");
                    } else {
                        System.out.format("%" + 50 + "s", "");
                    }
                    /**
                     * TODO getRootNode is not visible
                     */
                    System.out.println("[" + getRootName(profiledCallSite.getRootNode()) + " -> " + getRootName(profiledCallSite.getRootNode()) + "] (" +
                                    profiledCallSite.getInvocationCounterOfRootNode(calleeRootNode) + ")");
                    index++;
                }
            }
        }
    }

    private static String getRootName(RootNode rootNode) {
        String rootName = null;

        if (rootNode instanceof FunctionRootNode) {
            FunctionRootNode functionRootNode = (FunctionRootNode) rootNode;
            rootName = functionRootNode.getFunctionName();
        } else if (rootNode instanceof BuiltinFunctionRootNode) {
            BuiltinFunctionRootNode builtinFunctionRootNode = (BuiltinFunctionRootNode) rootNode;
            rootName = builtinFunctionRootNode.getFunctionName();
        } else if (rootNode instanceof ModuleNode) {
            rootName = "module";
        } else {
            throw new RuntimeException("Unknown root node type " + rootNode + " " + rootNode.getClass());
        }

        return rootName;
    }

    private static void sortTheProfilerList(List<ProfilerNode> list) {
        if (!list.isEmpty()) {
            Collections.sort(list, new Comparator<ProfilerNode>() {
                @Override
                public int compare(ProfilerNode p1, ProfilerNode p2) {
                    // Descending order
                    return Long.compare(p2.getProfilerResult(), p1.getProfilerResult());
                }
            });
        }
    }

}
