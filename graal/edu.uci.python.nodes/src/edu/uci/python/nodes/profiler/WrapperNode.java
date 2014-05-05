package edu.uci.python.nodes.profiler;

import java.util.*;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;

public class WrapperNode extends PNode {

    protected long counter;
    protected final @Child PNode profiledNode;

    static List<WrapperNode> wrapperNodes = new ArrayList<>();

    public WrapperNode(PNode profiledNode) {
        this.profiledNode = insert(profiledNode);
        this.counter = 0;
        wrapperNodes.add(this);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        counter++;
        return profiledNode.execute(frame);
    }

    public PNode getProfiledNode() {
        return profiledNode;
    }

    public long getProfilerResult() {
        return counter;
    }

    public static void print() {
        sortTheProfilerList(wrapperNodes);
        for (WrapperNode wrapperNode : wrapperNodes) {
            System.out.println(wrapperNode.profiledNode + " = " + wrapperNode.counter);
        }
    }

    private static void sortTheProfilerList(List<WrapperNode> list) {
        if (!list.isEmpty()) {
            Collections.sort(list, new Comparator<WrapperNode>() {
                @Override
                public int compare(WrapperNode p1, WrapperNode p2) {
                    // Descending order
                    return Long.compare(p2.getProfilerResult(), p1.getProfilerResult());
                }
            });
        }
    }
}
