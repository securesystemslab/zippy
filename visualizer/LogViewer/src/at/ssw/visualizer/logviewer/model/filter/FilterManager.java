package at.ssw.visualizer.logviewer.model.filter;

import at.ssw.visualizer.logviewer.model.LogLine;
import at.ssw.visualizer.logviewer.model.LogModel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexander Stipsits
 */
public class FilterManager {
	
	private final List<Filter> filters = new ArrayList<>();
	private final Filter methodFilter, scopeFilter, nodeFilter, fulltextFilter;
	
	/**
	 * Creates a new instance of the filter manager.
	 */
	public FilterManager() {
		add(methodFilter = new MethodFilter());
		add(scopeFilter = new ScopeFilter());
		add(nodeFilter = new NodeFilter());
		add(fulltextFilter = new FullTextFilter());
	}
	
	/**
	 * Executes a filtering process with the given log model as subject. Inactive filters will be ignored.
	 * @param model subject
	 * @return filtered list of log lines
	 * @throws InterruptedException 
	 */
	public List<LogLine> execute(LogModel model) throws InterruptedException {
		List<LogLine> input = model.getLogs();
		List<LogLine> output = new ArrayList<>();
		
		for(LogLine line : input) {
			if(Thread.interrupted()) throw new InterruptedException();
			if(keep(line)) output.add(line);
		}
		
		if(Thread.interrupted()) throw new InterruptedException();
		
		return output;
	}
	
	private boolean keep(LogLine line) {
		boolean keep = true;
		for(Filter filter : filters) {
			keep = keep && (!filter.isActive() || filter.keep(line));
		}
		return keep;
	}
	
	private Filter add(Filter filter) {
		filters.add(filter);
		return filter;
	}
	
	/**
	 * Returns the instance of a method filter
	 * @return method filter
	 */
	public Filter getMethodFilter() {
		return methodFilter;
	}
	
	/**
	 * Returns the instance of a scope filter
	 * @return scope filter
	 */
	public Filter getScopeFilter() {
		return scopeFilter;
	}
	
	/**
	 * Returns the instance of a node filter
	 * @return node filter
	 */
	public Filter getNodeFilter() {
		return nodeFilter;
	}
	
	/**
	 * Returns the instance of a full text filter
	 * @return full text filter
	 */
	public Filter getFullTextFilter() {
		return fulltextFilter;
	}
	
}
