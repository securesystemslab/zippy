package org.python.ast;

import java.math.BigInteger;
import java.util.Iterator;

import org.python.ast.datatypes.PComplex;
import org.python.ast.nodes.FunctionRootNode;
import org.python.ast.nodes.PNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.AddNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.DivNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.FloorDivNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.ModuloNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.MulNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.PowerNode;
import org.python.ast.nodes.expressions.BinaryArithmeticNode.SubNode;
import org.python.ast.nodes.statements.*;
import org.python.ast.nodes.literals.*;

import com.oracle.truffle.api.intrinsics.ExactMath;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;


public class PythonTreeOptimizer implements PNodeVisitor {

    public void optimize(RootNode rootNode) {
        traverseChildren(rootNode);
    }
    
    public void visitPNode(PNode node) {
    }

    public void visitStatementNode(StatementNode node) {
        traverseChildren(node);
    }
 
    
    public void visitAddNode(AddNode node) {
        Iterator<Node> iterator  = node.getChildren().iterator();
        // Iterator for next field in statement node
        iterator.next();
        Node leftNode = iterator.next();
        Node rightNode = iterator.next();
       
        if ((leftNode instanceof IntegerLiteralNode) && (rightNode instanceof IntegerLiteralNode)) {
            int leftValue = ((IntegerLiteralNode)leftNode).getValue();
            int rightValue = ((IntegerLiteralNode)rightNode).getValue();
            try {
                int resultValue = ExactMath.addExact(leftValue, rightValue);
                IntegerLiteralNode result = IntegerLiteralNodeFactory.create(resultValue);
                node.replace(result);
            } catch(ArithmeticException e) {
                BigInteger resultValue = BigInteger.valueOf(leftValue).add(BigInteger.valueOf(rightValue));
                BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
                node.replace(resultNode);
            }
        } else if (isLiteralBigIntegerType(leftNode) && isLiteralBigIntegerType(rightNode)) {
            BigInteger leftValue = getBigIntegerValueOfLiteral(leftNode);
            BigInteger rightValue = getBigIntegerValueOfLiteral(rightNode);
            BigInteger resultValue = leftValue.add(rightValue);
            BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralDoubleType(leftNode) && isLiteralDoubleType(rightNode)) {
            double leftValue = getDoubleValueOfLiteral(leftNode);
            double rightValue = getDoubleValueOfLiteral(rightNode);
            double resultValue = leftValue + rightValue;
            DoubleLiteralNode resultNode = DoubleLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralComplexType(leftNode) && isLiteralComplexType(rightNode)) {
            PComplex leftValue = getComplexValueOfLiteral(leftNode);
            PComplex rightValue =getComplexValueOfLiteral(rightNode);
            PComplex resultValue = leftValue.add(rightValue);
            ComplexLiteralNode resultNode = ComplexLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        }        
    }
    
    public void visitSubNode(SubNode node) {
        Iterator<Node> iterator  = node.getChildren().iterator();
        // Iterator for next field in statement node
        iterator.next();
        Node leftNode = iterator.next();
        Node rightNode = iterator.next();
       
        if ((leftNode instanceof IntegerLiteralNode) && (rightNode instanceof IntegerLiteralNode)) {
            int leftValue = ((IntegerLiteralNode)leftNode).getValue();
            int rightValue = ((IntegerLiteralNode)rightNode).getValue();
            try {
                int resultValue = ExactMath.subtractExact(leftValue, rightValue);
                IntegerLiteralNode result = IntegerLiteralNodeFactory.create(resultValue);
                node.replace(result);
            } catch(ArithmeticException e) {
                BigInteger resultValue = BigInteger.valueOf(leftValue).subtract(BigInteger.valueOf(rightValue));
                BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
                node.replace(resultNode);
            }
        } else if (isLiteralBigIntegerType(leftNode) && isLiteralBigIntegerType(rightNode)) {
            BigInteger leftValue = getBigIntegerValueOfLiteral(leftNode);
            BigInteger rightValue = getBigIntegerValueOfLiteral(rightNode);
            BigInteger resultValue = leftValue.subtract(rightValue);
            BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralDoubleType(leftNode) && isLiteralDoubleType(rightNode)) {
            double leftValue = getDoubleValueOfLiteral(leftNode);
            double rightValue = getDoubleValueOfLiteral(rightNode);
            double resultValue = leftValue - rightValue;
            DoubleLiteralNode resultNode = DoubleLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralComplexType(leftNode) && isLiteralComplexType(rightNode)) {
            PComplex leftValue = getComplexValueOfLiteral(leftNode);
            PComplex rightValue =getComplexValueOfLiteral(rightNode);
            PComplex resultValue = leftValue.sub(rightValue);
            ComplexLiteralNode resultNode = ComplexLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        }   
    }
    
    
    public void visitMulNode(MulNode node) {
        Iterator<Node> iterator  = node.getChildren().iterator();
        // Iterator for next field in statement node
        iterator.next();
        Node leftNode = iterator.next();
        Node rightNode = iterator.next();
       
        if ((leftNode instanceof IntegerLiteralNode) && (rightNode instanceof IntegerLiteralNode)) {
            int leftValue = ((IntegerLiteralNode)leftNode).getValue();
            int rightValue = ((IntegerLiteralNode)rightNode).getValue();
            try {
                int resultValue = ExactMath.multiplyExact(leftValue, rightValue);
                IntegerLiteralNode result = IntegerLiteralNodeFactory.create(resultValue);
                node.replace(result);
            } catch(ArithmeticException e) {
                BigInteger resultValue = BigInteger.valueOf(leftValue).multiply(BigInteger.valueOf(rightValue));
                BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
                node.replace(resultNode);
            }
        } else if (isLiteralBigIntegerType(leftNode) && isLiteralBigIntegerType(rightNode)) {
            BigInteger leftValue = getBigIntegerValueOfLiteral(leftNode);
            BigInteger rightValue = getBigIntegerValueOfLiteral(rightNode);
            BigInteger resultValue = leftValue.multiply(rightValue);
            BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralDoubleType(leftNode) && isLiteralDoubleType(rightNode)) {
            double leftValue = getDoubleValueOfLiteral(leftNode);
            double rightValue = getDoubleValueOfLiteral(rightNode);
            double resultValue = leftValue * rightValue;
            DoubleLiteralNode resultNode = DoubleLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralComplexType(leftNode) && isLiteralComplexType(rightNode)) {
            PComplex leftValue = getComplexValueOfLiteral(leftNode);
            PComplex rightValue =getComplexValueOfLiteral(rightNode);
            PComplex resultValue = leftValue.mul(rightValue);
            ComplexLiteralNode resultNode = ComplexLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        }   
    }
    
    public void visitDivNode(DivNode node) {
        Iterator<Node> iterator  = node.getChildren().iterator();
        // Iterator for next field in statement node
        iterator.next();
        Node leftNode = iterator.next();
        Node rightNode = iterator.next();
       
        if ((leftNode instanceof IntegerLiteralNode) && (rightNode instanceof IntegerLiteralNode)) {
            int leftValue = ((IntegerLiteralNode)leftNode).getValue();
            int rightValue = ((IntegerLiteralNode)rightNode).getValue();
            try {
                int resultValue = leftValue / rightValue;
                IntegerLiteralNode result = IntegerLiteralNodeFactory.create(resultValue);
                node.replace(result);
            } catch(ArithmeticException e) {
                BigInteger resultValue = BigInteger.valueOf(leftValue).divide(BigInteger.valueOf(rightValue));
                BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
                node.replace(resultNode);
            }
        } else if (isLiteralBigIntegerType(leftNode) && isLiteralBigIntegerType(rightNode)) {
            BigInteger leftValue = getBigIntegerValueOfLiteral(leftNode);
            BigInteger rightValue = getBigIntegerValueOfLiteral(rightNode);
            BigInteger resultValue = leftValue.divide(rightValue);
            BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralDoubleType(leftNode) && isLiteralDoubleType(rightNode)) {
            double leftValue = getDoubleValueOfLiteral(leftNode);
            double rightValue = getDoubleValueOfLiteral(rightNode);
            double resultValue = leftValue / rightValue;
            DoubleLiteralNode resultNode = DoubleLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralComplexType(leftNode) && isLiteralComplexType(rightNode)) {
            PComplex leftValue = getComplexValueOfLiteral(leftNode);
            PComplex rightValue =getComplexValueOfLiteral(rightNode);
            PComplex resultValue = leftValue.div(rightValue);
            ComplexLiteralNode resultNode = ComplexLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        }    
    }
    
    public void visitFloorDivNode(FloorDivNode node) {
        Iterator<Node> iterator  = node.getChildren().iterator();
        // Iterator for next field in statement node
        iterator.next();
        Node leftNode = iterator.next();
        Node rightNode = iterator.next();
       
        if ((leftNode instanceof IntegerLiteralNode) && (rightNode instanceof IntegerLiteralNode)) {
            int leftValue = ((IntegerLiteralNode)leftNode).getValue();
            int rightValue = ((IntegerLiteralNode)rightNode).getValue();
            try {
                int resultValue = leftValue / rightValue;
                IntegerLiteralNode result = IntegerLiteralNodeFactory.create(resultValue);
                node.replace(result);
            } catch(ArithmeticException e) {
                BigInteger resultValue = BigInteger.valueOf(leftValue).divide(BigInteger.valueOf(rightValue));
                BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
                node.replace(resultNode);
            }
        } else if (isLiteralBigIntegerType(leftNode) && isLiteralBigIntegerType(rightNode)) {
            BigInteger leftValue = getBigIntegerValueOfLiteral(leftNode);
            BigInteger rightValue = getBigIntegerValueOfLiteral(rightNode);
            BigInteger resultValue = leftValue.divide(rightValue);
            BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralDoubleType(leftNode) && isLiteralDoubleType(rightNode)) {
            double leftValue = getDoubleValueOfLiteral(leftNode);
            double rightValue = getDoubleValueOfLiteral(rightNode);
            double resultValue = Math.floor(leftValue / rightValue);
            DoubleLiteralNode resultNode = DoubleLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralComplexType(leftNode) && isLiteralComplexType(rightNode)) {
            PComplex leftValue = getComplexValueOfLiteral(leftNode);
            PComplex rightValue =getComplexValueOfLiteral(rightNode);
            PComplex resultValue = leftValue.div(rightValue);
            ComplexLiteralNode resultNode = ComplexLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        }    
    }
    
    public void visitModuloNode(ModuloNode node) {
        Iterator<Node> iterator  = node.getChildren().iterator();
        // Iterator for next field in statement node
        iterator.next();
        Node leftNode = iterator.next();
        Node rightNode = iterator.next();
       
        if ((leftNode instanceof IntegerLiteralNode) && (rightNode instanceof IntegerLiteralNode)) {
            int leftValue = ((IntegerLiteralNode)leftNode).getValue();
            int rightValue = ((IntegerLiteralNode)rightNode).getValue();
            try {
                int resultValue = leftValue % rightValue;
                IntegerLiteralNode result = IntegerLiteralNodeFactory.create(resultValue);
                node.replace(result);
            } catch(ArithmeticException e) {
                BigInteger resultValue = BigInteger.valueOf(leftValue).mod(BigInteger.valueOf(rightValue));
                BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
                node.replace(resultNode);
            }
        } else if (isLiteralBigIntegerType(leftNode) && isLiteralBigIntegerType(rightNode)) {
            BigInteger leftValue = getBigIntegerValueOfLiteral(leftNode);
            BigInteger rightValue = getBigIntegerValueOfLiteral(rightNode);
            BigInteger resultValue = leftValue.mod(rightValue);
            BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralDoubleType(leftNode) && isLiteralDoubleType(rightNode)) {
            double leftValue = getDoubleValueOfLiteral(leftNode);
            double rightValue = getDoubleValueOfLiteral(rightNode);
            double resultValue = leftValue % rightValue;
            DoubleLiteralNode resultNode = DoubleLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } 
        /**
         * TODO Complex is missing
         */
    }
    
    public void visitPowerNode(PowerNode node) {
        Iterator<Node> iterator  = node.getChildren().iterator();
        // Iterator for next field in statement node
        iterator.next();
        Node leftNode = iterator.next();
        Node rightNode = iterator.next();
       
        if ((leftNode instanceof IntegerLiteralNode) && (rightNode instanceof IntegerLiteralNode)) {
            int leftValue = ((IntegerLiteralNode)leftNode).getValue();
            int rightValue = ((IntegerLiteralNode)rightNode).getValue();
            try {
                int resultValue = (int) Math.pow(leftValue, rightValue);
                IntegerLiteralNode result = IntegerLiteralNodeFactory.create(resultValue);
                node.replace(result);
            } catch(ArithmeticException e) {
                double value =  Math.pow(BigInteger.valueOf(leftValue).doubleValue(), BigInteger.valueOf(rightValue).doubleValue());
                BigInteger resultValue = BigInteger.valueOf((long)value);
                BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
                node.replace(resultNode);
            }
        } else if (isLiteralBigIntegerType(leftNode) && isLiteralBigIntegerType(rightNode)) {
            BigInteger leftValue = getBigIntegerValueOfLiteral(leftNode);
            BigInteger rightValue = getBigIntegerValueOfLiteral(rightNode);
            double value =  Math.pow(leftValue.doubleValue(), rightValue.doubleValue());
            BigInteger resultValue = BigInteger.valueOf((long)value);
            BigIntegerLiteralNode resultNode = BigIntegerLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        } else if (isLiteralDoubleType(leftNode) && isLiteralDoubleType(rightNode)) {
            double leftValue = getDoubleValueOfLiteral(leftNode);
            double rightValue = getDoubleValueOfLiteral(rightNode);
            double resultValue = Math.pow(leftValue, rightValue);
            DoubleLiteralNode resultNode = DoubleLiteralNodeFactory.create(resultValue);
            node.replace(resultNode);
        }      
        /**
         * TODO Complex is missing
         */
    }
    
    private void traverseChildren(Node node) {
        Iterator<Node> iterator  = node.getChildren().iterator();
        while(iterator.hasNext()) {
            Node child = iterator.next();
            /**
             * First child of statementNodes are null because of next  
             */
            if (child != null) {
                if (child instanceof FunctionRootNode) {
                    traverseChildren(child);
                } else {
                    PNode pchild = (PNode) child;
                    pchild.accept(this);
                }
            }
        }   
    }

    private boolean isLiteralBigIntegerType(Node node) {
        if (node instanceof IntegerLiteralNode 
           || node instanceof BigIntegerLiteralNode) {
            return true;
        }
        
        return false;
    }
    
    private boolean isLiteralDoubleType(Node node) {
        if (node instanceof IntegerLiteralNode 
            || node instanceof BigIntegerLiteralNode
            || node instanceof DoubleLiteralNode) {
            return true;
        }
        
        return false;
    }
    
    private boolean isLiteralComplexType(Node node) {
        if (node instanceof IntegerLiteralNode 
            || node instanceof BigIntegerLiteralNode
            || node instanceof DoubleLiteralNode
            || node instanceof ComplexLiteralNode) {
            return true;
        }
        
        return false;
    }
    
    private BigInteger getBigIntegerValueOfLiteral(Node node) {
        if (node instanceof IntegerLiteralNode) {
            int value = ((IntegerLiteralNode)node).getValue();
            return BigInteger.valueOf(value);
        } else if (node instanceof BigIntegerLiteralNode) {
            BigInteger value = ((BigIntegerLiteralNode)node).getValue();
            return value;
        } else {
            throw new RuntimeException("Unexpected literal node: " + node);
        }
    }
    
    private double getDoubleValueOfLiteral(Node node) {
        if (node instanceof IntegerLiteralNode) {
            int value = ((IntegerLiteralNode)node).getValue();
            return value;
        } else if (node instanceof BigIntegerLiteralNode) {
            BigInteger value = ((BigIntegerLiteralNode)node).getValue();
            return value.doubleValue();
        } else if (node instanceof DoubleLiteralNode) {
            double value = ((DoubleLiteralNode)node).getValue();
            return value;
        }  else {
            throw new RuntimeException("Unexpected literal node: " + node);
        }
    }
    
    private PComplex getComplexValueOfLiteral(Node node) {
        if (node instanceof IntegerLiteralNode) {
            int value = ((IntegerLiteralNode)node).getValue();
            return new PComplex(value, 0);
        } else if (node instanceof BigIntegerLiteralNode) {
            BigInteger value = ((BigIntegerLiteralNode)node).getValue();
            return new PComplex(value.doubleValue(), 0);
        } else if (node instanceof DoubleLiteralNode) {
            double value = ((DoubleLiteralNode)node).getValue();
            return new PComplex(value, 0);
        }  else if (node instanceof ComplexLiteralNode) {
            PComplex value = ((ComplexLiteralNode)node).getValue();
            return value;
        }  else {
            throw new RuntimeException("Unexpected literal node: " + node);
        }
    }
    
}
