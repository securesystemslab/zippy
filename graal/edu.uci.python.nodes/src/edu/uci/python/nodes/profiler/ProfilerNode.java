package edu.uci.python.nodes.profiler;

import java.util.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;

/**
 * @author Gulfem
 */

public class ProfilerNode extends PNode {

    private long counter;
    private Node profiledNode;

    private static List<ProfilerNode> profiledNodes = new ArrayList<>();

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
