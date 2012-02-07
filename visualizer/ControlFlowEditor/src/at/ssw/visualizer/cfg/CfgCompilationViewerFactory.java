/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ssw.visualizer.cfg;

import com.oracle.graal.visualizer.editor.CompilationViewer;
import com.oracle.graal.visualizer.editor.CompilationViewerFactory;
import com.oracle.graal.visualizer.editor.DiagramViewModel;
import com.oracle.graal.visualizer.editor.SplitCompilationViewerFactory;
import com.sun.hotspot.igv.data.InputGraph;

public class CfgCompilationViewerFactory extends SplitCompilationViewerFactory {

    @Override
    public String getName() {
        return "CFG";
    }

    @Override
    protected CompilationViewer createViewer(InputGraph graph) {
        return new CfgCompilationViewer(graph);
    }

}
