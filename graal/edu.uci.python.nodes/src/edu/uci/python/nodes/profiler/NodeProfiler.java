package edu.uci.python.nodes.profiler;

import java.util.*;

import edu.uci.python.nodes.loop.*;
import edu.uci.python.nodes.statement.*;

public class NodeProfiler {

    private static NodeProfiler INSTANCE = new NodeProfiler();

    private List<IfNode> ifNodes;
    private List<ForNode> forNodes;

    private NodeProfiler() {
        ifNodes = new ArrayList<>();
        forNodes = new ArrayList<>();
    }

    public void addIfNode(IfNode node) {
        ifNodes.add(node);
    }

    public void addForNode(ForNode node) {
        forNodes.add(node);
    }

    public void deleteForNode(ForNode node) {
        forNodes.remove(node);
    }

    public void printProfilerResults() {
// System.out.println("============= " + "For Results" + " ============= ");

// for (int i = 0; i < ifNodes.size(); i++) {
// IfNode ifNode = ifNodes.get(i);
// System.out.println("Then counter " + ifNode.thenCounter);
// System.out.println("Else counter " + ifNode.elseCounter);
// }

// for (int i = 0; i < forNodes.size(); i++) {
// ForNode forNode = forNodes.get(i);
// System.out.println("For counter " + forNode.forCounter);
// }

    }

    public static NodeProfiler getInstance() {
        return INSTANCE;
    }

}
