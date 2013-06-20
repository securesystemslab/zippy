package org.python.ast.nodes;

import java.math.*;
import java.util.*;
import java.util.List;

import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;
import org.python.ast.datatypes.*;
import org.python.ast.nodes.expressions.BinaryArithmeticNodeFactory.*;
import org.python.ast.nodes.expressions.BinaryBitwiseNodeFactory.*;
import org.python.ast.nodes.expressions.BinaryComparisonNodeFactory.*;
import org.python.ast.nodes.expressions.ComprehensionNodeFactory.*;
import org.python.ast.nodes.expressions.*;
import org.python.ast.nodes.expressions.BinaryBooleanNodeFactory.*;
import org.python.ast.nodes.expressions.UnaryArithmeticNodeFactory.*;
import org.python.ast.nodes.expressions.CompoundBinaryOpsNode.*;
import org.python.ast.nodes.literals.*;
import org.python.ast.nodes.statements.*;
import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.*;

public class NodeFactory {

    public RootNode createModule(List<StatementNode> body, FrameDescriptor fd) {
        return new ModuleNode(body.toArray(new StatementNode[body.size()]), fd);
    }

    public StatementNode createFunctionDef(FrameSlot slot, String name, ParametersNode parameters, CallTarget callTarget, RootNode funcRoot) {
        return new FunctionDefNode(slot, name, parameters, callTarget, funcRoot);
    }

    public FunctionRootNode createFunctionRoot(ParametersNode parameters, StatementNode body) {
        return new FunctionRootNode(parameters, body);
    }

    public RootNode createGeneratorRoot(ParametersNode parameters, StatementNode body) {
        return new GeneratorRootNode(parameters, body);
    }

    public ParametersNode createParametersOfSizeOne(LeftHandSideNode parameter, List<String> paramNames) {
        return new ParametersOfSizeOneNode(paramNames, parameter);
    }

    public ParametersNode createParametersOfSizeTwo(LeftHandSideNode parameter0, LeftHandSideNode parameter1, List<String> paramNames) {
        return new ParametersOfSizeTwoNode(paramNames, parameter0, parameter1);
    }

    public ParametersNode createParametersWithDefaults(List<TypedNode> parameters, List<TypedNode> defaults, List<String> paramNames) {
        return new ParametersWithDefaultsNode(parameters.toArray(new TypedNode[parameters.size()]), defaults.toArray(new TypedNode[defaults.size()]), paramNames);
    }

    public ParametersNode createParametersWithNoDefaults(List<TypedNode> parameters, List<String> paramNames) {
        return new ParametersWithNoDefaultsNode(parameters.toArray(new TypedNode[parameters.size()]), paramNames);
    }

    public BlockNode createBlock(List<StatementNode> statements) {
        StatementNode[] array = statements.toArray(new StatementNode[statements.size()]);

        if (array.length == 1) {
            return new OneStmtBlockNode(array[0]);
        } else if (array.length == 2) {
            return new TwoStmtsBlockNode(array[0], array[1]);
        } else if (array.length == 3) {
            return new ThreeStmtsBlockNode(array[0], array[1], array[2]);
        } else if (array.length == 4) {
            return new FourStmtsBlockNode(array[0], array[1], array[2], array[3]);
        } else {
            return new BlockNode(array);
        }
    }

    public StatementNode createImport(FrameSlot[] slots, String fromModule, String[] names) {
        return new ImportNode(slots, fromModule, names);
    }

    public StatementNode createWhile(ConditionNode condition, BlockNode body, BlockNode orelse) {
        return new WhileNode(condition, body, orelse);
    }
    
    public StatementNode createWhileTrue(BlockNode body) {
        return new WhileTrueNode(body);
    }

    public StatementNode createIf(TypedNode condition, BlockNode thenPart, BlockNode elsePart) {
        return IfNodeFactory.create(condition, thenPart, elsePart);
    }
    
    public StatementNode createIfNot(TypedNode condition, BlockNode thenPart, BlockNode elsePart) {
        return IfNotNodeFactory.create(condition, thenPart, elsePart);
    }

    public StatementNode createFor(LeftHandSideNode target, TypedNode iterator, BlockNode body, BlockNode orelse) {
        return new ForNode(target, iterator, body, orelse);
    }
    
    public StatementNode createForRangeWithOneValue(LeftHandSideNode target, TypedNode start, BlockNode body, BlockNode orelse) {
        return new ForRangeWithOneValueNode(target, start, body, orelse);
    }
    
    public StatementNode createForRangeWithTwoValues(LeftHandSideNode target, TypedNode start, TypedNode stop, BlockNode body, BlockNode orelse) {
        return new ForRangeWithTwoValuesNode(target, start, stop, body, orelse);
    }

    public StatementNode createReturn() {
        return new ReturnNode();
    }

    public StatementNode createExplicitReturn(TypedNode value) {
        return new ExplicitReturnNode(value);
    }

    public StatementNode createBreak() {
        return new BreakNode();
    }

    public StatementNode createYield(TypedNode right) {
        return new YieldNode(right);
    }

    public StatementNode createPrint(List<TypedNode> values, boolean nl) {
        return new PrintNode(values.toArray(new TypedNode[values.size()]), nl);
    }

    public TypedNode createIntegerLiteral(int value) {
        return IntegerLiteralNodeFactory.create(value);
    }

    public TypedNode createBigIntegerLiteral(BigInteger value) {
        return BigIntegerLiteralNodeFactory.create(value);
    }

    public TypedNode createDoubleLiteral(double value) {
        return DoubleLiteralNodeFactory.create(value);
    }

    public TypedNode createComplexLiteral(PComplex value) {
        return ComplexLiteralNodeFactory.create(value);
    }

    public TypedNode createStringLiteral(PyString value) {
        return StringLiteralNodeFactory.create(value.getString());
    }

    public TypedNode createDictLiteral(List<TypedNode> keys, List<TypedNode> values) {
        TypedNode[] convertedKeys = keys.toArray(new TypedNode[keys.size()]);
        TypedNode[] convertedValues = values.toArray(new TypedNode[values.size()]);
        return DictLiteralNodeFactory.create(convertedKeys, convertedValues);
    }

    public TypedNode createTupleLiteral(List<TypedNode> values) {
        TypedNode[] convertedValues = values.toArray(new TypedNode[values.size()]);
        return TupleLiteralNodeFactory.create(convertedValues);
    }

    public TypedNode createListLiteral(List<TypedNode> values) {
        TypedNode[] convertedValues = values.toArray(new TypedNode[values.size()]);
        return ListLiteralNodeFactory.create(convertedValues);
    }

    public TypedNode createListComprehension(ComprehensionNode comprehension) {
        return ListComprehensionNodeFactory.create(comprehension);
    }

    public TypedNode createOuterComprehension(LeftHandSideNode target, TypedNode iterator, ConditionNode condition, TypedNode innerLoop) {
        return OuterComprehensionNodeFactory.create(target, iterator, condition, innerLoop);
    }

    public TypedNode createInnerComprehension(LeftHandSideNode target, TypedNode iterator, ConditionNode condition, TypedNode loopBody) {
        return InnerComprehensionNodeFactory.create(target, iterator, condition, loopBody);
    }

    public TypedNode createGeneratorExpression(GeneratorNode generator, FrameDescriptor descriptor) {
        return GeneratorExpressionNodeFactory.create(generator, descriptor);
    }

    public GeneratorNode createGenerator(ComprehensionNode comprehension) {
        return new GeneratorNode(ParametersNode.EMPTY_PARAMS, comprehension, "<generator_exp>");
    }

    public TypedNode createUnaryOperation(unaryopType operator, TypedNode operand) {
        TypedNode typedNodeOperand = operand;

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

    public TypedNode createBinaryOperation(operatorType operator, TypedNode left, TypedNode right) {
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

    public TypedNode createAttributeCall(TypedNode primary, TypedNode[] args, String name) {
        return AttributeCallNodeFactory.create(primary, args, name);
    }

    public TypedNode createBinaryOperations(TypedNode left, operatorType op, List<TypedNode> rights) {
        TypedNode current = createBinaryOperation((operatorType) op, left, rights.get(0));

        for (int i = 1; i < rights.size(); i++) {
            TypedNode right = rights.get(i);
            current = createBinaryOperation(op, current, right);
        }

        return current;
    }

    public TypedNode createComparisonOperation(cmpopType operator, TypedNode left, TypedNode right) {
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
        default:
            throw new RuntimeException("unexpected operation: " + operator);
        }
    }

    public TypedNode createComparisonOperations(TypedNode left, List<cmpopType> ops, List<TypedNode> rights) {
        if (rights.size() == 1) {
            return createComparisonOperation(ops.get(0), left, rights.get(0));
        }

        List<ConditionNode> comparisons = new ArrayList<ConditionNode>();
        TypedNode current = createComparisonOperation(ops.get(0), left, rights.get(0));
        comparisons.add(current);

        for (int i = 1; i < rights.size(); i++) {
            left = rights.get(i - 1);
            TypedNode right = rights.get(i);
            current = createComparisonOperation(ops.get(i), left, right);
            comparisons.add(current);
        }

        current = createCompoundComparisons(comparisons);
        return current;
    }

    TypedNode createCompoundComparisons(List<ConditionNode> comparisons) {
        return new CompoundComparisonsNode(comparisons.toArray(new TypedNode[comparisons.size()]));
    }

    TypedNode createBooleanOperation(boolopType operator, TypedNode left, TypedNode right) {
        switch (operator) {
        case And:
            return AndNodeFactory.create(left, right);
        case Or:
            return OrNodeFactory.create(left, right);
        default:
            throw new RuntimeException("unexpected operation: " + operator);
        }
    }

    public TypedNode createBooleanOperations(TypedNode left, boolopType operator, List<TypedNode> rights) {
        TypedNode current = createBooleanOperation(operator, left, rights.get(0));

        for (int i = 1; i < rights.size(); i++) {
            TypedNode right = rights.get(i);
            current = createBooleanOperation(operator, current, right);
        }

        return current;
    }

    public TypedNode createAttributeRef(TypedNode operand, String name) {
        return AttributeRefNodeFactory.create(operand, name);
    }

    public TypedNode createAttributeUpdate(TypedNode primary, String name, TypedNode value) {
        return AttributeUpdateNodeFactory.create(primary, name, value);
    }

    public TypedNode createSlice(TypedNode lower, TypedNode upper, TypedNode step) {
        return SliceNodeFactory.create(lower, upper, step);
    }

    public TypedNode createIndex(TypedNode operand) {
        return IndexNodeFactory.create(operand);
    }

    public TypedNode createSubscriptLoad(TypedNode primary, TypedNode slice) {
        return SubscriptLoadNodeFactory.create(primary, slice);
    }

    public TypedNode createSubscriptStore(TypedNode primary, TypedNode slice, TypedNode value) {
        return SubscriptStoreNodeFactory.create(primary, slice, value);
    }

    public TypedNode createReadLocal(FrameSlot slot) {
        return ReadLocalNodeFactory.create(slot);
    }

    public TypedNode createReadEnvironment(FrameSlot slot, int level) {
        return ReadEnvironmentNodeFactory.create(slot, level);
    }

    public TypedNode createWriteLocal(TypedNode right, FrameSlot slot) {
        return WriteLocalNodeFactory.create(slot, right);
    }

    public TypedNode createLHWriteLocal(FrameSlot slot) {
        return LHWriteLocalNodeFactory.create(slot);
    }

    public TypedNode createWriteGlobal(String name, TypedNode right) {
        return WriteGlobalNodeFactory.create(name, right);
    }

    public TypedNode createMultiAssignment(List<TypedNode> targets, TypedNode right) {
        return MultiAssignmentNodeFactory.create(castToLeftHandSideNodes(targets), right);
    }
    
    public TypedNode createTwoAssignment(TypedNode target0, TypedNode target1, TypedNode right) {
        return TwoAssignmentNodeFactory.create(castToLeftHandSideNode(target0), castToLeftHandSideNode(target1), right);
    }
    
    public TypedNode createThreeAssignment(TypedNode target0, TypedNode target1, TypedNode target2, TypedNode right) {
        return ThreeAssignmentNodeFactory.create(castToLeftHandSideNode(target0), castToLeftHandSideNode(target1), castToLeftHandSideNode(target2), right);
    }

    public TypedNode createAssignmentTargetSequence(List<TypedNode> targets) {
        return AssignmentTargetSequenceNodeFactory.create(castToLeftHandSideNodes(targets));
    }

    public TypedNode createReadGlobal(String name) {
        return ReadGlobalNodeFactory.create(name);
    }

    public TypedNode createBooleanLiteral(boolean value) {
        return BooleanLiteralNodeFactory.create(value);
    }

    public TypedNode createNoneLiteral() {
        return NoneLiteralNodeFactory.create();
    }

    public TypedNode createCall(TypedNode callee, TypedNode[] arguments, TypedNode[] keywords) {
        return CallNodeFactory.create(callee, arguments, keywords);
    }
    
    public TypedNode createCallBuiltIn(PCallable callee, String name, TypedNode[] arguments, TypedNode[] keywords) {
        return CallBuiltInNodeFactory.create(callee, name, arguments, keywords);
    }

    public TypedNode createKeywordLiteral(TypedNode value, String name) {
        return KeywordLiteralNodeFactory.create(value, name);
    }

    public TypedNode createCallWithOneArgumentNoKeyword(TypedNode callee, TypedNode argument) {
        return CallWithOneArgumentNoKeywordNodeFactory.create(callee, argument);
    }
    
    public TypedNode createCallBuiltInWithOneArgNoKeyword(PCallable callee, String name, TypedNode argument) {
        return CallBuiltInWithOneArgNoKeywordNodeFactory.create(callee, name, argument);
    }

    public TypedNode createCallWithTwoArgumentsNoKeyword(TypedNode callee, TypedNode argument0, TypedNode argument1) {
        return CallWithTwoArgumentsNoKeywordNodeFactory.create(callee, argument0, argument1);
    }
    
    public TypedNode createCallBuiltInWithTwoArgsNoKeyword(PCallable callee, String name, TypedNode argument0, TypedNode argument1) {
        return CallBuiltInWithTwoArgsNoKeywordNodeFactory.create(callee, name, argument0, argument1);
    }

    LeftHandSideNode[] castToLeftHandSideNodes(List<TypedNode> nodes) {
        LeftHandSideNode[] convertedNodes = new LeftHandSideNode[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            convertedNodes[i] = castToLeftHandSideNode(nodes.get(i));
        }

        return convertedNodes;
    }

    LeftHandSideNode castToLeftHandSideNode(Node node) {
        try {
            return (LeftHandSideNode) node;
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw Py.SystemError("cannot cast to LeftHandSideNode " + node.getClass().getSimpleName());
    }

    public List<PythonTree> castToPythonTreeList(List<stmt> argsInit) {
        List<PythonTree> pythonTreeList = new ArrayList<PythonTree>();

        for (stmt s : argsInit) {
            pythonTreeList.add(s);
        }

        return pythonTreeList;
    }
    
    public TypedNode createYesNode(TypedNode operand) {
        return YesNodeFactory.create(operand);
    }

    public TypedNode createIfExpNode(TypedNode body, TypedNode test, TypedNode orelse) {
        return IfExpressionNodeFactory.create(body, test, orelse);
    }
    
    public TypedNode createIfNotExpNode(TypedNode body, TypedNode test, TypedNode orelse) {
        return IfNotExpressionNodeFactory.create(body, test, orelse);
    }

}
