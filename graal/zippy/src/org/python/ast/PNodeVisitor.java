package org.python.ast;

import org.python.ast.nodes.PNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.AddNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.DivNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.FloorDivNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.ModuloNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.MulNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.PowerNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.SubNode;
import org.python.ast.nodes.statements.*;


public interface PNodeVisitor {
    
    public void visitPNode(PNode node);
        
    public void visitStatementNode(StatementNode node);
    
    public void visitAddNode(AddNode node);
    
    public void visitSubNode(SubNode node);
    
    public void visitMulNode(MulNode node);
    
    public void visitDivNode(DivNode node);
    
    public void visitFloorDivNode(FloorDivNode node);
    
    public void visitModuloNode(ModuloNode node);
    
    public void visitPowerNode(PowerNode node);

}
