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
import edu.uci.python.nodes.control.*;
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

    public static void printNodeProfilerResults() {
        Map<PythonWrapperNode, ProfilerInstrument> nodes;
        if (PythonOptions.SortProfilerResults) {
            nodes = sortByValue(PythonNodeProber.getWrapperToInstruments());
        } else {
            nodes = PythonNodeProber.getWrapperToInstruments();
        }

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

        Iterator<Map.Entry<PythonWrapperNode, ProfilerInstrument>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Entry<PythonWrapperNode, ProfilerInstrument> entry = it.next();
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

    public static void printCallProfilerResults() {
        Map<PythonWrapperNode, ProfilerInstrument> calls;
        if (PythonOptions.SortProfilerResults) {
            calls = sortByValue(PythonNodeProber.getCallWrapperToInstruments());
        } else {
            calls = PythonNodeProber.getCallWrapperToInstruments();
        }

        /**
         * 50 is the length of the text by default padding left padding is added, so space is added
         * to the beginning of the string, minus sign adds padding to the right
         */

        PrintStream out = System.out;
        out.format("%-50s", "Function Name");
        out.format("%-20s", "Number of Calls");
        out.format("%-9s", "Line");
        out.format("%-11s", "Column");
        out.format("%-11s", "Length");
        out.println();
        out.println("===============                                    ===============     ====     ======     ======");

        Iterator<Map.Entry<PythonWrapperNode, ProfilerInstrument>> it = calls.entrySet().iterator();
        while (it.hasNext()) {
            Entry<PythonWrapperNode, ProfilerInstrument> entry = it.next();
            PythonWrapperNode wrapper = entry.getKey();
            ProfilerInstrument instrument = entry.getValue();
            Node child = wrapper.getChild();
            FunctionRootNode rootNode = (FunctionRootNode) (wrapper.getParent());
            out.format("%-50s", rootNode.getFunctionName());
            out.format("%15s", instrument.getCounter());
            out.format("%9s", child.getSourceSection().getStartLine());
            out.format("%11s", child.getSourceSection().getStartColumn());
            out.format("%11s", child.getSourceSection().getCharLength());
            out.println();
        }
    }

    public static void printIfProfilerResults() {
        Map<PythonWrapperNode, ProfilerInstrument> conditions;
        if (PythonOptions.SortProfilerResults) {
            conditions = sortByValue(PythonNodeProber.getConditionWrapperToInstruments());
        } else {
            conditions = PythonNodeProber.getConditionWrapperToInstruments();
        }

        Map<PythonWrapperNode, ProfilerInstrument> thens = PythonNodeProber.getThenWrapperToInstruments();
        Map<PythonWrapperNode, ProfilerInstrument> elses = PythonNodeProber.getElseWrapperToInstruments();

        PrintStream out = System.out;
        out.format("%-20s", "If Counter");
        out.format("%15s", "Then Counter");
        out.format("%20s", "Else Counter");
        out.format("%9s", "Line");
        out.format("%11s", "Column");
        out.format("%11s", "Length");
        out.println();
        out.println("===========            ============        ============     ====     ======     ======");

        Iterator<Map.Entry<PythonWrapperNode, ProfilerInstrument>> it = conditions.entrySet().iterator();
        while (it.hasNext()) {
            Entry<PythonWrapperNode, ProfilerInstrument> entry = it.next();
            PythonWrapperNode conditionWrapper = entry.getKey();
            ProfilerInstrument conditionInstrument = entry.getValue();
            IfNode ifNode = (IfNode) conditionWrapper.getParent().getParent();
            PNode thenNode = ifNode.getThen();
            PNode elseNode = ifNode.getElse();
            ProfilerInstrument thenInstrument = thens.get(thenNode);

            if (conditionInstrument.getCounter() > 0) {
                out.format("%11s", conditionInstrument.getCounter());
                out.format("%24s", thenInstrument.getCounter());

                if (!(ifNode.getElse() instanceof EmptyNode)) {
                    ProfilerInstrument elseInstrument = elses.get(elseNode);
                    out.format("%20s", elseInstrument.getCounter());
                } else {
                    out.format("%20s", "-");
                }

                out.format("%9s", ifNode.getSourceSection().getStartLine());
                out.format("%11s", ifNode.getSourceSection().getStartColumn());
                out.format("%11s", ifNode.getSourceSection().getCharLength());
                out.println();
            }
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

    public static void printNodesEmptySourceSections() {
        // CheckStyle: stop system..print check
        for (PNode node : nodesEmptySourceSections) {
            System.out.println(node);
        }
        // CheckStyle: resume system..print check
    }
}
