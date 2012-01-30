package at.ssw.visualizer.cfg.editor;

import at.ssw.visualizer.model.cfg.ControlFlowGraph;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import org.openide.cookies.OpenCookie;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;


public class CfgEditorSupport extends CloneableOpenSupport implements OpenCookie  {
    private ControlFlowGraph cfg;

    public CfgEditorSupport(ControlFlowGraph cfg) {
        super(new Env());
        ((Env) env).editorSupport = this;
        this.cfg = cfg;
    }

    protected CloneableTopComponent createCloneableTopComponent() {    
         return new CfgEditorTopComponent(cfg);       
    }

    public String messageOpened() {
        return "Opened " + cfg.getCompilation().getMethod() + " - " + cfg.getName();
    }

    public String messageOpening() {
        return "Opening " + cfg.getCompilation().getMethod() + " - " + cfg.getName();
    }
    
 
    public static class Env implements CloneableOpenSupport.Env {
        private PropertyChangeSupport prop = new PropertyChangeSupport(this);
        private VetoableChangeSupport veto = new VetoableChangeSupport(this);
        private CfgEditorSupport editorSupport;

        public boolean isValid() {
            return true;
        }

        public boolean isModified() {
            return false;
        }

        public void markModified() throws IOException {
            throw new IOException("Editor is readonly");
        }

        public void unmarkModified() {
            // Nothing to do.
        }

        public CloneableOpenSupport findCloneableOpenSupport() {
            return editorSupport;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            prop.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            prop.removePropertyChangeListener(l);
        }

        public void addVetoableChangeListener(VetoableChangeListener l) {
            veto.addVetoableChangeListener(l);
        }

        public void removeVetoableChangeListener(VetoableChangeListener l) {
            veto.removeVetoableChangeListener(l);
        }
    }
}
