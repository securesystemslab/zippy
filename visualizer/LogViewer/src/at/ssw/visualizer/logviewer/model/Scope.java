package at.ssw.visualizer.logviewer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alexander Stipsits
 */
public class Scope {

	private final String name;
	private final Method method;
	private List<Node> nodes = new ArrayList<>();
	private List<LogLine> logs = new ArrayList<>();
	
	Scope(String name, Method method) {
		this.name = name;
		this.method = method;
	}
	
	/**
	 * Returns the scope name.
	 * @return the scope name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the parent method.
	 * @return the parent method
	 */
	public Method getMethod() {
		return method;
	}
	
	void add(Node node) {
		nodes.add(node);
	}
	
	/**
	 * Returns a list of all node-objects inside a scope.
	 * @return list of nodes
	 */
	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	
	void add(LogLine log) {
		logs.add(log);
	}
	
	/**
	 * Returns a list of all logs inside a scope.
	 * @return list of log lines
	 */
	public List<LogLine> getLogs() {
		return logs;
	}
	
	/**
	 * Returns the node inside a scope with the given number.
	 * @param number node number
	 * @return node-object
	 */
	public Node getNode(int number) {
		for(Node node : nodes) {
			if(node.getNumber() == number) return node;
		}
		return null;
	}
	
	/**
	 * The string representation of a scope is its name.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

}
