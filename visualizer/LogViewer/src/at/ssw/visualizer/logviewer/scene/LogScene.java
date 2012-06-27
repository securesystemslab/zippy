package at.ssw.visualizer.logviewer.scene;

import at.ssw.visualizer.logviewer.model.LogLine;
import at.ssw.visualizer.logviewer.model.LogModel;
import at.ssw.visualizer.logviewer.model.LogParser;
import at.ssw.visualizer.logviewer.model.filter.Filter;
import at.ssw.visualizer.logviewer.model.filter.FilterManager;
import at.ssw.visualizer.logviewer.model.io.ProgressMonitor;
import at.ssw.visualizer.logviewer.scene.model.LogTableModel;
import at.ssw.visualizer.logviewer.scene.model.TableLine;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import sun.awt.CausedFocusEvent;

/**
 *
 * @author Katrin Strassl
 */
public class LogScene extends JPanel {
    private static final String PREFERENCE_DIR = "dir";
    
    private static final String ICON_PREFIX = "/at/ssw/visualizer/logviewer/scene/icons/";
    private static final String ICON_ARROW_DOWN = ICON_PREFIX + "arrow_down.png";
    private static final String ICON_ARROW_UP = ICON_PREFIX + "arrow_up.png";
    private static final String ICON_BOOKMARK_BACK = ICON_PREFIX + "bookmark_back.png";
    private static final String ICON_BOOKMARK_FWD = ICON_PREFIX + "bookmark_forward.png";
    private static final String ICON_BOOKMARK_LIST = ICON_PREFIX + "bookmark_list.png";
    private static final String ICON_LOAD = ICON_PREFIX + "loading.gif";
    
    private static final ImageIcon IMGICON_LOAD = new ImageIcon(LogScene.class.getResource(ICON_LOAD));
    
    private static final int LOG_LINES = 50;
    private static final int KEYSTROKE_RATE = 100; // rate at which the keystrokes are checked
    private static final int KEYSTROKE_TRESHOLD = 400; // time in ms after which a search is triggered
            
    private FilterManager filterManager;
    private Thread filterRunThread;
    
    private int timeSinceLastKeystroke = -1;
    
    private JLabel lblMessage;
    private JLabel lblIcon;
    
    private JLabel lblMethodFilter;
    private JLabel lblScopeFilter;
    private JLabel lblNodeFilter;
    private JLabel lblFulltextFilter;
    private JTextField txfMethodFilter;
    private JTextField txfScopeFilter;
    private JTextField txfNodeFilter;
    private JTextField txfFulltextFilter;
    
    private JTable tblResult;
    private LogTableModel tblResultModel;
    
    private BookmarkableLogViewer logViewer;
    private JButton btnBookmarkBack;
    private JButton btnBookmarkFwd;
    private JButton btnBookmarkList;
    private JButton btnLoadMoreUp;
    private JButton btnLoadMoreDown;
    
    private LogParser parser = new LogParser();
    private LogModel logModel = null;
    private LogStatus logStatus = LogStatus.NO_LOG;
    
    private static LogScene instance;
    
    private enum LogStatus {
        NO_LOG, LOADING, ACTIVE
    }

    public static LogScene getInstance() {
        return instance;
    }
    
    public LogScene() {
        initComponents();
    }
    
    private void initComponents() {
        filterManager = new FilterManager();
        
        lblMessage = new JLabel("No logfile loaded.");
        lblIcon = new JLabel("");
        
        // Initialize filter components
        lblMethodFilter = new JLabel("Target Method:");
        lblScopeFilter = new JLabel("Scope:");
        lblNodeFilter = new JLabel("Node:");
        lblFulltextFilter = new JLabel("Fulltext:");
        
        txfMethodFilter = new JTextField();
        txfScopeFilter = new JTextField();
        txfNodeFilter = new JTextField();
        txfFulltextFilter = new JTextField();
        
        tblResultModel = new LogTableModel();
        tblResult = new JTable(tblResultModel);
        tblResult.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblResult.getTableHeader().setReorderingAllowed(false);
        tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        Dimension dim = tblResult.getPreferredScrollableViewportSize();
        tblResult.setPreferredScrollableViewportSize(new Dimension(dim.width, 10*tblResult.getRowHeight() + tblResult.getTableHeader().getHeight()));
        JScrollPane scpTblResult = new JScrollPane(tblResult);
        scpTblResult.setMinimumSize(new Dimension(dim.width, 10*tblResult.getRowHeight() + tblResult.getTableHeader().getHeight()));
        
        logViewer = new BookmarkableLogViewer();
        
        btnLoadMoreUp = new JButton(new ImageIcon(LogScene.class.getResource(ICON_ARROW_UP)));
        btnLoadMoreDown = new JButton(new ImageIcon(LogScene.class.getResource(ICON_ARROW_DOWN)));
        
        btnLoadMoreUp.setFocusable(false);
        btnLoadMoreUp.setToolTipText("Load more previous log lines");
        
        btnLoadMoreDown.setFocusable(false);
        btnLoadMoreDown.setToolTipText("Load more following log lines");
        
        btnBookmarkBack = new JButton(new ImageIcon(LogScene.class.getResource(ICON_BOOKMARK_BACK)));
        btnBookmarkList = new JButton(new ImageIcon(LogScene.class.getResource(ICON_BOOKMARK_LIST)));
        btnBookmarkFwd = new JButton(new ImageIcon(LogScene.class.getResource(ICON_BOOKMARK_FWD)));
        
        btnBookmarkBack.setFocusable(false);
        btnBookmarkBack.setToolTipText("Navigate to the last bookmark.");
        
        btnBookmarkFwd.setFocusable(false);
        btnBookmarkFwd.setToolTipText("Navigate to the next bookmark.");
        
        btnBookmarkList.setFocusable(false);
        btnBookmarkList.setToolTipText("List all known bookmarks.");
        
        JPanel pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());
        pnl.setOpaque(false);
        
        JPanel logViewerPanel = new JPanel();
        logViewerPanel.setLayout(new GridBagLayout());
        logViewerPanel.setOpaque(false);
        
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.LINE_AXIS));
        messagePanel.setOpaque(false);
        
        // Initialize Listeners
        initListeners();
        
        // Layout components
        GridBagConstraints gbc = new GridBagConstraints();
        
        messagePanel.add(lblMessage);
        messagePanel.add(Box.createHorizontalGlue());
        messagePanel.add(lblIcon);
        
        Insets standardInsets = gbc.insets;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 0, 2, 0);
        pnl.add(messagePanel, gbc);
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridy++;
        gbc.insets = standardInsets;
        pnl.add(lblMethodFilter, gbc);
        
        gbc.gridy++;
        pnl.add(lblScopeFilter, gbc);
        
        gbc.gridy++;
        pnl.add(lblNodeFilter, gbc);
        
        gbc.gridy++;
        pnl.add(lblFulltextFilter, gbc);
        
        gbc.gridx++;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.75;
        pnl.add(txfMethodFilter, gbc);
        
        gbc.gridy++;
        pnl.add(txfScopeFilter, gbc);
        
        gbc.gridy++;
        pnl.add(txfNodeFilter, gbc);
        
        gbc.gridy++;
        pnl.add(txfFulltextFilter, gbc);
        
        gbc.gridx = 0;
        //gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        //pnl.add(btnSearch, gbc);
        
        gbc.gridy++;
        pnl.add(scpTblResult, gbc);
        
        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        pnl.add(logViewerPanel, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 6;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        logViewerPanel.add(logViewer, gbc);
        
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.gridheight = 1;
        logViewerPanel.add(btnLoadMoreUp, gbc);
        
        gbc.gridy++;
        logViewerPanel.add(btnLoadMoreDown, gbc);
        
        gbc.gridy++;
        logViewerPanel.add(Box.createRigidArea(btnLoadMoreUp.getSize()));
        
        gbc.gridy++;
        logViewerPanel.add(btnBookmarkBack, gbc);
        
        gbc.gridy++;
        logViewerPanel.add(btnBookmarkFwd, gbc);
        
        gbc.gridy++;
        logViewerPanel.add(btnBookmarkList, gbc);
        
        this.setLayout(new BorderLayout());
        this.add(pnl, BorderLayout.CENTER);
        
        Timer keyTimer = new Timer();
        keyTimer.scheduleAtFixedRate(new KeystrokeTimer(), KEYSTROKE_RATE, KEYSTROKE_RATE);
        
        LogScene.instance = this;
    }
    
    private void initListeners() {
        txfMethodFilter.addKeyListener(new FilterKeyListener());
        txfScopeFilter.addKeyListener(new FilterKeyListener());
        txfNodeFilter.addKeyListener(new FilterKeyListener());
        txfFulltextFilter.addKeyListener(new FilterKeyListener());
        
        txfMethodFilter.addFocusListener(new FilterFocusListener());
        txfScopeFilter.addFocusListener(new FilterFocusListener());
        txfNodeFilter.addFocusListener(new FilterFocusListener());
        txfFulltextFilter.addFocusListener(new FilterFocusListener());
        
        btnLoadMoreUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int line = logViewer.getCurrentlyDisplayedTopLine();
                int top = logViewer.getCurrentTopLine();
                int bottom = logViewer.getCurrentBottomLine();
                logViewer.setLogLines(logModel.range(line, line - top + LOG_LINES, bottom - line), line); 
            } 
        });
        
        btnLoadMoreDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int line = logViewer.getCurrentlyDisplayedTopLine();
                int top = logViewer.getCurrentTopLine();
                int bottom = logViewer.getCurrentBottomLine();
                logViewer.setLogLines(logModel.range(line, line - top, bottom - line + LOG_LINES), line); 
            } 
        });
        
        btnBookmarkBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int result = logViewer.tryLastBookmark();
                if (result >= 0) {
                    tblResult.clearSelection();
                    logViewer.setLogLines(logModel.range(result, LOG_LINES, LOG_LINES), result); 
                }
            }
        });
        
        btnBookmarkFwd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int result = logViewer.tryNextBookmark();
                if (result >= 0) {
                    tblResult.clearSelection();
                    logViewer.setLogLines(logModel.range(result, LOG_LINES, LOG_LINES), result);
                }
            }
        });
        
        btnBookmarkList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                List<LogLine> bookmarkedLines = logViewer.getBookmarkedLines();
                if (bookmarkedLines.size() > 0) {
                    BookmarkDialog bd = new BookmarkDialog(bookmarkedLines, null);
                    if (bd.getTarget() != null) {
                        LogLine target = bd.getTarget();
                        int line = target.getLineNumber();
                        logViewer.setLogLines(logModel.range(line, LOG_LINES, LOG_LINES), line); 
                    }
                } else {
                    JOptionPane.showMessageDialog(LogScene.this, "No bookmarks set.", "", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        tblResult.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (tblResult.getSelectedRow() == -1) {
                    logViewer.clearLogLines();
                    return;
                }
                
                TableLine selectedLine = tblResultModel.getTableLine(tblResult.getSelectedRow());
                int firstLine = selectedLine.getFirstLine();
                int lastLine = selectedLine.getLastLine();
                
                if (firstLine == lastLine) { 
                    // only display selected line
                    List<LogLine> line = new ArrayList<>();
                    line.add(selectedLine.getLogLine());
                    logViewer.setLogLines(line);
                } else {
                    // display selected range
                    int pre = firstLine==lastLine?LOG_LINES:selectedLine.getLogLine().getLineNumber()-firstLine;
                    int post = firstLine==lastLine?LOG_LINES:lastLine-selectedLine.getLogLine().getLineNumber();

                    logViewer.setLogLines(logModel.range(selectedLine.getLogLine(), pre, post));           
                }
            }
        });
    }
    
    private boolean executeFilters() {
        if (logStatus == LogStatus.NO_LOG) {
            loadLogFile();
            return false;
        }
        if (logStatus == LogStatus.LOADING) {
            return false;
        }

        boolean execute = trySetFilter(filterManager.getMethodFilter(), txfMethodFilter.getText().trim(), lblMethodFilter);
        execute = trySetFilter(filterManager.getScopeFilter(), txfScopeFilter.getText().trim(), lblScopeFilter) || execute;
        try {
            // node number
            int node = Integer.parseInt(txfNodeFilter.getText().trim());
            execute = trySetFilter(filterManager.getNodeFilter(), node, lblNodeFilter) || execute;
        } catch (Exception e) {
            // node name
            execute = trySetFilter(filterManager.getNodeFilter(), txfNodeFilter.getText().trim(), lblNodeFilter) || execute;
        }
        execute = trySetFilter(filterManager.getFullTextFilter(), txfFulltextFilter.getText().trim(), lblFulltextFilter) || execute;

        if (!execute)
            return true;
        
        tryInterruptFilter();

        filterRunThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    List<LogLine> result = filterManager.execute(logModel);
                    lblIcon.setIcon(null);

                    if (!Thread.interrupted())
                        buildTableEntries(result);
                } catch (InterruptedException e) {
                }
            }

        });
        lblIcon.setIcon(IMGICON_LOAD);
        filterRunThread.start();
        
        return true;
    }
    
    private boolean trySetFilter(Filter filter, Object constraint, JLabel marker) {
        try {
            filter.setConstraints(constraint);
            marker.setForeground(Color.BLACK);
            return constraint != null && (!(constraint instanceof String) || (constraint instanceof String && ((String)constraint).trim().length() > 0));
        } catch (PatternSyntaxException e) {
            filter.setConstraints(new Object[]{});
            marker.setForeground(Color.RED);
            return false;
        }
    }
    
    private void tryInterruptFilter() {
        if (filterRunThread != null && (filterRunThread.isAlive() || !filterRunThread.isInterrupted())) {
            lblIcon.setIcon(null);
            filterRunThread.interrupt();
        }
    }
    
    private void buildTableEntries(List<LogLine> filterResult) {
        List<TableLine> tableEntries;
        
        int methodLength = txfMethodFilter.getText().trim().length();
        int scopeLength = txfScopeFilter.getText().trim().length();
        int nodeLength = txfNodeFilter.getText().trim().length();
        int fulltextLength = txfFulltextFilter.getText().trim().length();
        
        boolean preventGrouping = nodeLength != 0 || fulltextLength != 0;
        
        if (!preventGrouping && methodLength > 0 && scopeLength == 0) {
            tableEntries = groupByMethod(filterResult);
        } else if (!preventGrouping && scopeLength > 0) {
            tableEntries = groupByScope(filterResult);
        } else {
            tableEntries = showAllLines(filterResult);
        }
        
        tblResultModel.setLogEntries(tableEntries);
        
        int width = 0;
        for (int i = 0; i < tableEntries.size(); i++) {
            TableCellRenderer tcr = tblResult.getCellRenderer(i, 0);
            Component c = tcr.getTableCellRendererComponent(tblResult, tblResultModel.getValueAt(i, 0), false, false, i, 0);
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                FontMetrics fm = l.getFontMetrics(l.getFont());
                width = Math.max(width, fm.stringWidth(l.getText()));
            }
        }
       
        // proper resizing of column
        // setWidth and setPreferredWidth do not trigger resize
        TableColumn col = tblResult.getColumnModel().getColumn(0);
        col.setMinWidth(width+5);
        col.setMaxWidth(width+5);
        
        // reenable resizing for user
        col.setMinWidth(0);
        col.setMaxWidth(999);
    }
    
    private List<TableLine> groupByMethod(List<LogLine> filterResult) {
        List<TableLine> tableEntries = new ArrayList<>();
        int firstLine = -1;
        for (int i = 0; i < filterResult.size(); i++) {
            LogLine line = filterResult.get(i);
            
            if (firstLine < 0)
                firstLine = line.getLineNumber();

            if (i < filterResult.size() - 1) {
                LogLine next = filterResult.get(i+1);
                if (line.getMethod() != next.getMethod()) {
                    tableEntries.add(new TableLine(line, firstLine));
                    firstLine = next.getLineNumber();
                }
            } else {
                tableEntries.add(new TableLine(line, firstLine));
            }
        }
        return tableEntries;
    }
    
    private List<TableLine> groupByScope(List<LogLine> filterResult) {
        List<TableLine> tableEntries = new ArrayList<>();
        LogLine firstScopeLine = null;
        for (int i = 0; i < filterResult.size(); i++) {
            LogLine line = filterResult.get(i);
            
            if (firstScopeLine == null)
                firstScopeLine = line;

            if (i < filterResult.size() - 1) {
                LogLine next = filterResult.get(i+1);
                if (line.getScope() != next.getScope()) {
                    tableEntries.add(new TableLine(firstScopeLine, line.getLineNumber()));
                    firstScopeLine = next;
                }
            } else {
                tableEntries.add(new TableLine(firstScopeLine, line.getLineNumber()));
            }
        }
        return tableEntries;
    }
    
    private List<TableLine> showAllLines(List<LogLine> filterResult) {
        List<TableLine> tableEntries = new ArrayList<>();
        for (LogLine line : filterResult) {
            tableEntries.add(new TableLine(line));
        }
        return tableEntries;
    }
    
    public void loadLogFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() ||
                       f.getName().toLowerCase().endsWith(".txt") ||
                       f.getName().toLowerCase().endsWith(".log");
            }

            @Override
            public String getDescription() {
                return "Log files (*.txt, *.log)";
            }
        });
        fc.setCurrentDirectory(new File(NbPreferences.forModule(LogScene.class).get(PREFERENCE_DIR, "~")));
        fc.setDialogTitle("Load log file");

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            tryInterruptFilter();
            
            final File file = fc.getSelectedFile();
            
            NbPreferences.forModule(LogScene.class).put(PREFERENCE_DIR, file.getParent());
            
            lblIcon.setIcon(IMGICON_LOAD);
            logStatus = LogStatus.LOADING;
            new Thread(new Runnable(){

                @Override
                public void run() {
                    try {
                        logModel = parser.parse(file, new LoadProgressMonitor(lblMessage, "Loading file " + file.getName() + "..."));
                        lblIcon.setIcon(null);
                        lblMessage.setText("Current logfile: " + file.getName());
                        logStatus = LogStatus.ACTIVE;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            }).start();
        }
    }
    
    private class FilterKeyListener extends KeyAdapter {
        @Override
        public void keyTyped(KeyEvent e) {
            tryInterruptFilter();
            timeSinceLastKeystroke = 0;
        }
    }
    
    private class FilterFocusListener implements FocusListener {
        private boolean ignore = false;
        
        @Override
        public void focusGained(FocusEvent fe) {
            if (!ignore && logStatus == LogStatus.NO_LOG) {
                ignore = true;
                loadLogFile();
            }
        }

        @Override
        public void focusLost(FocusEvent fe) {
            if (fe instanceof CausedFocusEvent) {
                CausedFocusEvent cfe = (CausedFocusEvent) fe;
                ignore = ignore && // don't change ignore to true if it is already false
                         cfe.getCause() == CausedFocusEvent.Cause.ACTIVATION; // ACTIVATION is triggered if the
                                                                              // FileChooser is opened
            }
        }
        
    }
    
    private class KeystrokeTimer extends TimerTask {
        @Override
        public void run() {
            if (timeSinceLastKeystroke < 0)
                return;
            
            timeSinceLastKeystroke += KEYSTROKE_RATE;
            
            if (timeSinceLastKeystroke >= KEYSTROKE_TRESHOLD) {
                int save = timeSinceLastKeystroke;
                timeSinceLastKeystroke = -1; // needs to be set to -1 to prevent 
                                             // multiple parallel filter execution
                
                if (!executeFilters())
                    timeSinceLastKeystroke = save;
            }
        }
    }
    
    private class LoadProgressMonitor implements ProgressMonitor {
        private String staticText;
        private JLabel lblStatus;
        
        public LoadProgressMonitor(JLabel lblStatus, String staticText) {
            this.lblStatus = lblStatus;
            this.staticText = staticText;
            
            lblStatus.setText(staticText);
        }
        
        @Override
        public void worked(float percentage) {
            int perc = Math.round(percentage*100);
            lblStatus.setText(staticText + " (" + perc + " %)");
        }

        @Override
        public void finished() {
            if (parser.hasErrors()) {
                if (JOptionPane.showConfirmDialog(LogScene.this, "Parsing log file finished with errors. Show error messages?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    List<LogParser.ParseError> errors = parser.getErrors();
                    ImportLogErrorDialog.showDialog(LogScene.this, errors);
                }
            }
        }
        
    };
}
