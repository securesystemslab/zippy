package at.ssw.visualizer.logviewer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alexander Stipsits
 */
public class Node {

	private final int number;
	private final String name;
	private final Scope scope;
	private List<LogLine> logs = new ArrayList<>();
	
	
	Node(int number, String name, Scope scope) {
		this.number = number;
		this.name = name;
		this.scope = scope;
	}
	
	/**
	 * Returns the node number.
	 * @return the node number
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Returns the node name.
	 * @return the node name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the parent scope.
	 * @return the parent scope
	 */
	public Scope getScope() {
		return scope;
	}
	
	void add(LogLine log) {
		logs.add(log);
	}
	
	/**
	 * Returns a list of all logs within a nodes context.
	 * @return list of log lines
	 */
	public List<LogLine> getLogs() {
		return Collections.unmodifiableList(logs);
	}
	
	/**
	 * The string represenation of a node is &quot;&lt;name&gt; (&lt;number&gt;)&quot;.
	 */
	@Override
	public String toString() {
		return getName() + "(" + getNumber() + ")";
	}
}
