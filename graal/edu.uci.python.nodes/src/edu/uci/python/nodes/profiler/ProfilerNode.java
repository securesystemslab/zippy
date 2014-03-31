package edu.uci.python.nodes.profiler;

import java.util.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;

public class ProfilerNode extends PNode {

    protected long counter;
    protected Node profiledNode;

    protected static List<ProfilerNode> profiledNodes = new ArrayList<>();

    public ProfilerNode(Node profiledNode) {
        this.profiledNode = profiledNode;
        this.counter = 0;
        profiledNodes.add(this);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        counter++;
        return null;
    }

    public Node getProfiledNode() {
        return profiledNode;
    }

    public long getProfilerResult() {
        return counter;
    }

    public static List<ProfilerNode> getProfiledNodes() {
        return profiledNodes;
    }

}
