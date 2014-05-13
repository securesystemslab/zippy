package edu.uci.python.nodes.profiler;

import java.util.*;
import java.util.Map.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.*;

/**
 * @author Gulfem
 */

public class ProfilerResultPrinter {

    public static void printProfilerInstrumenterResults() {
        Map<PythonWrapperNode, ProfilerInstrument> sorted = sortByValue(PythonNodeProber.getWrapperToInstruments());

        Iterator it = sorted.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            PythonWrapperNode wrapper = (PythonWrapperNode) entry.getKey();
            ProfilerInstrument instrument = (ProfilerInstrument) entry.getValue();
            System.out.println(wrapper.getChild() + " line " + wrapper.getChild().getSourceSection().getStartLine() + " column " + wrapper.getChild().getSourceSection().getStartColumn() + " = " +
                            instrument.getCounter());
        }
    }

    private static Map<PythonWrapperNode, ProfilerInstrument> sortByValue(Map<PythonWrapperNode, ProfilerInstrument> map) {
        List<Map.Entry<PythonWrapperNode, ProfilerInstrument>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<PythonWrapperNode, ProfilerInstrument>>() {

            public int compare(Map.Entry<PythonWrapperNode, ProfilerInstrument> m1, Map.Entry<PythonWrapperNode, ProfilerInstrument> m2) {
                return (int) (m2.getValue().getCounter() - m1.getValue().getCounter());
            }
        });

        Map<PythonWrapperNode, ProfilerInstrument> result = new LinkedHashMap<>();
        for (Map.Entry<PythonWrapperNode, ProfilerInstrument> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // "FUNCTION NAME"(13 char) + "10 space" + "INVOCATION COUNTS"(27 char)
    public static void printFunctionInvocationProfilerResults() {
        List<ProfilerNode> profiledNodes = ProfilerNode.getProfiledNodes();
        if (PythonOptions.SortProfilerResults) {
            sortTheProfilerList(profiledNodes);
        }

        // CheckStyle: stop system..print check
        System.out.format("%-23s", "Function Name");
        System.out.format("%-27s", "Number of Calls");
        System.out.println();
        System.out.println("=============          ===============");
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
            System.out.println();
        } else if (profilerResult < 0) {
            throw new RuntimeException("Profiler result can't be less than 0: " + profilerResult + " for " + profiledRootNode);
        }
    }

    private static void printInvocationCount(String functionName, long invocationCount) {
        // CheckStyle: stop system..print check
        System.out.format("%-23s", functionName);
        System.out.format("%15s", invocationCount);
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

}
