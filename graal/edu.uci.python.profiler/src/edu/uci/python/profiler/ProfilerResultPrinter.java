package edu.uci.python.profiler;
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


import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem
 */

public class ProfilerResultPrinter {

    private PrintStream out = System.out;

    private PythonProfilerNodeProber profilerProber;
    private PythonProfiler profiler;

    private List<PNode> nodesEmptySourceSections = new ArrayList<>();

    private List<PNode> nodesUsingExistingProbes = new ArrayList<>();

    public ProfilerResultPrinter(PythonProfilerNodeProber profilerProber) {
        this.profilerProber = profilerProber;
    }

    public ProfilerResultPrinter(PythonProfilerNodeProber profilerProber, PythonProfiler profiler) {
        this.profilerProber = profilerProber;
        this.profiler = profiler;
    }

    public void addNodeEmptySourceSection(PNode node) {
        nodesEmptySourceSections.add(node);
    }

    public void addNodeUsingExistingProbe(PNode node) {
        nodesUsingExistingProbes.add(node);
    }

    public void printCallProfilerResults() {
        long totalCount = 0;
        List<ProfilerInstrument> callInstruments;
        if (PythonOptions.SortProfilerResults) {
            callInstruments = sortProfilerResult(profilerProber.getCallInstruments());
        } else {
            callInstruments = profilerProber.getCallInstruments();
        }

        if (callInstruments.size() > 0) {
            printBanner("Call Profiling Results", 72);
            /**
             * 50 is the length of the text by default padding left padding is added, so space is
             * added to the beginning of the string, minus sign adds padding to the right
             */

            out.format("%-50s", "Function Name");
            out.format("%-20s", "Number of Calls");
            out.format("%-9s", "Line");
            out.format("%-11s", "Column");
            out.format("%-11s", "Length");
            out.println();
            out.println("===============                                   ===============     ====     ======     ======");

            for (ProfilerInstrument instrument : callInstruments) {
                if (instrument.getCounter() > 0) {
                    Node node = instrument.getNode();
                    out.format("%-50s", ((FunctionRootNode) node.getRootNode()).getFunctionName());
                    out.format("%15s", instrument.getCounter());
                    totalCount = totalCount + instrument.getCounter();
                    out.format("%9s", node.getSourceSection().getStartLine());
                    out.format("%11s", node.getSourceSection().getStartColumn());
                    out.format("%11s", node.getSourceSection().getCharLength());
                    out.println();
                }
            }

            out.println("Total number of instruments : " + callInstruments.size());
            out.println("Total number of executed instruments: " + totalCount);
        }
    }

    public void printLoopProfilerResults() {
        long totalCount = 0;
        List<ProfilerInstrument> loopInstruments;
        if (PythonOptions.SortProfilerResults) {
            loopInstruments = sortProfilerResult(profilerProber.getLoopInstruments());
        } else {
            loopInstruments = profilerProber.getLoopInstruments();
        }

        if (loopInstruments.size() > 0) {
            printBanner("Loop Profiling Results", 132);

            out.format("%-50s", "Node");
            out.format("%-20s", "Counter");
            out.format("%-9s", "Line");
            out.format("%-11s", "Column");
            out.format("%-11s", "Length");
            out.format("%-70s", "In Method");
            out.println();
            out.println("=============                                     ===============     ====     ======     ======     =======================================================");

            for (ProfilerInstrument instrument : loopInstruments) {
                if (instrument.getCounter() > 0) {
                    Node node = instrument.getNode();
                    Node loopNode = node.getParent().getParent();

                    if (loopNode instanceof LoopNode) {
                        /**
                         * During generator optimizations for node is replaced with
                         * PeeledGeneratorLoopNode. Since the for loop is replaced, it's better not
                         * to print the result of this specific for profiling.
                         */
                        out.format("%-50s", loopNode.getClass().getSimpleName());
                        out.format("%15s", instrument.getCounter());
                        totalCount = totalCount + instrument.getCounter();
                        out.format("%9s", node.getSourceSection().getStartLine());
                        out.format("%11s", node.getSourceSection().getStartColumn());
                        out.format("%11s", node.getSourceSection().getCharLength());
                        out.format("%5s", "");
                        out.format("%-70s", node.getRootNode());
                        out.println();
                    }
                }
            }
        }

        out.println("Total number of instruments : " + loopInstruments.size());
        out.println("Total number of executed instruments: " + totalCount);
    }

    public void printIfProfilerResults() {
        Map<ProfilerInstrument, List<ProfilerInstrument>> ifInstruments;
        if (PythonOptions.SortProfilerResults) {
            ifInstruments = sortIfProfilerResults(profilerProber.getIfInstruments());
        } else {
            ifInstruments = profilerProber.getIfInstruments();
        }

        if (ifInstruments.size() > 0) {
            printBanner("If Node Profiling Results", 120);
            out.format("%-20s", "If Counter");
            out.format("%15s", "Then Counter");
            out.format("%20s", "Else Counter");
            out.format("%9s", "Line");
            out.format("%11s", "Column");
            out.format("%11s", "Length");
            out.format("%-70s", "In Method");
            out.println();
            out.println("===========            ============        ============     ====     ======     ======     =======================================================");

            Iterator<Map.Entry<ProfilerInstrument, List<ProfilerInstrument>>> it = ifInstruments.entrySet().iterator();
            while (it.hasNext()) {
                Entry<ProfilerInstrument, List<ProfilerInstrument>> entry = it.next();
                ProfilerInstrument ifInstrument = entry.getKey();
                if (ifInstrument.getCounter() > 0) {
                    List<ProfilerInstrument> instruments = entry.getValue();
                    ProfilerInstrument thenInstrument = instruments.get(0);
                    out.format("%11s", ifInstrument.getCounter());
                    out.format("%24s", thenInstrument.getCounter());

                    if (instruments.size() == 1) {
                        out.format("%20s", "-");
                    } else if (instruments.size() == 2) {
                        ProfilerInstrument elseInstrument = instruments.get(1);
                        out.format("%20s", elseInstrument.getCounter());
                    }

                    Node ifNode = ifInstrument.getNode();
                    out.format("%9s", ifNode.getSourceSection().getStartLine());
                    out.format("%11s", ifNode.getSourceSection().getStartColumn());
                    out.format("%11s", ifNode.getSourceSection().getCharLength());
                    out.format("%5s", "");
                    out.format("%-70s", ifNode.getRootNode());
                    out.println();
                }
            }
        }
    }

    public void printNodeProfilerResults() {
        long totalCount = 0;
        List<ProfilerInstrument> nodeInstruments;
        if (PythonOptions.SortProfilerResults) {
            nodeInstruments = sortProfilerResult(profilerProber.getNodeInstruments());
        } else {
            nodeInstruments = profilerProber.getNodeInstruments();
        }

        if (nodeInstruments.size() > 0) {
            printBanner("Node Profiling Results", 132);
            out.format("%-50s", "Node");
            out.format("%-20s", "Counter");
            out.format("%-9s", "Line");
            out.format("%-11s", "Column");
            out.format("%-11s", "Length");
            out.format("%-70s", "In Method");
            out.println();
            out.println("=============                                     ===============     ====     ======     ======     =======================================================");

            for (ProfilerInstrument instrument : nodeInstruments) {
                if (instrument.getCounter() > 0) {
                    Node node = instrument.getNode();
                    out.format("%-50s", node.getClass().getSimpleName());
                    out.format("%15s", instrument.getCounter());
                    totalCount = totalCount + instrument.getCounter();
                    out.format("%9s", node.getSourceSection().getStartLine());
                    out.format("%11s", node.getSourceSection().getStartColumn());
                    out.format("%11s", node.getSourceSection().getCharLength());
                    out.format("%5s", "");
                    out.format("%-70s", node.getRootNode());
                    out.println();
                }
            }
            out.println("Total number of instruments : " + nodeInstruments.size());
            out.println("Total number of executed instruments: " + totalCount);
        }
    }

    public void printNodeProfilerResultsWithoutInstruments() {
        long totalCount = 0;
        List<ProfilerNode> nodes;
        if (PythonOptions.SortProfilerResults) {
            nodes = sortProfilerResult2(profiler.getNodes());
        } else {
            nodes = profiler.getNodes();
        }

        if (nodes.size() > 0) {
            printBanner("Node Profiling Results Without Instruments", 132);
            out.format("%-50s", "Node");
            out.format("%-20s", "Counter");
            out.format("%-9s", "Line");
            out.format("%-11s", "Column");
            out.format("%-11s", "Length");
            out.format("%-70s", "In Method");
            out.println();
            out.println("=============                                     ===============     ====     ======     ======     =======================================================");

            for (ProfilerNode node : nodes) {
                if (node.getCounter() > 0) {
                    out.format("%-50s", node.getChild().getClass().getSimpleName());
                    out.format("%15s", node.getCounter());
                    totalCount = totalCount + node.getCounter();
                    out.format("%9s", node.getChild().getSourceSection().getStartLine());
                    out.format("%11s", node.getChild().getSourceSection().getStartColumn());
                    out.format("%11s", node.getChild().getSourceSection().getCharLength());
                    out.format("%5s", "");
                    out.format("%-70s", node.getChild().getRootNode());
                    out.println();
                }
            }
            out.println("Total Count: " + totalCount);
        }
    }

    private static List<ProfilerNode> sortProfilerResult2(List<ProfilerNode> list) {
        Collections.sort(list, new Comparator<ProfilerNode>() {
            @Override
            public int compare(final ProfilerNode profiler1, final ProfilerNode profiler2) {
                return Long.compare(profiler2.getCounter(), profiler1.getCounter());
            }
        });

        return list;
    }

    private static List<ProfilerInstrument> sortProfilerResult(List<ProfilerInstrument> list) {
        Collections.sort(list, new Comparator<ProfilerInstrument>() {
            @Override
            public int compare(final ProfilerInstrument profiler1, final ProfilerInstrument profiler2) {
                return Long.compare(profiler2.getCounter(), profiler1.getCounter());
            }
        });

        return list;
    }

    private static Map<ProfilerInstrument, List<ProfilerInstrument>> sortIfProfilerResults(Map<ProfilerInstrument, List<ProfilerInstrument>> map) {
        List<Map.Entry<ProfilerInstrument, List<ProfilerInstrument>>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<ProfilerInstrument, List<ProfilerInstrument>>>() {

            public int compare(Map.Entry<ProfilerInstrument, List<ProfilerInstrument>> if1, Map.Entry<ProfilerInstrument, List<ProfilerInstrument>> if2) {
                return Long.compare(if2.getKey().getCounter(), if1.getKey().getCounter());
            }
        });

        Map<ProfilerInstrument, List<ProfilerInstrument>> result = new LinkedHashMap<>();
        for (Map.Entry<ProfilerInstrument, List<ProfilerInstrument>> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;

    }

    public void printNodesEmptySourceSections() {
        if (nodesEmptySourceSections.size() > 0) {
            printBanner("Nodes That Have Empty Source Sections", 10);
            for (PNode node : nodesEmptySourceSections) {
                out.println(node.getClass().getSimpleName() + " in " + node.getRootNode());
            }
        }
    }

    public void printNodesUsingExistingProbes() {
        if (nodesUsingExistingProbes.size() > 0) {
            printBanner("Nodes That Reuses an Existing Probe", 10);
            for (PNode node : nodesUsingExistingProbes) {
                out.println(node.getClass().getSimpleName() + " in " + node.getRootNode());
            }
        }
    }

    private static void printBanner(String caption, int size) {
        // CheckStyle: stop system..print check
        for (int i = 0; i < size / 2; i++) {
            System.out.print("=");
        }

        System.out.print(" " + caption + " ");

        for (int i = 0; i < size / 2; i++) {
            System.out.print("=");
        }

        System.out.println();
        // CheckStyle: resume system..print check
    }
}
