package at.ssw.visualizer.logviewer.scene;

import at.ssw.visualizer.logviewer.model.LogLine;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;

/**
 *
 * @author Katrin Strassl
 */
public class BookmarkableLogViewer extends JPanel {

    private static final Color BOOKMARK_COLOR = Color.CYAN;
    
    private static final Highlighter.HighlightPainter BOOKMARK_PAINTER =
            new DefaultHighlighter.DefaultHighlightPainter(BOOKMARK_COLOR);
    
    private JTextPane txpText;
    private JTextPane txpLines;
    private Highlighter textHighlighter;
    private Highlighter lineHighlighter;
    private FontMetrics fm;
    private Map<Integer, Bookmark> bookmarks = new TreeMap<>();
    private List<LogLine> logLines = new ArrayList<>();

    public BookmarkableLogViewer() {
        init();
    }

    private void init() {
        txpText = new JTextPane();
        txpLines = new JTextPane();

        // needed for correct layouting
        Insets ins = txpLines.getInsets();
        txpLines.setMargin(new Insets(ins.top + 1, ins.left, ins.bottom, ins.right));

        textHighlighter = new BookmarkHighlighter();
        lineHighlighter = new BookmarkHighlighter();

        txpText.setHighlighter(textHighlighter);
        //txpText.setMinimumSize(new Dimension(100, 100));
        txpText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txpText.setEditable(false);

        txpLines.setHighlighter(lineHighlighter);
        txpLines.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txpLines.setBackground(Color.LIGHT_GRAY);
        txpLines.setEnabled(false);
        txpLines.setForeground(Color.BLACK);
        txpLines.addMouseListener(mouseInputListener);

        fm = txpText.getFontMetrics(txpText.getFont());

        JPanel pnlBookmarks = new JPanel();
        pnlBookmarks.setLayout(new BorderLayout());
        pnlBookmarks.add(txpText, BorderLayout.CENTER);

        JScrollPane jspBookmarks = new JScrollPane(pnlBookmarks);
        jspBookmarks.setRowHeaderView(txpLines);
        jspBookmarks.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jspBookmarks.getVerticalScrollBar().setUnitIncrement(15);

        this.setLayout(new BorderLayout());
        this.add(jspBookmarks, BorderLayout.CENTER);
    }
   
    public int getCurrentlyDisplayedTopLine() {
        Rectangle view = txpText.getVisibleRect();
        return getAbsoluteLine((int) Math.ceil(view.y/fm.getHeight()));
    }
    
    public int getCurrentTopLine() {
        return logLines.get(0).getLineNumber();
    }
    
    public int getCurrentBottomLine() {
        return logLines.get(logLines.size()-1).getLineNumber();
    }

    public void clearLogLines() {
        this.logLines = new ArrayList<>();
        txpText.setText("");
        txpLines.setText("");
        clearHighlighter();
    }

    public void setLogLines(List<LogLine> logLines) {
        this.logLines = logLines;

        clearHighlighter();
        clearLineNumbers();
        String text = "";
        for (int i = 0; i < logLines.size(); i++) {
            text += logLines.get(i).getText();
            if (i < logLines.size() - 1) {
                text += "\n";
            }
        }
        txpText.setText(text);
        setLineNumbers();
   
        txpText.setCaretPosition(0);
        txpLines.setCaretPosition(0);

        for (int bookmark : bookmarks.keySet()) {
            if (bookmark < logLines.get(0).getLineNumber()) {
                continue;
            }
            if (bookmark > logLines.get(logLines.size() - 1).getLineNumber()) {
                break;
            }

            int line = getRelativeLine(bookmark);

            addHighlight(logLines.get(line));
        }
    }

    public void setLogLines(List<LogLine> logLines, int focus) {
        setLogLines(logLines);
        
        //does not work for some reason - jtextpane jumps to caret position afterwards
        //moveToBookmark(focus);
        
        //workaround - set caret position bevore moving
        Rectangle visible = txpText.getVisibleRect();
        int visibleLines = (int)(Math.ceil(visible.height/fm.getHeight()));

        String text = txpText.getText();
        int line = logLines.get(0).getLineNumber();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n')
                line++;
            if (line == focus+visibleLines || i == text.length() - 1) {
                txpText.setCaretPosition(i);
                break;
            }
        }
        moveToBookmark(focus);
    }

    public List<LogLine> getBookmarkedLines() {
        List<LogLine> lines = new ArrayList<>();
        for (int bookmark : bookmarks.keySet()) {
            lines.add(bookmarks.get(bookmark).logLine);
        }
        return lines;
    }

    public int tryLastBookmark() {
        int line = getCurrentlyDisplayedTopLine();
        
        int firstShownLine = getCurrentTopLine();

        int bookmarkKey = -1;
        for (int key : bookmarks.keySet()) {
            if (key >= line) {
                break;
            }

            bookmarkKey = key;
        }

        if (bookmarkKey >= firstShownLine) {
            moveToBookmark(bookmarkKey);
            return -1;
        }

        return bookmarkKey;
    }

    public int tryNextBookmark() {
        Rectangle visible = txpText.getVisibleRect();
        int lastLine = (int) Math.floor((visible.y + visible.height) / fm.getHeight());
        lastLine = getAbsoluteLine(lastLine);

        int lastShownLine = getCurrentBottomLine();

        int bookmarkKey = -1;
        for (int key : bookmarks.keySet()) {
            if (key > lastLine) {
                bookmarkKey = key;
                break;
            }
            if (key > lastShownLine) {
                break;
            }
        }

        if (bookmarkKey > 0 && bookmarkKey <= lastShownLine) {
            moveToBookmark(bookmarkKey);
            return -1;
        }

        return bookmarkKey;
    }

    private void moveToBookmark(int line) {
        int relLine = getRelativeLine(line);
        Rectangle visible = txpText.getVisibleRect();
        Rectangle position = new Rectangle(0, relLine * fm.getHeight(), visible.width, visible.height);
        txpText.scrollRectToVisible(position);
    }

    private int getAbsoluteLine(int line) {
        return logLines.get(0).getLineNumber() + line;
    }

    private int getRelativeLine(int line) {
        return line - logLines.get(0).getLineNumber();
    }

    private int getStartOffset(JTextComponent component, int line) {
        return component.getDocument().getDefaultRootElement().getElement(line).getStartOffset();
    }

    private int getEndOffset(JTextComponent component, int line) {
        return component.getDocument().getDefaultRootElement().getElement(line).getEndOffset();
    }

    private void addHighlight(LogLine logLine) {
        try {
            int line = logLine.getLineNumber();
            int relativeLine = getRelativeLine(line);

            Object hl1 = textHighlighter.addHighlight(getStartOffset(txpText, relativeLine), getEndOffset(txpText, relativeLine), BOOKMARK_PAINTER);
            Object hl2 = lineHighlighter.addHighlight(getStartOffset(txpLines, relativeLine), getEndOffset(txpLines, relativeLine), BOOKMARK_PAINTER);
            bookmarks.put(line, new Bookmark(logLine, hl1, hl2));
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void removeHighlight(int line) {
        int abs = getAbsoluteLine(line);
        Bookmark bookmark = bookmarks.get(abs);
        textHighlighter.removeHighlight(bookmark.hl1);
        lineHighlighter.removeHighlight(bookmark.hl2);
        bookmarks.remove(abs);
    }

    public void clearBookmarks() {
        bookmarks = new TreeMap<>();
        clearHighlighter();
    }

    private void clearHighlighter() {
        textHighlighter.removeAllHighlights();
        lineHighlighter.removeAllHighlights();
    }

    private void clearLineNumbers() {
        txpLines.setText("");
    }

    private void setLineNumbers() {
        clearLineNumbers();

        if (logLines.isEmpty()) {
            return;
        }

        int colCnt = String.valueOf(logLines.get(logLines.size() - 1).getLineNumber()).length() + 2;

        int firstNr = logLines.get(0).getLineNumber();
        String text = String.format("%" + colCnt + "s", firstNr + " ");
        for (int i = 1; i < logLines.size(); i++) {
            text += "\n";
            text += String.format("%" + colCnt + "s", (firstNr + i) + " ");
        }
        txpLines.setText(text);
    }

    private MouseListener mouseInputListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent me) {
            if (txpLines.getText().length() == 0) {
                return;
            }
            if (me.getClickCount() == 2) {
                int caretPos = txpLines.getCaretPosition();

                int lineOffset = txpLines.getDocument().getDefaultRootElement().getElementIndex(caretPos);
                if (txpLines.getText().charAt(caretPos - 1) == '\n') {
                    lineOffset--;
                }

                if (bookmarks.containsKey(getAbsoluteLine(lineOffset))) {
                    removeHighlight(lineOffset);
                } else {
                    addHighlight(logLines.get(lineOffset));
                }
            }
        }
    };

    class Bookmark {

        public final LogLine logLine;
        public final Object hl1;
        public final Object hl2;

        public Bookmark(LogLine logLine, Object hl1, Object hl2) {
            this.logLine = logLine;
            this.hl1 = hl1;
            this.hl2 = hl2;
        }
    }

    class BookmarkHighlighter extends DefaultHighlighter {

        private JTextComponent component;

        @Override
        public void install(JTextComponent component) {
            super.install(component);
            this.component = component;
        }

        @Override
        public void deinstall(JTextComponent component) {
            super.deinstall(component);
            this.component = null;
        }

        @Override
        public void paint(Graphics g) {
            Highlighter.Highlight[] highlights = getHighlights();

            for (int i = 0; i < highlights.length; i++) {
                Highlighter.Highlight hl = highlights[i];
                Rectangle bg = component.getBounds();
                Insets insets = component.getInsets();
                bg.x = insets.left;
                bg.y = insets.top;
                bg.height = insets.top + insets.bottom;
                Highlighter.HighlightPainter painter = hl.getPainter();
                painter.paint(g, hl.getStartOffset(), hl.getEndOffset(), bg, component);
            }
        }
    }
}
