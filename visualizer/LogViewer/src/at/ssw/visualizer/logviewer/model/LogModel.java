package at.ssw.visualizer.logviewer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author Alexander Stipsits
 */
public class LogModel {

	private List<Method> methods = new ArrayList<>();
	private List<LogLine> logs = new ArrayList<>();
	
	void add(Method method) {
		methods.add(method);
	}
	
	/**
	 * Returns a list of all methods within the log model.
	 * @return list of methods
	 */
	public List<Method> getMethods() {
		return Collections.unmodifiableList(methods);
	}
	
	void add(LogLine log) {
		logs.add(log);
	}
	
	/**
	 * Returns a list of all logs
	 * @return list of log lines
	 */
	public List<LogLine> getLogs() {
		return Collections.unmodifiableList(logs);
	}
	
	/**
	 * Returns a list of logs which are before and after a specific log line (and the line itself).
	 * @param log reference log line
	 * @param pre number of lines before the reference line
	 * @param post number of lines after the reference line
	 * @return list of log lines (size: pre + post + 1)
	 */
	public List<LogLine> range(LogLine log, int pre, int post) {
		if(log == null) throw new NoSuchElementException();
		
		List<LogLine> logs = getLogs();
		int index = logs.indexOf(log);
		
		if(index == -1) throw new NoSuchElementException();
		
		int fromIndex = index - pre;
		int toIndex = index + post + 1;
		
		return getLogs().subList(fromIndex > 0 ? fromIndex : 0, toIndex < logs.size() ? toIndex : logs.size());
	}
	
	/**
	 * Returns a string with log l which are before and after a specific log line (and the line itself).
	 * @param log reference log line
	 * @param pre number of lines before the reference line
	 * @param post number of lines after the reference line
	 * @return list of log lines (size: pre + post + 1)
	 */
	public String rangeAsString(LogLine log, int pre, int post) {
		List<LogLine> list = range(log, pre, post);
		
		String ls = System.getProperty("line.separator");
		
		boolean first = true;
		StringBuilder buf = new StringBuilder();
		for(LogLine line : list) {
			if(!first) buf.append(ls);
			else first = false;
			
			buf.append(line);
		}
		
		return buf.toString();
	}
	
	public LogLine getLogLine(int lineNum) {
		int index = lineNum;
		if(index >= logs.size()) index = logs.size()-1;
		
		LogLine logLine = logs.get(lineNum);
		while(logLine.getLineNumber() > lineNum) {
			logLine = logs.get(--index);
		}
		while(logLine.getLineNumber() < lineNum) {
			logLine = logs.get(++index);
		}
		
		return logLine;
	}
	
	public List<LogLine> range(int lineNum, int pre, int post) {
		LogLine logLine = getLogLine(lineNum);
		return range(logLine, pre, post);
	}
	
	
}
