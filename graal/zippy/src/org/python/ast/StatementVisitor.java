package org.python.ast;

import org.python.ast.nodes.statements.*;

public interface StatementVisitor<R> {

    public R visitStatementNode(StatementNode node);

    public R visitBlockNode(BlockNode node);

    public R visitForNode(ForNode node);

    public R visitIfNode(IfNode node);
    
    public R visitIfNotNode(IfNotNode ifNotNode);

    public R visitWhileNode(WhileNode node);
    
    public R visitWhileTrueNode(WhileTrueNode node);

}
