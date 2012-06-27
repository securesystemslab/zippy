package at.ssw.visualizer.logviewer.scene;

import at.ssw.visualizer.logviewer.model.LogParser;
import java.awt.Component;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Katrin Strassl
 */
public class ImportLogErrorDialog {
    
    public static void showDialog(Component parent, List<LogParser.ParseError> errors) {
        JTextArea txaErrors = new JTextArea();
        
        for (LogParser.ParseError error : errors) {
            txaErrors.append("Error at line " + error.getLineNumber());
            txaErrors.append(": " + error.getMessage());
            txaErrors.append("\n");
            txaErrors.append("Log line: " + error.getLine());
            txaErrors.append("\n");
        }
        
        JScrollPane scpErrors = new JScrollPane(txaErrors);
        
        JOptionPane.showMessageDialog(parent, scpErrors, "Parse errors", JOptionPane.ERROR_MESSAGE);
    }
}
