package at.ssw.visualizer.logviewer.scene.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Katrin Strassl
 */
public class LogTableModel extends AbstractTableModel {
    private final static String[] columnNames = {"Line #", "Method", "Scope", "Node", "Log Text"};
    
    private List<TableLine> entries = new ArrayList<>();
    
    public void setLogEntries(List<TableLine> entries) {
        this.entries = entries;
        fireTableDataChanged();
    }
    
    public TableLine getTableLine(int line) {
        return entries.get(line);
    }
    
    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int i, int i1) {
        switch (i1) {
            case 0:
                return entries.get(i).getLineNr();
            case 1:
                return entries.get(i).getLogLine().getMethod().getName();
            case 2:
                return entries.get(i).getLogLine().getScope()!=null?
                           entries.get(i).getLogLine().getScope().getName():"";
            case 3:
                return entries.get(i).getLogLine().getNode()!=null?
                           entries.get(i).getLogLine().getNode().getName():"";
            case 4:
                return entries.get(i).getLogLine().getText();
        }
        return null;
    }
    
}
