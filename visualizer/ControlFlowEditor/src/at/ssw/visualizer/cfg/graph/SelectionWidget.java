package at.ssw.visualizer.cfg.graph;

import at.ssw.visualizer.cfg.preferences.CfgPreferences;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;


public class SelectionWidget extends Widget {
    public SelectionWidget(Scene scene) {
        super(scene);
    }
    
    public static void renderSelectedRect(Graphics2D gr, Rectangle rect) {
        if (rect == null) {
            return;
        }
        gr.setColor(CfgPreferences.getInstance().getSelectionColorBackground());
        gr.fillRect(rect.x, rect.y, rect.width, rect.height);
        gr.setColor(CfgPreferences.getInstance().getSelectionColorForeground());
    }

    public void renderSelectionRectangle(Graphics2D gr, Rectangle selectionRectangle) {
        Composite composite = gr.getComposite();
        gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
       
        renderSelectedRect(gr, selectionRectangle);
        gr.setComposite(composite);
    }
    
    @Override
    public void paintWidget(){       
        this.renderSelectionRectangle(this.getGraphics(), this.getBounds());
    }

}
