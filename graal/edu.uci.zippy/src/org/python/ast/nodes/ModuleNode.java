package org.python.ast.nodes;

import org.python.ast.nodes.statements.StatementNode;
import org.python.core.truffle.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

public class ModuleNode extends RootNode implements Visualizable {

    @Children
    StatementNode[] body;

    private final FrameDescriptor descriptor;

    public ModuleNode(StatementNode[] body, FrameDescriptor descriptor) {
        this.body = adoptChildren(body);
        this.descriptor = descriptor;
    }

    public FrameDescriptor getFrameDescriptor() {
        return descriptor;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        GlobalScope.getInstance().setCachedGlobalFrame(frame);

        for (int i = 0; i < body.length; i++) {
            body[i].executeVoid(frame);
        }

        return null;
    }
    
    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);
        
        level++;
        
        for (StatementNode s : body) {
            s.visualize(level);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
