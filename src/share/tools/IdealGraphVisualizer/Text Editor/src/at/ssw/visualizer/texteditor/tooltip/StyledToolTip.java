package at.ssw.visualizer.texteditor.tooltip;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.text.EditorKit;

/**
 *
 * @author Bernhard Stiftner
 * @author Christian Wimmer
 * @author Alexander Reder
 */
public class StyledToolTip extends JPanel {

    public static final int BORDER = 2;

    private final String text;
    private final JEditorPane toolTipPane;

    public StyledToolTip(String text, EditorKit editorKit) {
        this.text = text;

        setBackground(new Color(235, 235, 163));
        setBorder(new LineBorder(Color.BLACK));
        setOpaque(true);
        setLayout(new BorderLayout());

        toolTipPane = new JEditorPane();
        toolTipPane.setEditable(false);
        toolTipPane.setEditorKit(editorKit);
        toolTipPane.setText(text);

        add(toolTipPane, BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
        // Workaround: JEditorPane does not provide a proper width (only few pixels),
        //             so make the tooltip as large as the editor it is shown in
        Dimension prefSize = super.getPreferredSize();
        prefSize.width = Integer.MAX_VALUE;
        return prefSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Workaround: Add a new line at the end so that the caret is not in the visible area.
        toolTipPane.setText(text + "\n");

        super.paintComponent(g);
    }
}
