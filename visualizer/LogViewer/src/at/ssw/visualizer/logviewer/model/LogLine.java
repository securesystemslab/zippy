package at.ssw.visualizer.logviewer.model;

/**
 *
 * @author Alexander Stipsits
 */
public class LogLine {

    private final int lineNum;
	private final CharSequence text;
	private final Method method;
	private final Scope scope;
	private final Node node;
	
	LogLine(int lineNum, CharSequence text, Method method, Scope scope, Node node) {
		this.lineNum = lineNum;
        this.text = text;
		this.method = method;
		this.scope = scope;
		this.node = node;
	}
    
    /**
     * Returns the number of the line in the log file
     * @return file line number
     */
    public int getLineNumber() {
        return lineNum;
    }
	
	/**
	 * Returns the text of the log line
	 * @return log line text
	 */
	public String getText() {
		return text.toString();
	}
	
	/**
	 * Returns the parent method
	 * @return the parent method
	 */
	public Method getMethod() {
		return method;
	}
	
	/**
	 * Returns the parent scope
	 * @return the parent scope
	 */
	public Scope getScope() {
		return scope;
	}
	
	/**
	 * Returns the parent node
	 * @return the parent node (or <code>null</code> if the log is not in a node's context)
	 */
	public Node getNode() {
		return node;
	}
	
	/**
	 * The string representation of a log line is the text of it.
	 */
	@Override
	public String toString() {
		return getText();
	}
	
}
