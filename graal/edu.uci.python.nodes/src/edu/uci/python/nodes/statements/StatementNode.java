/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.nodes.statements;


import edu.uci.python.nodes.*;
import edu.uci.python.nodes.translation.*;

/**
 * New StatementNode replacing stmt.
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

    public final boolean isBreak() {
        return isBreak;
    }

    protected final boolean reachedBreak() {
        if (loopHeader != null) {
            return loopHeader.isBreak();
        } else {
            return false;
        }
    }

    public final boolean reachedReturn() {
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
