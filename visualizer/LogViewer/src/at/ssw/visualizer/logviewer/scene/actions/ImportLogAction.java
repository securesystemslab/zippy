package at.ssw.visualizer.logviewer.scene.actions;

import at.ssw.visualizer.logviewer.scene.LogScene;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;


/**
 *
 * @author Katrin Strassl
 */

@ActionID(id = "at.ssw.visualizer.logviewer.scene.actions.ImportLogActions", category = "File")
@ActionRegistration(displayName = "Import Log", iconBase = "at/ssw/visualizer/logviewer/scene/icons/import_log.png")
@ActionReference(path = "Menu/File", position = 600)
public class ImportLogAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent ae) {
        LogScene.getInstance().loadLogFile();
    }
}
