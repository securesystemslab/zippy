package org.python.antlr.ast;

import org.python.antlr.PythonTree;
import org.python.antlr.base.mod;
import org.python.core.Py;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ErrorMod extends mod {

    public ErrorMod(PythonTree tree) {
        super(tree);
    }

    public String toString() {
        return "ErrorMod";
    }

    public String toStringTree() {
        return "ErrorMod";
    }

    public int getLineno() {
        return getLine();
    }

    public int getCol_offset() {
        return getCharPositionInLine();
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return null;
    }

    public void traverse(VisitorIF visitor) throws Exception {
        // no op.
    }

    @Override
    public Object execute(VirtualFrame frame) {
        throw Py.SystemError("Not implemented!");
    }
}
