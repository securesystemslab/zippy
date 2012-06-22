/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ssw.visualizer.logviewer;

import com.oracle.graal.visualizer.editor.CompilationViewer;
import com.oracle.graal.visualizer.editor.SplitCompilationViewerFactory;
import com.sun.hotspot.igv.data.InputGraph;

public class LogCompilationViewerFactory extends SplitCompilationViewerFactory {

    @Override
    public String getName() {
        return "Log";
    }

    @Override
    protected CompilationViewer createViewer(InputGraph graph) {
        return new LogCompilationViewer();
    }

}
