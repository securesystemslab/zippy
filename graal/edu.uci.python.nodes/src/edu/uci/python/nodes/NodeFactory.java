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
package edu.uci.python.nodes;

import java.math.*;
import java.util.*;
import java.util.List;
import java.util.Set;

import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;

import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.generator.ComprehensionNodeFactory.MapPutNodeFactory;
import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.nodes.subscript.*;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.nodes.expression.BinaryBooleanNodeFactory.*;
import edu.uci.python.nodes.expression.BinaryArithmeticNodeFactory.*;
import edu.uci.python.nodes.expression.BinaryComparisonNodeFactory.*;
import edu.uci.python.nodes.expression.CastToBooleanNodeFactory.*;
import edu.uci.python.nodes.expression.BinaryBitwiseNodeFactory.*;
import edu.uci.python.nodes.expression.UnaryArithmeticNodeFactory.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.standardtype.*;

public class NodeFactory {

    @CompilationFinal private static NodeFactory factory;

    public static NodeFactory getInstance() {
        if (factory == null) {
            factory = new NodeFactory();
        }
        return factory;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public <T> T duplicate(Node orig, Class<T> clazz) {
        return (T) NodeUtil.cloneNode(orig);
    }

    public RootNode createModule(String name, List<PNode> body, FrameDescriptor fd) {
        PNode block = createBlock(body);
        return new ModuleNode(name, block, fd);
    }

    public FunctionRootNode createFunctionRoot(PythonContext context, String functionName, boolean isGenerator, FrameDescriptor frameDescriptor, PNode body) {
        return new FunctionRootNode(context, functionName, isGenerator, frameDescriptor, body);
    }

    public ClassDefinitionNode createClassDef(PythonContext context, String name, PNode[] baseClasses, FunctionDefinitionNode definitnionFunction) {
        return ClassDefinitionNodeFactory.create(context, name, baseClasses, definitnionFunction);
    }

    public PNode createBlock(List<PNode> statements) {
        PNode[] array = statements.toArray(new PNode[statements.size()]);
        return createBlock(array);
    }

    public PNode createBlock(PNode... statements) {
        return BlockNode.create(statements);
    }

    public PNode createImport(PythonContext context, String importee) {
        return new ImportNode(context, importee);
    }

    public PNode createImportFrom(PythonContext context, PythonModule relativeto, String fromModuleName, String importee) {
        return new ImportFromNode(context, relativeto, fromModuleName, importee);
    }

    public PNode createImportStar(PythonContext context, PythonModule relativeto, String fromModuleName) {
        return new ImportStarNode(context, relativeto, fromModuleName);
    }

    public LoopNode createWhile(CastToBooleanNode condition, PNode body) {
        return new WhileNode(condition, body);
    }

    public StatementNode createIf(CastToBooleanNode condition, PNode thenPart, PNode elsePart) {
        return IfNode.create(condition, thenPart, elsePart);
    }

    public GetIteratorNode createGetIterator(PNode collection) {
        return GetIteratorNodeFactory.create(collection);
    }

    public StatementNode createElse(StatementNode then, PNode orelse) {
        return new ElseNode(then, orelse);
    }

    public StatementNode createReturn() {
        return new ReturnNode();
    }

    public StatementNode createFrameReturn(PNode value) {
        return new ReturnNode.FrameReturnNode(value);
    }

    public StatementNode createBreak() {
        return new BreakNode();
    }

    public StatementNode createContinue() {
        return new ContinueNode();
    }

    public StatementNode createContinueTarget(PNode child) {
        return new ContinueTargetNode(child);
    }

    public StatementNode createBreakTarget(StatementNode child) {
        return new BreakTargetNode(child);
    }

    public StatementNode createYield(PNode right, FrameSlot returnSlot) {
        return new YieldNode(createWriteLocal(right, returnSlot));
    }

    public StatementNode createPrint(List<PNode> values, boolean nl, PythonContext context) {
        return new PrintNode(values.toArray(new PNode[values.size()]), nl, context);
    }

    public PNode createIntegerLiteral(int value) {
        return new IntegerLiteralNode(value);
    }

    public PNode createBigIntegerLiteral(BigInteger value) {
        return new BigIntegerLiteralNode(value);
    }

    public PNode createDoubleLiteral(double value) {
        return new DoubleLiteralNode(value);
    }

    public PNode createComplexLiteral(PComplex value) {
        return new ComplexLiteralNode(value);
    }

    public PNode createStringLiteral(String value) {
        return new StringLiteralNode(value);
    }

    public PNode createDictLiteral(List<PNode> keys, List<PNode> values) {
        PNode[] convertedKeys = keys.toArray(new PNode[keys.size()]);
        PNode[] convertedValues = values.toArray(new PNode[values.size()]);
        return DictLiteralNode.create(convertedKeys, convertedValues);
    }

    public PNode createTupleLiteral(List<PNode> values) {
        PNode[] convertedValues = values.toArray(new PNode[values.size()]);
        return new TupleLiteralNode(convertedValues);
    }

    public PNode createListLiteral(List<PNode> values) {
        PNode[] convertedValues = values.toArray(new PNode[values.size()]);
        return new ListLiteralNode.UninitializedListLiteralNode(convertedValues);
    }

    public PNode createSetLiteral(Set<PNode> values) {
        PNode[] convertedValues = values.toArray(new PNode[values.size()]);
        return new SetLiteralNode(convertedValues);
    }

    public PNode createListComprehension(FrameSlot frameSlot, PNode comprehension) {
        return new ComprehensionNode.ListComprehensionNode(frameSlot, comprehension);
    }

    public PNode createListAppend(FrameSlot frameSlot, PNode right) {
        PNode readList = createReadLocal(frameSlot);
        return ListAppendNodeFactory.create(readList, right);
    }

    public PNode createDictComprehension(FrameSlot frameSlot, PNode comprehension) {
        return new ComprehensionNode.DictComprehensionNode(frameSlot, comprehension);
    }

    public PNode createMapPut(FrameSlot frameSlot, PNode key, PNode value) {
        return MapPutNodeFactory.create(frameSlot, key, value);
    }

    public PNode createUnaryOperation(unaryopType operator, PNode operand) {
        PNode typedNodeOperand = operand;

        switch (operator) {
            case UAdd:
                return PlusNodeFactory.create(typedNodeOperand);
            case USub:
                return MinusNodeFactory.create(typedNodeOperand);
            case Invert:
                return InvertNodeFactory.create(typedNodeOperand);
            case Not:
                return NotNodeFactory.create(typedNodeOperand);
            default:
                throw new RuntimeException("unexpected operation: " + operator);
        }
    }

    public PNode createBinaryOperation(operatorType operator, PNode left, PNode right) {
        switch (operator) {
            case Add:
                return AddNodeFactory.create(left, right);
            case Sub:
                return SubNodeFactory.create(left, right);
            case Mult:
                return MulNodeFactory.create(left, right);
            case Div:
                return DivNodeFactory.create(left, right);
            case FloorDiv:
                return FloorDivNodeFactory.create(left, right);
            case Mod:
                return ModuloNodeFactory.create(left, right);
            case Pow:
                return PowerNodeFactory.create(left, right);
            case LShift:
                return LeftShiftNodeFactory.create(left, right);
            case RShift:
                return RightShiftNodeFactory.create(left, right);
            case BitAnd:
                return BitAndNodeFactory.create(left, right);
            case BitOr:
                return BitOrNodeFactory.create(left, right);
            case BitXor:
                return BitXorNodeFactory.create(left, right);
            default:
                throw new RuntimeException("unexpected operation: " + operator);
        }
    }

    public PNode createComparisonOperation(cmpopType operator, PNode left, PNode right) {
        switch (operator) {
            case Eq:
                return EqualNodeFactory.create(left, right);
            case NotEq:
                return NotEqualNodeFactory.create(left, right);
            case Lt:
                return LessThanNodeFactory.create(left, right);
            case LtE:
                return LessThanEqualNodeFactory.create(left, right);
            case Gt:
                return GreaterThanNodeFactory.create(left, right);
            case GtE:
                return GreaterThanEqualNodeFactory.create(left, right);
            case Is:
                return IsNodeFactory.create(left, right);
            case IsNot:
                return IsNotNodeFactory.create(left, right);
            case In:
                return InNodeFactory.create(left, right);
            case NotIn:
                return NotInNodeFactory.create(left, right);
            default:
                throw new RuntimeException("unexpected operation: " + operator);
        }
    }

    public PNode createBooleanOperation(boolopType operator, PNode left, PNode right) {
        switch (operator) {
            case And:
                return AndNodeFactory.create(left, right);
            case Or:
                return OrNodeFactory.create(left, right);
            default:
                throw new RuntimeException("unexpected operation: " + operator);
        }
    }

    public PNode createGetAttribute(PNode primary, String name) {
        return new GetAttributeNode.UninitializedGetAttributeNode(name, primary);
    }

    public PNode createSlice(PNode lower, PNode upper, PNode step) {
        return SliceNodeFactory.create(lower, upper, step);
    }

    public PNode createIndex(PNode operand) {
        return IndexNodeFactory.create(operand);
    }

    public PNode createSubscriptLoadSlice(PNode primary, PNode slice) {
        return SubscriptLoadSliceNodeFactory.create(primary, slice);
    }

    public PNode createSubscriptLoadIndex(PNode primary, PNode slice) {
        return SubscriptLoadIndexNodeFactory.create(primary, slice);
    }

    public PNode createReadLocal(FrameSlot slot) {
        assert slot != null;
        return ReadLocalVariableNode.create(slot);
    }

    public PNode createReadLevel(FrameSlot slot, int level) {
        return ReadLevelVariableNode.create(slot, level);
    }

    public PNode createWriteLocal(PNode right, FrameSlot slot) {
        return WriteLocalVariableNodeFactory.create(slot, right);
    }

    public PNode createReadGlobalScope(PythonContext context, PythonModule globalScope, String attributeId) {
        return ReadGlobalNode.create(context, globalScope, attributeId);
    }

    public PNode createBooleanLiteral(boolean value) {
        return new BooleanLiteralNode(value);
    }

    public PNode createNoneLiteral() {
        return new NoneLiteralNode();
    }

    public PNode createObjectLiteral(Object obj) {
        return new ObjectLiteralNode(obj);
    }

    public PNode createKeywordLiteral(PNode value, String name) {
        return new KeywordLiteralNode(value, name);
    }

    public List<PythonTree> castToPythonTreeList(List<stmt> argsInit) {
        List<PythonTree> pythonTreeList = new ArrayList<>();

        for (stmt s : argsInit) {
            pythonTreeList.add(s);
        }

        return pythonTreeList;
    }

    public CastToBooleanNode toBooleanCastNode(PNode node) {
        if (node instanceof CastToBooleanNode) {
            return (CastToBooleanNode) node;
        } else {
            return createYesNode(node);
        }
    }

    public CastToBooleanNode createYesNode(PNode operand) {
        return YesNodeFactory.create(operand);
    }

    public StatementNode createTryFinallyNode(PNode body, PNode finalbody) {
        return new TryFinallyNode(body, finalbody);
    }

    public StatementNode createAssert(CastToBooleanNode condition, PNode message) {
        return new AssertNode(condition, message);
    }

    public StatementNode createWithNode(PNode withContext, PNode[] targetNodes, PNode body) {
        return WithNode.create(withContext, targetNodes, body);
    }

}
