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
package edu.uci.python.nodes.profiler;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem
 */

public class ProfilerResultPrinter {

    static List<PNode> nodesEmptySourceSections = new ArrayList<>();

    public static void addNodeEmptySourceSection(PNode node) {
        nodesEmptySourceSections.add(node);
    }

    public static void printProfilerInstrumenterResults() {
        Map<PythonWrapperNode, ProfilerInstrument> sorted = sortByValue(PythonNodeProber.getWrapperToInstruments());

        /**
         * 50 is the length of the text by default padding left padding is added, so space is added
         * to the beginning of the string, minus sign adds padding to the right
         */

        PrintStream out = System.out;
        out.format("%-50s", "Node");
        out.format("%-20s", "Counter");
        out.format("%-9s", "Line");
        out.format("%-11s", "Column");
        out.format("%-11s", "Length");
        out.println();
        out.println("=============                                     ===============     ====     ======     ======");

        @SuppressWarnings("rawtypes")
        Iterator it = sorted.entrySet().iterator();
        while (it.hasNext()) {
            @SuppressWarnings("unchecked")
            Entry<PythonWrapperNode, ProfilerInstrument> entry = (Entry<PythonWrapperNode, ProfilerInstrument>) it.next();
            PythonWrapperNode wrapper = entry.getKey();
            ProfilerInstrument instrument = entry.getValue();

            Node child = wrapper.getChild();
            out.format("%-50s", child.getClass().getSimpleName());
            out.format("%15s", instrument.getCounter());
            out.format("%9s", child.getSourceSection().getStartLine());
            out.format("%11s", child.getSourceSection().getStartColumn());
            out.format("%11s", child.getSourceSection().getCharLength());
            out.println();

        }
    }

    private static Map<PythonWrapperNode, ProfilerInstrument> sortByValue(Map<PythonWrapperNode, ProfilerInstrument> map) {
        List<Map.Entry<PythonWrapperNode, ProfilerInstrument>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<PythonWrapperNode, ProfilerInstrument>>() {

            public int compare(Map.Entry<PythonWrapperNode, ProfilerInstrument> m1, Map.Entry<PythonWrapperNode, ProfilerInstrument> m2) {
                return Long.compare(m2.getValue().getCounter(), m1.getValue().getCounter());
            }
        });

        Map<PythonWrapperNode, ProfilerInstrument> result = new LinkedHashMap<>();
        for (Map.Entry<PythonWrapperNode, ProfilerInstrument> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * "Function Name"(13 char) + "17 space" + + "5 space" + "Number of Calls"(15 char).
     */

    public static void printFunctionInvocationProfilerResults() {
        List<ProfilerNode> profiledNodes = ProfilerNode.getProfiledNodes();
        if (PythonOptions.SortProfilerResults) {
            sortTheProfilerList(profiledNodes);
        }

        // CheckStyle: stop system..print check
        PrintStream out = System.out;
        out.format("%-30s", "Function Name");
        out.format("%20s", "Number of Calls");
        out.println();
        out.println("=============                      ===============");
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
            // CheckStyle: stop system..print check
            PrintStream out = System.out;
            out.println();
            // CheckStyle: resume system..print check
        } else if (profilerResult < 0) {
            throw new RuntimeException("Profiler result can't be less than 0: " + profilerResult + " for " + profiledRootNode);
        }
    }

    private static void printInvocationCount(String functionName, long invocationCount) {
        // CheckStyle: stop system..print check
        System.out.format("%-30s", functionName);
        System.out.format("%20s", invocationCount);
        // CheckStyle: resume system..print check
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

    public static void printNodesEmptySourceSections() {
        // CheckStyle: stop system..print check
        for (PNode node : nodesEmptySourceSections) {
            System.out.println(node);
        }
        // CheckStyle: resume system..print check
    }
}
