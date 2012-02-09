package at.ssw.visualizer.cfg.preferences;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * A color selection button. It will preview the currently selected color as
 * an icon. Clicking the button will bring up the JColorChooser dialog.
 *
 * @author Bernhard Stiftner
 */
public class ColorChooserButton extends JButton implements ActionListener {

    public static final Dimension ICON_SIZE = new Dimension(24, 8);

    Color color;
    boolean colorChooserEnabled = true; // bring up dialog when clicked?


    public ColorChooserButton() {
        this(Color.black);
    }

    public ColorChooserButton(Color defaultColor) {
        setIcon(new ColorBoxIcon());
        addActionListener(this);
        color = defaultColor;
        Dimension size = new Dimension(ICON_SIZE);
        size.width += getInsets().left + getInsets().right;
        size.height += getInsets().top + getInsets().bottom;
        setPreferredSize(size);
    }

    public void setColor(Color newColor) {
        color = newColor;
        repaint();
    }

    public Color getColor() {
        return color;
    }

    public boolean isColorChooserEnabled() {
        return colorChooserEnabled;
    }

    public void setColorChooserEnabled(boolean enabled) {
        this.colorChooserEnabled = enabled;
    }

    public void actionPerformed(ActionEvent e) {
        if (!colorChooserEnabled) {
            return;
        }

        Color c = JColorChooser.showDialog(this, "Choose color", color);
        if (c != null) {
            setColor(c);
        }
    }

    class ColorBoxIcon implements Icon {

        public int getIconWidth() {
            return ICON_SIZE.width;
        }

        public int getIconHeight() {
            return ICON_SIZE.height;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color oldColor = g.getColor();
            g.translate(x, y);

            g.setColor(color);
            g.fillRect(0, 0, ICON_SIZE.width, ICON_SIZE.height);

            g.setColor(Color.black);
            g.drawRect(0, 0, ICON_SIZE.width, ICON_SIZE.height);

            g.translate(-x, -y);
            g.setColor(oldColor);
        }
    }
}

