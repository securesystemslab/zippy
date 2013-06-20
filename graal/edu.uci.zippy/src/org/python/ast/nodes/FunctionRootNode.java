package org.python.ast.nodes;

import org.python.ast.nodes.statements.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

/**
 * RootNode of a Python Function body. It should invoked by a CallTarget Object.
 * 
 * @author zwei
 * 
 */
public class FunctionRootNode extends RootNode implements Visualizable {

    @Child
    protected ParametersNode parameters;

    @Child
    protected StatementNode body;
    
    private boolean reachedReturn = false;
    
    private Object returnVal = null;

    public FunctionRootNode(ParametersNode parameters, StatementNode body) {
        this.parameters = adoptChild(parameters);
        this.body = adoptChild(body);
    }
    
    public void setBody(StatementNode body) {
        this.body = adoptChild(body);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        parameters.executeVoid(frame);

        //try {
        body.executeVoid(frame);
        //} catch (ExplicitReturnException ere) {
            //return ere.getValue();
        //} catch (ImplicitReturnException ire) {

        //}

        //return null;
        this.reachedReturn = false;
        return this.returnVal;
        //return body.getPropogatedValue();
    }
    
    public boolean reachedReturn() {
        return this.reachedReturn;
    }
    
    public void setReturn(boolean reachedReturn, Object returnVal) {
        this.reachedReturn = reachedReturn;
        this.returnVal = returnVal;
    }
    
    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);
        
        level++;
        parameters.visualize(level);
        body.visualize(level);
    }
    
}
