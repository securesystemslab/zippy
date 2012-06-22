package at.ssw.visualizer.logviewer.scene;

import at.ssw.visualizer.logviewer.model.LogLine;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author Katrin Strassl
 */
public class BookmarkDialog extends JDialog {
    
    private JList lstBookmarks;
    private List<LogLine> logLines;
    
    public BookmarkDialog(List<LogLine> bookmarks, Window parent) {
        super(parent, JDialog.ModalityType.APPLICATION_MODAL);
        
        this.logLines = bookmarks;
        
        DefaultListModel mdlBookmarks = new DefaultListModel();
        lstBookmarks = new JList(mdlBookmarks);
        JScrollPane jspBookmarks = new JScrollPane(lstBookmarks);
        
        for (LogLine bookmark : bookmarks) {
            mdlBookmarks.addElement(bookmark.getLineNumber() + ": Method " + bookmark.getMethod() + " - Scope " + bookmark.getScope() + " - " + bookmark.getText());
        }
        
        JButton btnOk = new JButton("Go to");
        JButton btnCancel = new JButton("Cancel");

        // init listeners
        lstBookmarks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    close();
                }
            }
        });
        
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (lstBookmarks.isSelectionEmpty())
                    return;
                close();
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                lstBookmarks.clearSelection();
                close();
            }
        });
        
        // build layout
        JPanel pnlButtons = new JPanel();
        
        pnlButtons.setLayout(new GridLayout(1, 2));
        pnlButtons.add(btnOk);
        pnlButtons.add(btnCancel);
        
        this.setLayout(new BorderLayout());
        this.add(jspBookmarks, BorderLayout.CENTER);
        this.add(pnlButtons, BorderLayout.SOUTH);
        
        this.pack();
        this.setMinimumSize(new Dimension(600, this.getPreferredSize().height));
        this.setLocationRelativeTo(parent);
        this.setResizable(false);
        this.setVisible(true);
    }
    
    public LogLine getTarget() {
        if (lstBookmarks.isSelectionEmpty())
            return null;
        return logLines.get(lstBookmarks.getSelectedIndex());
    }
    
    private void close() {
        this.setVisible(false);
        this.dispose();
    }
}
