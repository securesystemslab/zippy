package at.ssw.visualizer.cfg.action;

import at.ssw.visualizer.cfg.editor.CfgEditorTopComponent;
import at.ssw.visualizer.cfg.graph.CfgScene;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 * Exports Scene to various Graphics Formats
 * 
 * @author Rumpfhuber Stefan
 */
public class ExportAction extends AbstractCfgEditorAction {
    
    public static final String DESCRIPTION_SVG = "Scaleable Vector Format (.svg)";
    public static final String DESCRIPTION_EMF = "Extended Meta File (.emf)";
    public static final String DESCRIPTION_GIF = "Graphics Interchange Format (.gif)";    
    public static final String EXT_GIF = "gif", EXT_EMF = "emf", EXT_SVG = "svg";

    String lastDirectory = null;
    
    @Override
    public void performAction() {       
        CfgEditorTopComponent tc = this.getEditor();
        CfgScene scene = tc.getCfgScene();
        JComponent view = scene.getView();
           
        JFileChooser chooser = new JFileChooser ();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle (getName());
        chooser.setDialogType (JFileChooser.SAVE_DIALOG);
        chooser.setMultiSelectionEnabled (false);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);              
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(DESCRIPTION_SVG, EXT_SVG));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(DESCRIPTION_EMF, EXT_EMF));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(DESCRIPTION_GIF, EXT_GIF));
        if(lastDirectory != null)
            chooser.setCurrentDirectory(new File(lastDirectory));
        chooser.setSelectedFile(new File(tc.getName()));
        
                              
        if (chooser.showSaveDialog (tc) != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile ();
                
        if(file == null)
            return;
                
        FileNameExtensionFilter filter = (FileNameExtensionFilter) chooser.getFileFilter();
        String fn = file.getAbsolutePath().toLowerCase();
        String ext = filter.getExtensions()[0];
        if(!fn.endsWith("." + ext)){
            file = new File( file.getParentFile(), file.getName() + "." + ext);
        }
                        
        if (file.exists ()) {
            DialogDescriptor descriptor = new DialogDescriptor (
                "File (" + file.getAbsolutePath () + ") already exists. Do you want to overwrite it?",
                "File Exists", true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null);
                DialogDisplayer.getDefault ().createDialog (descriptor).setVisible (true);
                    if (descriptor.getValue () != DialogDescriptor.YES_OPTION)
                        return;
        }   
        
        lastDirectory = chooser.getCurrentDirectory().getAbsolutePath();
                        
        /*if(ext.equals(EXT_SVG)){                   
            SVGGraphics2D g;
            try {                  
                g = new SVGGraphics2D(file, scene.getBounds().getSize());                   
                g.startExport();                
                scene.paint(g);
                g.endExport();
            } catch(Exception ex){               
            }
        } else if (ext.equals(EXT_EMF)) {                 
            EMFGraphics2D g;   
            try {                                                                   
                g = new EMFGraphics2D(file, scene.getBounds().getSize());                       
                g.startExport();         
                scene.paint(g);
                g.endExport();                     
            } catch(Exception ex){                
            }
        } else if (ext.equals(EXT_GIF)) { 
            GIFGraphics2D g;
            try {  
                //use the view as image source to get a image with the current zoomfactor
                //using the scene.paint() would always return a image with zoomfactor 1.0
                g = new GIFGraphics2D(file, view);
                g.startExport();
                view.print(g);                     
                g.endExport();   
            } catch(Exception ex){
            }
        }  */
        // TODO(tw): Add SVG export.
    }          
       
    
    @Override
    public String getName() {     
        return "Export CFG";
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
     @Override
    protected String iconResource() {
        return "at/ssw/visualizer/cfg/icons/disk.gif";    
    }

}
