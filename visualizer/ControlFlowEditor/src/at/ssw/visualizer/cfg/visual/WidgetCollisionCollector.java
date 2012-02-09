package at.ssw.visualizer.cfg.visual;

import java.util.List;
import org.netbeans.api.visual.widget.Widget;


public interface WidgetCollisionCollector {
    
    //returns a list of widgets which should be handled as obstacles
    void collectCollisions(List<Widget> collisions);
      
}
