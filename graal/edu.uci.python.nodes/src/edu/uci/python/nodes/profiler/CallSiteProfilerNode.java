package edu.uci.python.nodes.profiler;

import java.util.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

public class CallSiteProfilerNode extends ProfilerNode {

    private static List<CallSiteProfilerNode> profiledCallSites = new ArrayList<>();

    private HashMap<RootNode, Long> invocationCounts;

    public CallSiteProfilerNode(Node profiledNode) {
        super(profiledNode);
        invocationCounts = new HashMap<>();
        profiledCallSites.add(this);
    }

    public void executeWithCallableName(VirtualFrame frame, RootNode calleeRootNode) {
        execute(frame);
        if (invocationCounts.containsKey(calleeRootNode)) {
            long invocationCount = invocationCounts.get(calleeRootNode);
            invocationCounts.put(calleeRootNode, invocationCount + 1);
        } else {
            invocationCounts.put(calleeRootNode, 1L);
        }
    }

    public boolean doesCallRootNode(RootNode rootNode) {
        return invocationCounts.containsKey(rootNode);
    }

    public long getInvocationCounterOfRootNode(RootNode rootNode) {
        return invocationCounts.get(rootNode);
    }

    public static List<CallSiteProfilerNode> getProfiledCallSites() {
        return profiledCallSites;
    }

}
