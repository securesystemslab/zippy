package at.ssw.visualizer.logviewer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alexander Stipsits
 */
public class Method {

	private String name;
	private boolean isStub;
	private final LogModel model;
	private List<Scope> scopes = new ArrayList<>();
	private List<LogLine> logs = new ArrayList<>();
	
	Method(LogModel model) {
		name = null;
		isStub = false;
		this.model = model;
	}
	
	void init(String name, boolean isStub) {
		this.name = name;
		this.isStub = isStub;
	}
	
	/**
	 * Returns the name of the method
	 * @return the method name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns <code>true</code> if this is a stub method.
	 * @return <code>true</code> if is stub, <code>false</code> otherwise
	 */
	public boolean isStub() {
		return isStub;
	}
	
	/**
	 * Returns the model which holds this method.
	 * @return model-object
	 */
	public LogModel getModel() {
		return model;
	}
	
	void add(Scope scope) {
		scopes.add(scope);
	}
	
	/**
	 * Returns a list of all scopes inside a target method.
	 * @return list of scopes
	 */
	public List<Scope> getScopes() {
		return scopes;
	}
	
	void add(LogLine log) {
		logs.add(log);
	}
	
	/**
	 * Returns a list of all logs in the context of the method.
	 * @return list of log lines
	 */
	public List<LogLine> getLogs() {
		return Collections.unmodifiableList(logs);
	}
	
	/**
	 * The string representation of a method is its name.
	 */
	@Override
	public String toString() {
		return getName();
	}
	
}
