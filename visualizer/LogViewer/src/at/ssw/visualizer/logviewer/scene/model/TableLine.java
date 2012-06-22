package at.ssw.visualizer.logviewer.scene.model;

import at.ssw.visualizer.logviewer.model.LogLine;

/**
 *
 * @author Katrin Strassl
 */
public class TableLine {

    private int lineNr;
    private LogLine logLine;
    
    // display single log line
    public TableLine(LogLine logLine) {
        this(logLine, logLine.getLineNumber());
    }
    
    // display line number range (from logLine to lineNr or vice versa)
    public TableLine(LogLine logLine, int lineNr) {
        this.logLine = logLine;
        this.lineNr = lineNr;
    }
    
    public int getFirstLine() {
        return lineNr < logLine.getLineNumber()?lineNr:logLine.getLineNumber();
    }
    
    public int getLastLine() {
        return lineNr > logLine.getLineNumber()?lineNr:logLine.getLineNumber();
    }
    
    public String getLineNr() {
        if (lineNr == logLine.getLineNumber()) {
            return String.valueOf(lineNr);
        }
        return getFirstLine() + "-" + getLastLine();
    }
    
    public LogLine getLogLine() {
        return logLine;
    }
}
