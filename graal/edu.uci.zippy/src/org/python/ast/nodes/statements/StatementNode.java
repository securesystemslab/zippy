package org.python.ast.nodes.statements;

import org.python.ast.*;
import org.python.ast.nodes.FunctionRootNode;
import org.python.ast.nodes.PNode;

import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * New StatementNode replacing stmt
 * 
 * @author zwei
 * 
 */
public abstract class StatementNode extends PNode {

    /**
     * StatementNodes can form a linearized linked list.
     */
    private StatementNode next;

    protected FunctionRootNode funcRoot = null;

    protected StatementNode loopHeader = null;

    protected boolean isBreak = false;

    public void setBreak(boolean isBreak) {
        this.isBreak = isBreak;
    }

    public void setLoopHeader(StatementNode loopHeader) {
        this.loopHeader = loopHeader;
    }

    public boolean isBreak() {
        return isBreak;
    }

    protected boolean reachedBreak() {
        if (loopHeader != null) {
            return loopHeader.isBreak();
        } else {
            return false;
        }
    }

    public boolean reachedReturn() {
        if (funcRoot != null) {
            return funcRoot.reachedReturn();
        } else {
            return false;
        }
    }

    public void setFuncRootNode(FunctionRootNode funcRoot) {
        this.funcRoot = funcRoot;
    }

    public FunctionRootNode getFuncRootNode() {
        return this.funcRoot;
    }

    protected void setNext(StatementNode next) {
        this.next = next;
    }

    public StatementNode next() {
        assert this.next != null : "next is not initialized";
        return next;
    }

    public abstract void executeVoid(VirtualFrame frame);

    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitStatementNode(this);
    }

    @Override
    public void accept(PNodeVisitor visitor) {
        visitor.visitStatementNode(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
