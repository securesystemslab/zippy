package at.ssw.visualizer.logviewer.model.filter;

import at.ssw.visualizer.logviewer.model.LogLine;

/**
 *
 * @author Alexander Stipsits
 */
public interface Filter {

	/**
	 * Sets the constraints for a filter.<br>
	 * They can be every type of objects. In general these are strings, but it can also be
	 * a Integer (like in the <code>NodeFilter</code>):<br>
	 * <code>Filter nodeFilter = filterManager.getNodeFilter();
	 * filter.setConstraints((String)nodeName, (Integer)nodeNumber);</code>
	 * @param constraints filter constraints
	 */
	void setConstraints(Object ... constraints);
	
	/**
	 * Indicates whether to keep a log line in the filter results, or not.
	 * @param line log line which should be checked
	 * @return <code>true</code> if the line should be kept, <code>false</code> otherwise
	 */
	boolean keep(LogLine line);
	
	/**
	 * Activates or deactivates a filter. Deactivated filters will be ignored.
	 * @param active <code>true</code> to activate, <code>false</code> to deactivate
	 */
	void setActive(boolean active);
	
	/**
	 * Returns the activation status
	 * @return <code>true</code> for an active filter, <code>false</code> otherwise
	 */
	boolean isActive();
	
}
