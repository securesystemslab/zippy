package at.ssw.visualizer.logviewer.model;

import at.ssw.visualizer.logviewer.model.io.FileLine;
import at.ssw.visualizer.logviewer.model.io.ProgressMonitor;
import at.ssw.visualizer.logviewer.model.io.SeekableFile;
import at.ssw.visualizer.logviewer.model.io.SeekableFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Alexander Stipsits
 */
public class LogParser {
	
	private static final Pattern METHOD_PATTERN =
		Pattern.compile("Finished target method HotSpotMethod<(.+?)>, isStub (true|false)");
	private static final Pattern SCOPE_PATTERN =
		Pattern.compile("scope: (.+?)");
	private static final Pattern NODE_PATTERN =
		Pattern.compile(".*?([0-9]+)\\|([0-9a-zA-Z.]+).*?");
	
	private final List<ParseErrorListener> errorListeners = new ArrayList<>();
	private final List<ParseError> errors = new ArrayList<>();
	
	/**
	 * Parses a log file without any feedback. When the parsing process is finished, the method returns a LogModel-object.
	 * @param file log file
	 * @return model-object
	 * @throws IOException
	 * @throws LogParserException
	 */
	public LogModel parse(File file) throws IOException {
		return parse(file, null);
	}
	
	/**
	 * Parses a log file and uses a ProgressMonitor to give the caller a progress feedback. It returns a LogModel-object.
	 * @param file log file
	 * @param monitor monitor for progress monitoring (uses default gap)
	 * @return model-object
	 * @throws IOException
	 * @throws LogParserException
	 */
	public LogModel parse(File file, ProgressMonitor monitor) throws IOException {
		return parse(file, monitor, SeekableFileReader.DEFAULT_GAP);
	} 
	
	/**
	 * Parses a log file and uses a ProgressMonitor to give the caller a progress feedback. It returns a LogModel-object.
	 * @param file log file
	 * @param monitor monitor for progress monitoring (uses custom gap)
	 * @param gap custom gap (every x bytes read from the file, the monitor will be informed)
	 * @return model-object
	 * @throws IOException
	 * @throws LogParserException
	 */
	public LogModel parse(File file, ProgressMonitor monitor, int gap) throws IOException {
		errors.clear();
		SeekableFileReader reader = new SeekableFileReader(file, monitor, gap);
		SeekableFile seekableFile = reader.getSeekableFile();
		BufferedReader r = new BufferedReader(reader);
		
		LogModel model = new LogModel();
		Method currentMethod = new Method(model);
		Scope currentScope = null;
		
		Matcher matcher;
		boolean methodNameSet = false;
		
		int lineNum = -1;
		String line;
		while((line = r.readLine()) != null) {
			lineNum++;
			
			methodNameSet = false;
			
			matcher = SCOPE_PATTERN.matcher(line);
			if(matcher.matches()) {
				String name = matcher.group(1);
				currentScope = new Scope(name, currentMethod);
				currentMethod.add(currentScope);
			}
            
            matcher = METHOD_PATTERN.matcher(line);
            boolean methodPatternMatches = matcher.matches();
            
            if(methodPatternMatches){
                currentScope = null;
				String name = matcher.group(1);
				boolean isStub = Boolean.parseBoolean(matcher.group(2));
				currentMethod.init(name, isStub);
				model.add(currentMethod);
				methodNameSet = true;
            }
			
			Node currentNode = null;
			matcher = NODE_PATTERN.matcher(line);
			if(matcher.matches()) {
                if(currentScope == null){
                	raiseError(lineNum, line, "scope is missing");
                	continue;
                }
				int number = Integer.parseInt(matcher.group(1));
				currentNode = currentScope.getNode(number);
				if(currentNode == null) {
					String name = matcher.group(2);
					currentNode = new Node(number, name, currentScope);
					currentScope.add(currentNode);
				}
			}
			
			FileLine fileLine = seekableFile.get(lineNum);
			LogLine log = new LogLine(lineNum, fileLine, currentMethod, currentScope, currentNode);
			if(currentNode != null) currentNode.add(log);
			if(currentScope != null) currentScope.add(log);
			currentMethod.add(log);
			model.add(log);
            
            if(methodPatternMatches) {
				currentMethod = new Method(model);
			}
		}
		
		if(!methodNameSet) raiseError(lineNum, line, "unexpected end of stream");
		
		return model;
	}
	
	private void raiseError(int lineNum, String line, String message) {
		ParseError error = new ParseError(lineNum, line, message);
		errors.add(error);
		for(ParseErrorListener listener : errorListeners) {
			listener.errorOccurred(error);
		}
	}
	
	/**
	 * Adds a ParseErrorListener to the listeners
	 * @param listener
	 */
	public void addParseErrorListener(ParseErrorListener listener) {
		errorListeners.add(listener);
	}
	
	public void removeParseErrorListener(ParseErrorListener listener) {
		errorListeners.remove(listener);
	}
	
	/**
	 * Checks if errors occurred during the parsing process
	 * @return <code>true</code> if there are errors stored, <code>false</code> otherwise
	 */
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	/**
	 * Returns a list of all errors.
	 * @return error list
	 */
	public List<ParseError> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
	public static class ParseError {
		
		private final int lineNum;
		private final String line;
		private final String message;
		
		private ParseError(int lineNum, String line, String message) {
			this.lineNum = lineNum;
			this.message = message;
			this.line = line;
		}
		
		public int getLineNumber() {
			return lineNum;
		}
		
		public String getLine() {
			return line;
		}
		
		public String getMessage() {
			return message;
		}
		
	}
	
	public static interface ParseErrorListener {
		
		/**
		 * Called when a new error occurs.<br><br>
		 * <b>Attention</b>: This method runs on the same thread as the parsing process.
		 * If time-consuming tasks should be executed, please consider multithreading.
		 * @param error
		 */
		public void errorOccurred(ParseError error);
		
	}
	
}
